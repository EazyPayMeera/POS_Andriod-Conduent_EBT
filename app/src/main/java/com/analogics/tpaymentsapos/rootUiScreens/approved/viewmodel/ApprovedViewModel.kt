package com.analogics.tpaymentsapos.rootUiScreens.approved.viewmodel


import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
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
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.convertObjRootToTxnEntity
import com.google.zxing.BarcodeFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class ApprovedViewModel @Inject constructor(private var dbRepository: TxnDBRepository,var apiServiceRepository: ApiServiceRepository): ViewModel(),
    IApiServiceResponseListener {


    val isPrinting = mutableStateOf(false)
    val isCustomer = mutableStateOf(false)
    val isPrintingError = mutableStateOf(false)
    var printerStatus: Any? = null // Declare the variable to hold the result

    private val objRoot = MutableStateFlow(ObjRootAppPaymentDetails())
    var userApiErrorHolder = MutableStateFlow(ApiServiceError())


    fun getBitmapBytes(bitmap: Bitmap): ByteArray? {
        var imageData: ByteArray? = null
        try {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            imageData = baos.toByteArray()
        } catch (e: Exception) {
            // TODO: handle exception
            e.printStackTrace()
            return null
        }
        return imageData
    }

    fun getLogoBitmap(context: Context, id: Int): Bitmap {
        val draw = context.resources.getDrawable(id) as BitmapDrawable
        val bitmap = draw.bitmap
        return bitmap
    }

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
                addReceiptDetails(objRootAppPaymentDetail,object : IPrinterResultProviderListener {
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


    suspend fun addReceiptDetails(objRootAppPaymentDetail: ObjRootAppPaymentDetails, iPrinterResultProviderListener: IPrinterResultProviderListener) {
        // Create an instance of ReceiptBuilder
        val receiptBuilder = ReceiptBuilder()

        // Switch to IO context for background processing
        withContext(Dispatchers.IO) {
            // Create the receipt using payment details
            val paymentServiceTxnDetails = PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(
                PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
            )

            // Generate the receipt
            val receipt = receiptBuilder.createReceipt(paymentServiceTxnDetails)

            // Proceed if the receipt was created successfully
            if (receipt != null) {
                val barcodeString = receipt.fields.find { it.first == "BARCODE" }?.second ?: ""

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
                    when (field.third) {
                        ReceiptBuilder.Alignment.LEFT -> 0
                        ReceiptBuilder.Alignment.CENTER -> 1
                        ReceiptBuilder.Alignment.RIGHT -> 2
                        else -> 0 // Default to left alignment if no match
                    }
                }

                // Extract the alignment for the barcode
                val alignment: Int = receipt.fields.firstOrNull { it.first == "QR CODE" }?.let { field ->
                    when (field.third) {
                        ReceiptBuilder.Alignment.LEFT -> 0
                        ReceiptBuilder.Alignment.CENTER -> 1
                        ReceiptBuilder.Alignment.RIGHT -> 2
                        else -> -1 // Default or error value
                    }
                } ?: -1 // Default or error value if no barcode field is found

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
                    alignmentText,
                    iPrinterResultProviderListener
                )
            } else {
                Log.d("ReceiptBuilder", "Failed to create receipt.")
            }
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

    fun updateTxnData(objRootAppPaymentDetails: ObjRootAppPaymentDetails) = viewModelScope.launch {
        // Convert ObjRootAppPaymentDetails to JSON or entity object
        val txnEntity = convertObjRootToTxnEntity(objRootAppPaymentDetails)

        txnEntity.id?.let { id ->
            // Check if the entity exists in the database using the primary key (invoice no)
            val existingEntity = dbRepository.fetchTransactionDetailsTxn(id)

            if (existingEntity != null) {
                // Entry found, proceed with update
                dbRepository.updateTxn(txnEntity)
                Log.d("Record Update", "Record update successful for invoice no: $id")
            } else {
                // Entry not found, log the message
                dbRepository.insertTxn(txnEntity)
            }
        } ?: Log.d("Record Update", "Invoice No is null")
    }
    fun onPurchaseApi(objRootAppPaymentDetail: ObjRootAppPaymentDetails) {
        viewModelScope.launch {
            try {
                val requestDetails =
                    PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
                apiServiceRepository.apiServicePurchase(
                    PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(requestDetails), this@ApprovedViewModel)
            } catch (e: Exception) {
                AppLogger.d(AppLogger.MODULE.APP_UI, e.message ?: "")
            }
        }
    }
    override fun onApiSuccess(response: Any) {
        when (response) {
            is ObjRootAppPaymentDetails -> {
                objRoot.value = response
                updateTxnData(objRoot.value)
            }
        }
    }

    override fun onApiError(apiServiceError: ApiServiceError) {
        userApiErrorHolder.value = apiServiceError
    }

    override fun onDisplayProgress(
        show: Boolean,
        title: String?,
        subTitle: String?,
        message: String?
    ) {

    }

}
