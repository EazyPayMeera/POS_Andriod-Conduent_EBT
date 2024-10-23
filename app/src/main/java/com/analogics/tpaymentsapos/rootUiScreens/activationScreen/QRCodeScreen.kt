package com.analogics.tpaymentsapos.rootUiScreens.activationScreen

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.analogics.paymentservicecore.listeners.responseListener.IScannerResultProviderListener
import com.analogics.tpaymentsapos.rootUiScreens.invoice.openScanner
import com.analogics.tpaymentsapos.rootUiScreens.login.InvoiceViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun QRCodeView(apiKey: String, apiSecret: String, size: Int = 256) {
    // JSON data with API key and secret
    val jsonData = """
        {
          "apiKey": "$apiKey",
          "apiSecret": "$apiSecret"
        }
    """.trimIndent()

    // Generate QR code from JSON data
    val qrBitmap = generateQRCodeBitmap(jsonData, size)

    // Display QR code in Image composable
    qrBitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = "QR Code",
            modifier = Modifier.size(size.dp)
        )
    }
}

// Function to generate QR code bitmap from data
fun generateQRCodeBitmap(data: String, size: Int): Bitmap? {
    return try {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            data, BarcodeFormat.QR_CODE, size, size
        )
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) -0x1000000 else -0x1)
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
@Composable
fun QRCodeScreen(context: Context,viewModel: InvoiceViewModel) {
    val apiKey = "your_api_key"
    val apiSecret = "your_api_secret"

   // QRCodeView(apiKey = apiKey, apiSecret = apiSecret)


                Icon(
                    imageVector = Icons.Default.QrCode,  // Your QR code icon
                    contentDescription = null, // Provide a content description if needed
                    modifier = Modifier
                        .clickable {
                            openScanner(context, viewModel)
                        },  // Toggle editable state on icon click
                    tint = MaterialTheme.colorScheme.primary
                )
}

fun openScanner(context: Context, viewModel: InvoiceViewModel) {
    val coroutineScope = CoroutineScope(Dispatchers.Main) // Use an appropriate coroutine context

    coroutineScope.launch {
        viewModel.startScanner(
            context,
            Bundle().apply {
                putString("camera_facing", "back") // Ensure back camera is used
            },
            object : IScannerResultProviderListener {
                override fun onSuccess(result: Any?) {
                    if (result is String) {
                        Log.d(TAG, "Scanner result: $result")
                        viewModel.updateInvoiceNo(result)
                    } else {
                        Log.d(TAG, "Scanner failed to return a string result")
                    }
                }

                override fun onFailure(exception: Exception) {
                    Log.e(TAG, "Scanner initialization failed: ${exception.message}")
                }
            }
        )
    }
}