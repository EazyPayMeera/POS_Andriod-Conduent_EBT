package com.analogics.tpaymentsapos.rootUiScreens.pleasewait.viewmodel


import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.analogics.paymentservicecore.listeners.responseListener.IPrinterResultProviderListener
import com.analogics.tpaymentcore.Printer.Printer
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.PrinterServiceRepository
import java.io.ByteArrayOutputStream

class PleaseWaitViewModel(context: Context): ViewModel() {

    private val _printStatus = mutableStateOf("")
    val printStatus: MutableState<String> = _printStatus

    private val printer = Printer.getInstance()




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


    fun addImage(format: Bundle, imageData: ByteArray)
    {
        printer.addImage(format,imageData)
    }

    fun addBarcode(format: Bundle, barcode:String)
    {
        printer.barCodePrinting(format,barcode)
    }

    fun addQRCode(format: Bundle, barcode:String)
    {
        printer.qrCodePrinting(format,barcode)
    }

    fun feedLine(lines:Int)
    {
        printer.feedLine(lines)
    }

    suspend fun addReceiptDetails(format: Bundle, iPrinterResultProviderListener: IPrinterResultProviderListener)
    {
        Log.d(TAG, "Initializing printer in viewModel...")
        //PrinterServiceRepository().printReceiptDetails(format, iPrinterResultProviderListener)
    }

    fun startPrint()
    {
        printer.startPrinting()
    }


    fun GetStatus() {
        val status = printer.getPrinterStatus()
        _printStatus.value = when (status) {
            0 -> "Printer is OK"
            240 -> "Error: Status 240"
            243 -> "Error: Status 243"
            225 -> "Error: Status 225"
            247 -> "Error: Status 247"
            251 -> "Error: Status 251"
            242 -> "Error: Status 242"
            else -> "Unknown status: $status"
        }
    }
}