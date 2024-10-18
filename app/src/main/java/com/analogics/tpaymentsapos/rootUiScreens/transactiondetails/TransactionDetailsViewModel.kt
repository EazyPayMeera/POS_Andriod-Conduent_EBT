package com.analogics.tpaymentsapos.rootUiScreens.transactiondetails

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.listeners.responseListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.listeners.responseListener.IPrinterResultProviderListener
import com.analogics.paymentservicecore.logger.AppLogger
import com.analogics.paymentservicecore.model.error.ApiServiceError
import com.analogics.paymentservicecore.repository.apiService.ApiServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUiScreens.utility.ReceiptBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.PrinterServiceRepository
import com.google.zxing.BarcodeFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TransactionDetailsViewModel @Inject constructor(private val dbRepository: TxnDBRepository, val apiServiceRepository: ApiServiceRepository) : ViewModel(),
    IApiServiceResponseListener {

    val isPrinting = mutableStateOf(false)
    val isCustomer = mutableStateOf(false)

    private val objRoot = MutableStateFlow(ObjRootAppPaymentDetails())
    var userApiServiceErrorHolder = MutableStateFlow(ApiServiceError())

    fun printReceipt(
        context: Context,
        customer: Boolean = false,
        objRootAppPaymentDetail: ObjRootAppPaymentDetails
    ) {
        viewModelScope.launch {
            // Call getPrinterStatus with a callback
            getPrinterStatus(objRootAppPaymentDetail, object : IPrinterResultProviderListener {
                override fun onSuccess(result: Any?) {
                    Log.d(TAG, "Printer status retrieved: $result")

                    val subtitleText = when (result) {
                        -1 -> context.resources.getString(R.string.printer_out_of_paper) // Example error for result -1
                        else -> context.resources.getString(R.string.printer_busy) // Default error for other cases
                    }

                    if (result != 0) {
                        Log.d(TAG, "Printer status retrieved inside result not equal to zero: $result")
                        CustomDialogBuilder.composeAlertDialog(
                            title = context.resources.getString(R.string.printer_error_title),
                            subtitle = subtitleText // Dynamic subtitle based on result
                        )
                    } else {
                        // If the printer status is OK, call initPrinter
                        launch { // Start a new coroutine to call initPrinter
                            try {
                                initPrinter(context,objRootAppPaymentDetail, object : IPrinterResultProviderListener {
                                    override fun onSuccess(result: Any?) {
                                        Log.d(TAG, "Printer initialized successfully.")
                                    }

                                    override fun onFailure(exception: Exception) {
                                        Log.e(TAG, "Failed to initialize printer: ${exception.message}")
                                        // Handle failure for printer initialization here
                                    }
                                })
                            } catch (e: Exception) {
                                Log.e(TAG, "Error during printer initialization: ${e.message}")
                            }
                        }
                    }
                }

                override fun onFailure(exception: Exception) {
                    Log.e(TAG, "Failed to get printer status: ${exception.message}")
                    // Handle failure for getting printer status here
                }
            })
        }
    }

    suspend fun initPrinter(
        context: Context,
        objRootAppPaymentDetail: ObjRootAppPaymentDetails,
        iPrinterResultProviderListener: IPrinterResultProviderListener
    )
    {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Approved View Model to Printer Service Repository 1")
                CustomDialogBuilder.composeProgressDialog(
                    title = context.resources.getString(R.string.printing),
                    subtitle = context.resources.getString(R.string.plz_wait)
                )
                val requestDetails =
                    PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
                PrinterServiceRepository(PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(requestDetails)).initPrinter(context, iPrinterResultProviderListener)
                addReceiptDetails(context,objRootAppPaymentDetail,object : IPrinterResultProviderListener {
                    override fun onSuccess(result: Any?) {
                        if(result == true)
                        {
                            CustomDialogBuilder.hideProgress()
                        }
                    }
                    override fun onFailure(exception: Exception) {

                    }
                })
                Log.d(TAG, "Approved View Model to Printer Service Repository 2 ${PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(requestDetails)}")
            } catch (e: Exception) {
                AppLogger.d(AppLogger.MODULE.APP_UI, e.message ?: "")
            }
        }

    }


    suspend fun addReceiptDetails(
        context: Context,
        objRootAppPaymentDetail: ObjRootAppPaymentDetails,
        iPrinterResultProviderListener: IPrinterResultProviderListener
    ) {
        // Create an instance of ReceiptBuilder
        val receiptBuilder = ReceiptBuilder()

        // Switch to IO context for background processing
        withContext(Dispatchers.IO) {
            // Create the receipt using payment details
            val paymentServiceTxnDetails = PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(
                PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
            )

            // Generate the receipt
            val receipt = receiptBuilder.createReceipt(context,paymentServiceTxnDetails)

            val barcodeString = receipt.fields.find { it.first == "BARCODE" }?.second ?: ""

            // Prepare receipt details for printing
            val receiptDetails = receipt.fields.map { (label, value) ->
                if (value.isEmpty()) {
                    "$label"
                } else {
                    "$label: $value"
                }
            } + receipt.items.mapIndexed { index, item ->
                "${index + 1}. ${item.name}              $${item.price}"
            }

            val alignmentText: List<Int> = receipt.fields.map { field ->
                // Use the alignment directly from field.third
                when (field.third) {
                    ReceiptBuilder.Alignment.LEFT -> 0
                    ReceiptBuilder.Alignment.CENTER -> 1
                    ReceiptBuilder.Alignment.RIGHT -> 2
                    else -> 0 // Default to left alignment if no match
                }
            }

            // Extract the alignment for the barcode only (assuming it's the first field or modify as needed)
            val alignment: Int = receipt.fields.firstOrNull { it.first == "QR CODE" }?.let { field ->
                when (field.third) {
                    ReceiptBuilder.Alignment.LEFT -> 0
                    ReceiptBuilder.Alignment.CENTER -> 1
                    ReceiptBuilder.Alignment.RIGHT -> 2
                    else -> -1 // Return a default or error value if alignment is not found
                }
            } ?: -1 // Return a default or error value if no barcode field is found

            // Prepare the printing format
            val format = Bundle().apply {
                putInt("align", alignment) // This might be your default alignment for text
                putInt("width", 300)
                putInt("height", 100)
                putSerializable("barcode_type", BarcodeFormat.CODE_39)
            }

            // Pass the receipt details to the PrinterServiceRepository
            PrinterServiceRepository(paymentServiceTxnDetails).printReceiptDetails(
                format,
                barcodeString,
                receiptDetails,
                alignmentText,
                iPrinterResultProviderListener
            )
        }
    }

    suspend fun getPrinterStatus(objRootAppPaymentDetail: ObjRootAppPaymentDetails,iPrinterResultProviderListener: IPrinterResultProviderListener) {
        try {

            val paymentServiceTxnDetails = PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(
                PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
            )
            PrinterServiceRepository(paymentServiceTxnDetails).getStatus(iPrinterResultProviderListener)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to get printer status: ${e.message}")
        }
    }

    override fun onApiSuccess(response: Any) {
        when (response) {
            is ObjRootAppPaymentDetails -> {
                objRoot.value = response
            }
            //delete entery from db
        }

    }

    override fun onApiError(paymentError: ApiServiceError) {
        Log.e("API Response", paymentError.errorMessage)
        userApiServiceErrorHolder.value = paymentError
    }

    override fun onDisplayProgress(
        show: Boolean,
        title: String?,
        subTitle: String?,
        message: String?
    ) {
        CustomDialogBuilder.composeProgressDialog(show = show, title = title, subtitle = subTitle, message = message)
    }
}