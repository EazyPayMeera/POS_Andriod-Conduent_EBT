package com.analogics.tpaymentsapos.rootUiScreens.approved.viewmodel


import addLogo
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.analogics.paymentservicecore.listeners.responseListener.IPrinterResultProviderListener
import com.analogics.paymentservicecore.logger.AppLogger
import com.analogics.paymentservicecore.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUiScreens.utility.ReceiptBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.PrinterServiceRepository
import com.google.zxing.BarcodeFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import getPrinterStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ApprovedViewModel @Inject constructor(private var dbRepository: TxnDBRepository): ViewModel()
{
    fun printReceipt(
        logoResId: Int,
        sharedViewModel: SharedViewModel,
        context: Context,
        customer: Boolean = false,
        objRootAppPaymentDetail: ObjRootAppPaymentDetails
    ) {
        viewModelScope.launch {
            // Call getPrinterStatus with a callback
            getPrinterStatus(objRootAppPaymentDetail, object : IPrinterResultProviderListener {
                override fun onSuccess(result: Any?) {

                    val subtitleText = when (result) {
                        -1 -> context.resources.getString(R.string.printer_out_of_paper) // Example error for result -1
                        else -> context.resources.getString(R.string.printer_busy) // Default error for other cases
                    }

                    if (result != 0) {
                        CustomDialogBuilder.composeAlertDialog(
                            title = context.resources.getString(R.string.printer_error_title),
                            subtitle = subtitleText // Dynamic subtitle based on result
                        )
                    } else {
                        // If the printer status is OK, call initPrinter
                        launch { // Start a new coroutine to call initPrinter
                            try {
                                initPrinter(logoResId,sharedViewModel,context,objRootAppPaymentDetail, object : IPrinterResultProviderListener {
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
        logoResId: Int,
        sharedViewModel: SharedViewModel,
        context: Context,
        objRootAppPaymentDetail: ObjRootAppPaymentDetails,
        iPrinterResultProviderListener: IPrinterResultProviderListener
    )
    {
        viewModelScope.launch {
            try {
                CustomDialogBuilder.composePrintingDialog(
                    title = context.resources.getString(R.string.printing),
                    subtitle = context.resources.getString(R.string.plz_wait),
                    onClose = {
                        viewModelScope.launch {
                            stopPrinting(objRootAppPaymentDetail, iPrinterResultProviderListener)
                        }
                    }
                )
                val requestDetails =
                    PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
                PrinterServiceRepository(PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(requestDetails)).initPrinter(context, iPrinterResultProviderListener)
                addLogo(context,objRootAppPaymentDetail,iPrinterResultProviderListener,logoResId)
                addReceiptDetails(sharedViewModel,context,objRootAppPaymentDetail,object : IPrinterResultProviderListener {
                    override fun onSuccess(result: Any?) {
                        if(result == true)
                        {
                            viewModelScope.launch {
                                CustomDialogBuilder.hideProgress()
                            }

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



    suspend fun addReceiptDetails(sharedViewModel: SharedViewModel, context: Context, objRootAppPaymentDetail: ObjRootAppPaymentDetails, iPrinterResultProviderListener: IPrinterResultProviderListener) {
        // Create an instance of ReceiptBuilder
        val receiptBuilder = ReceiptBuilder()

        // Switch to IO context for background processing
        withContext(Dispatchers.IO) {
            // Create the receipt using payment details
            val paymentServiceTxnDetails = PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(
                PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
            )

            // Generate the receipt
            val receipt = receiptBuilder.createReceipt(context,sharedViewModel,paymentServiceTxnDetails)
            val labelList: List<String> = receipt.fields.map { it.label.toString() }
            val descriptionList: List<String> = receipt.fields.map { it.description.toString() }
            val aligment: List<String> = receipt.fields.map { it.alignment.toString() }
            // Proceed if the receipt was created successfully
            if (receipt != null) {
                val barcodeString = receipt.fields.find { it.label == "BARCODE" }?.value ?: ""

                val receiptDetails = receipt.fields.map { (label, value) ->
                    if (value?.isEmpty() == true) {
                        "$label"
                    } else {
                        "$label: $value"
                    }
                } + receipt.items.mapIndexed { index, item ->
                    "${index + 1}. ${item.name}              $${item.price}"
                }

                val alignmentText: List<Int> = receipt.fields.map { field ->
                    when (field.alignment) {
                        ReceiptBuilder.Alignment.LEFT -> 0
                        ReceiptBuilder.Alignment.CENTER -> 1
                        ReceiptBuilder.Alignment.RIGHT -> 2
                        ReceiptBuilder.Alignment.NONE -> -1
                        else -> 0 // Default to left alignment if no match
                    }
                }

                // Extract the alignment for the barcode
                val alignment: Int = receipt.fields.firstOrNull { it.label == "QR CODE" }?.let { field ->
                    when (field.alignment) {
                        ReceiptBuilder.Alignment.LEFT -> 0
                        ReceiptBuilder.Alignment.CENTER -> 1
                        ReceiptBuilder.Alignment.RIGHT -> 2
                        else -> -1 // Default or error value
                    }
                } ?: -1 // Default or error value if no barcode field is found

                val fontsize: List<Int> = receipt.fields.map { field ->
                    when (field.fontsize) {
                        ReceiptBuilder.FontSize.Small -> 24
                        ReceiptBuilder.FontSize.Medium -> 28
                        ReceiptBuilder.FontSize.Big -> 32
                        else -> 1 // Default to left alignment if no match
                    }
                }

                // Prepare the printing format
                val format = Bundle().apply {
                    putInt("align", alignment) // Default alignment for text
                    putInt("width", 300)
                    putInt("height", 100)
                    putSerializable("barcode_type", BarcodeFormat.CODE_39)
                }
                // Pass the receipt details to the PrinterServiceRepository
                PrinterServiceRepository(paymentServiceTxnDetails).printReceiptDetails(
                    format,
                    barcodeString,
                    receiptDetails,
                    descriptionList,
                    alignmentText,
                    fontsize,
                    iPrinterResultProviderListener
                )
            } else {
                Log.d("ReceiptBuilder", "Failed to create receipt.")
            }
        }
    }


    suspend fun stopPrinting(objRootAppPaymentDetail: ObjRootAppPaymentDetails,iPrinterResultProviderListener: IPrinterResultProviderListener) {
        try {

            val paymentServiceTxnDetails = PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(
                PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
            )
            PrinterServiceRepository(paymentServiceTxnDetails).stopPrinting(iPrinterResultProviderListener)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to get printer status: ${e.message}")
        }
    }

}
