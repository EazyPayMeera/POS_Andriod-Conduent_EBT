package com.analogics.tpaymentsapos.rootUiScreens.pleasewait.viewmodel

import PrinterServiceRepository
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.analogics.paymentservicecore.listeners.responseListener.IPrinterResultProviderListener
import com.analogics.tpaymentcore.Printer.Printer
import com.analogics.tpaymentsapos.rootUiScreens.approved.viewmodel.ApprovedViewModel
import java.io.ByteArrayOutputStream

class PleaseWaitViewModel(context: Context): ViewModel() {

    private val _printStatus = mutableStateOf("")
    val printStatus: MutableState<String> = _printStatus

    private val printer = Printer.getInstance()


    data class Receipt(
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

    fun printReceiptDetails(receipt: ApprovedViewModel.Receipt) {
        val receiptDetails = listOf(
            "********** RECEIPT **********",
            "",
            "MERCHANT: ${receipt.merchantName}",
            "ADDRESS: ${receipt.address}",
            "PHONE:   ${receipt.phone}",
            "----------------------------------------",
            "TRANSACTION DETAILS",
            "----------------------------------------",
            "DATE/TIME:   ${receipt.transactionDetails.dateTime}",
            "RECEIPT #:   ${receipt.transactionDetails.receiptNumber}",
            "TERMINAL #:  ${receipt.transactionDetails.terminalNumber}",
            "----------------------------------------",
            "ITEMS PURCHASED",
            "----------------------------------------"
        ) + receipt.items.mapIndexed { index, item ->
            "${index + 1}. ${item.name}              $${item.price}"
        } + listOf(
            "----------------------------------------",
            "SUBTOTAL:              $${receipt.subtotal}",
            "TAX (5%):              $${receipt.tax}",
            "TOTAL:                 $${receipt.total}",
            "----------------------------------------",
            "PAYMENT METHOD: ${receipt.paymentMethod}",
            "CARD NUMBER:   ${receipt.cardNumber}",
            "AUTH CODE:     ${receipt.authCode}",
            "----------------------------------------",
            "THANK YOU FOR SHOPPING",
            "   WITH US TODAY!",
            "PLEASE VISIT US AGAIN SOON!",
            "----------------------------------------",
            "CUSTOMER SERVICE CONTACT",
            "TEL: ${receipt.customerService.phone}",
            "EMAIL: ${receipt.customerService.email}",
            "----------------------------------------"
        )

        //printer.addText(receiptDetails)
        addTextDetails(receiptDetails)
    }

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



    fun initPrint(context: Context) {
        printer.initPrint(context) // Pass the context instance directly
    }


    fun addTextDetails(texts: List<String>) {
        printer.printMultipleTextsAndStartPrinting(texts)
    }

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

    suspend fun initPrinter(context: Context, iPrinterResultProviderListener: IPrinterResultProviderListener)
    {
        PrinterServiceRepository().initPrinter(context,iPrinterResultProviderListener)
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