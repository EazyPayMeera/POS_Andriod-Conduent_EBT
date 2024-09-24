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
import com.analogics.securityframework.database.entity.TxnEntity
import com.analogics.tpaymentcore.Printer.Printer
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.PrinterServiceRepository
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.ReceiptBuilder
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class ApprovedViewModel(var dbRepository: TxnDBRepository, context: Context): ViewModel() {

    private val _printStatus = mutableStateOf("")
    val printStatus: MutableState<String> = _printStatus
    val isPrinting = mutableStateOf(false)

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

    fun printReceipt(context: Context)
    {
        printer.startPrinting()
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

    fun printReceipt(coroutineScope: CoroutineScope)
    {
        isPrinting.value = true
        coroutineScope.launch {
            delay(3000)
            isPrinting.value = false
        }
    }

    suspend fun initPrinter(context: Context, iPrinterResultProviderListener: IPrinterResultProviderListener)
    {
        val receiptBuilder = ReceiptBuilder() // Create an instance of ReceiptBuilder
        Log.d(TAG, "Initializing printer in viewModel...")
        PrinterServiceRepository(receiptBuilder).initPrinter(context, iPrinterResultProviderListener)
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

    // Update all the entities by setting invoice no as primary key
    fun updateTxnData(objRootAppPaymentDetails: ObjRootAppPaymentDetails)=viewModelScope.launch{
        val json = Gson().toJson(objRootAppPaymentDetails) // Convert ObjRootAppPaymentDetails to JSON

        dbRepository.updateTxn(Gson().fromJson(json, TxnEntity::class.java))
        Log.d("password " +
                "record insert suc", Gson().fromJson(json, TxnEntity::class.java).toString())

    }


}
