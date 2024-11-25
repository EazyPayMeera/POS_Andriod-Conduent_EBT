
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import com.analogics.paymentservicecore.listeners.responseListener.IPrinterResultProviderListener
import com.analogics.paymentservicecore.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.PrinterServiceRepository
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getBitmapBytes
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getLogoBitmap
import java.io.ByteArrayOutputStream


suspend fun getPrinterStatus(objRootAppPaymentDetail: ObjRootAppPaymentDetails, iPrinterResultProviderListener: IPrinterResultProviderListener) {
    try {

        val paymentServiceTxnDetails = PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(
            PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
        )
        PrinterServiceRepository(paymentServiceTxnDetails).getStatus(iPrinterResultProviderListener)

    } catch (e: Exception) {
        Log.e(TAG, "Failed to get printer status: ${e.message}")
    }
}


suspend fun addLogo(context: Context, objRootAppPaymentDetail: ObjRootAppPaymentDetails, iPrinterResultProviderListener: IPrinterResultProviderListener, logoResId: Int)
{
    val paymentServiceTxnDetails = PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(
        PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
    )
    val logoBitmap = getLogoBitmap(context, logoResId)

    // Convert the bitmap to ByteArray
    val imageData = getBitmapBytes(logoBitmap)

    // Ensure the imageData is not null
    if (imageData != null) {
        // Prepare the format Bundle for the printer
        val format = Bundle().apply {
            putInt("align", 1)  // Example alignment: Center
            putInt("width", 100)  // Width of the image
            putInt("height", 100)  // Height of the image
        }

        // Call the addImage function with format and image data
        PrinterServiceRepository(paymentServiceTxnDetails).printImage(format,imageData,iPrinterResultProviderListener)
    } else {
        // Handle the case where the image data is null
        Log.e("ImageError", "Failed to get image bytes")
    }
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