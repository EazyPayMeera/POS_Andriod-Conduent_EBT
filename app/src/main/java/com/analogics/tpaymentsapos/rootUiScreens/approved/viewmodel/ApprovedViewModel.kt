package com.analogics.tpaymentsapos.rootUiScreens.approved.viewmodel


import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.MutableState
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
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.utility.ReceiptBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.PrinterServiceRepository
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.convertObjRootToTxnEntity
import com.google.zxing.BarcodeFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class ApprovedViewModel @Inject constructor(private var dbRepository: TxnDBRepository,var apiServiceRepository: ApiServiceRepository): ViewModel(),
    IApiServiceResponseListener {

    private val _printStatus = mutableStateOf("")
    val printStatus: MutableState<String> = _printStatus
    val isPrinting = mutableStateOf(false)
    val isCustomer = mutableStateOf(false)

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

    @OptIn(DelicateCoroutinesApi::class)
    fun printReceipt(
        context: Context,
        customer: Boolean = false,
        objRootAppPaymentDetail: ObjRootAppPaymentDetails
    )
    {
        isPrinting.value = true
        isCustomer.value = customer

        GlobalScope.launch {
            initPrinter(context,objRootAppPaymentDetail, object : IPrinterResultProviderListener {
                override fun onSuccess(result: Any?) {
                    isPrinting.value = false
                }

                override fun onFailure(exception: Exception) {
                    isPrinting.value = false
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
                val requestDetails =
                    PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
                PrinterServiceRepository(PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(requestDetails)).initPrinter(context, iPrinterResultProviderListener)
                addReceiptDetails(objRootAppPaymentDetail,iPrinterResultProviderListener)
                Log.d(TAG, "Approved View Model to Printer Service Repository 2 ${PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(requestDetails)}")
            } catch (e: Exception) {
                AppLogger.d(AppLogger.MODULE.APP_UI, e.message ?: "")
            }
        }

    }


    suspend fun addReceiptDetails(objRootAppPaymentDetail: ObjRootAppPaymentDetails,iPrinterResultProviderListener: IPrinterResultProviderListener)
    {
        // Create an instance of ReceiptBuilder
        val receiptBuilder = ReceiptBuilder()

        // Create the receipt using payment details
        val paymentServiceTxnDetails = PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(
            PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
        )

        // Generate the receipt
        val receipt = receiptBuilder.createReceipt(paymentServiceTxnDetails)

        val barcodeString = receipt.fields.find { it.first == "BARCODE" }?.second ?: ""

        val receiptDetails = receipt.fields.map { (label, value) ->
            if (value.isEmpty())
            {
                "$label"
            }
            else {
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
        PrinterServiceRepository(paymentServiceTxnDetails).printReceiptDetails(format,barcodeString,receiptDetails,alignmentText, iPrinterResultProviderListener)
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
