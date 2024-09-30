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
import com.analogics.paymentservicecore.listeners.responseListener.IPrinterResultProviderListener
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.tpaymentcore.Printer.Printer
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.PrinterServiceRepository
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ReceiptBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.convertObjRootToTxnEntity
import com.google.zxing.BarcodeFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class ApprovedViewModel @Inject constructor(private var dbRepository: TxnDBRepository): ViewModel() {

    private val _printStatus = mutableStateOf("")
    val printStatus: MutableState<String> = _printStatus
    val isPrinting = mutableStateOf(false)
    val isCustomer = mutableStateOf(false)

    private val printer = Printer.getInstance()


    data class Receipt(
        val receiptHeaader: String,
        val merchantHead : String,
        val merchantName: String,
        val address: String,
        val phone: String,
        val transactionDetails: TransactionDetails,
        val items: List<ReceiptItem>,
        val subtotal: Double,
        val tax: Double,
        val total: Double,
        val paymentMethod: String,
        val cardNumber: String,
        val authCode: String,
        val customerService: CustomerService
    )

    data class TransactionDetails(
        val dateTime: String,
        val receiptNumber: String,
        val terminalNumber: String
    )

    data class ReceiptItem(
        val name: String,
        val price: Double
    )

    data class CustomerService(
        val phone: String,
        val email: String
    )


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



/*    fun initPrint(context: Context) {
        printer.initPrint(context) // Pass the context instance directly
    }*/


/*    fun addTextDetails(texts: List<String>) {
        printer.printMultipleTextsAndStartPrinting(texts)
    }*/

    fun addTextLeft(texts:String)  // Add Text on Left Side
    {
        printer.addTextOnlyLeft(texts)
    }

    fun addTextLeftRight(textLeft:String,textRight:String)
    {
        printer.addTextLeft_Right(textLeft,textRight)
    }

    fun addTextLeftCenterRight(textLeft:String,textCenter:String,textRight:String)
    {
        printer.addTextLeft_Center_Right(textLeft,textCenter,textRight)
    }

    fun addImage(format: Bundle, imageData: ByteArray)
    {
        printer.addImage(format,imageData)
    }

    fun addBarcode(format: Bundle,barcode:String)
    {
        printer.barCodePrinting(format,barcode)
    }

    fun addQRCode(format: Bundle,barcode:String)
    {
        printer.qrCodePrinting(format,barcode)
    }

    fun feedLine(lines:Int)
    {
        printer.feedLine(lines)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun printReceipt(context: Context, customer: Boolean = false)
    {
        isPrinting.value = true
        isCustomer.value = customer

        GlobalScope.launch {
            initPrinter(context, object : IPrinterResultProviderListener {
                override fun onSuccess(result: Any?) {
                    isPrinting.value = false
                }

                override fun onFailure(exception: Exception) {
                    isPrinting.value = false
                }
            })
        }
    }

    suspend fun initPrinter(context: Context, iPrinterResultProviderListener: IPrinterResultProviderListener)
    {
        val receiptBuilder = ReceiptBuilder() // Create an instance of ReceiptBuilder
        Log.d(TAG, "Initializing printer in viewModel...")
        PrinterServiceRepository(receiptBuilder).initPrinter(context, iPrinterResultProviderListener)
        addReceiptDetails(iPrinterResultProviderListener)
    }


    suspend fun addReceiptDetails(iPrinterResultProviderListener: IPrinterResultProviderListener)
    {
        val receiptBuilder = ReceiptBuilder()
        Log.d(TAG, "Initializing printer in viewModel...")
        val format = Bundle().apply {
            putInt("align", 1)
            putInt("width", 300)
            putInt("height", 100)
            putSerializable("barcode_type", BarcodeFormat.CODE_39)
        }
        PrinterServiceRepository(receiptBuilder).printReceiptDetails(format, iPrinterResultProviderListener)
    }

/*    // Update all the entities by setting invoice no as primary key
    fun updateTxnData(objRootAppPaymentDetails: ObjRootAppPaymentDetails)=viewModelScope.launch{
        // Convert ObjRootAppPaymentDetails to JSON

        dbRepository.updateTxn(convertObjRootToTxnEntity(objRootAppPaymentDetails))
        Log.d("password " +
                "record update suc ", convertObjRootToTxnEntity(objRootAppPaymentDetails).toString())

    }*/

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



}
