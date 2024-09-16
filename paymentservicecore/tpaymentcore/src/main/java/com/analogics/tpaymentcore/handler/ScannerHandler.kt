/*
package com.analogics.tpaymentcore.handler

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.analogics.tpaymentcore.listener.IScannerHandlerListener
import com.analogics.tpaymentcore.listener.ScannerListener
import com.analogics.tpaymentcore.scanner.Scanner

object ScannerHandler : ScannerListener {
    private const val TAG = "ScannerHandler"


    override fun initScanner(
        context: Context,
        scannerHandlerListener: IScannerHandlerListener
    ) {
        try {
            // Initialize the printer
            Scanner.getInstance().initScanner(context)
            Log.d(ScannerHandler.TAG, "Printer initialized successfully.")

            // Notify success
            scannerHandlerListener.onScannerRespHandler("SUCCESS")
        } catch (exception: Exception) {
            Log.e(ScannerHandler.TAG, "Failed to initialize printer: ${exception.message}")

            // Notify failure
            scannerHandlerListener.onScannerRespHandler("FAILURE")
        }
    }

    override fun startScanner(
        data: Bundle,
        onScanned: (qrCode: String) -> Unit,
        onError: (errorCode: Int, message: String) -> Unit,
        onTimeout: () -> Unit,
        onCancel: () -> Unit,
        scannerHandlerListener: IScannerHandlerListener
    ) {
        try {
            // Initialize the printer
            Scanner.getInstance().startScanner(data,1,500L,onScanned,onError,onTimeout,onCancel)
            Log.d(ScannerHandler.TAG, "Scanner initialized successfully.")

            // Notify success
            scannerHandlerListener.onScannerRespHandler("SUCCESS")
        } catch (exception: Exception) {
            Log.e(ScannerHandler.TAG, "Failed to initialize printer: ${exception.message}")

            // Notify failure
            scannerHandlerListener.onScannerRespHandler("FAILURE")
        }
    }


    override fun stopScanner(
        context: Context,
        scannerHandlerListener: IScannerHandlerListener
    ) {
        try {
            // Initialize the printer
            Scanner.getInstance().stopScanner()
            Log.d(ScannerHandler.TAG, "Printer initialized successfully.")

            // Notify success
            scannerHandlerListener.onScannerRespHandler("SUCCESS")
        } catch (exception: Exception) {
            Log.e(ScannerHandler.TAG, "Failed to initialize printer: ${exception.message}")

            // Notify failure
            scannerHandlerListener.onScannerRespHandler("FAILURE")
        }
    }
}*/


package com.analogics.tpaymentcore.handler

import android.content.Context
import android.util.Log
import com.analogics.tpaymentcore.listener.IScannerHandlerListener
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

object ScannerHandler {

    private const val TAG = "ScannerHandler"

    private var barcodeScanner = BarcodeScanning.getClient()

    fun initScanner(
        context: Context,
        scannerHandlerListener: IScannerHandlerListener
    ) {
        try {
            // Initialize the barcode scanner
            barcodeScanner = BarcodeScanning.getClient()
            Log.d(TAG, "Scanner initialized successfully.")

            // Notify success
            scannerHandlerListener.onScannerRespHandler("SUCCESS")
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to initialize scanner: ${exception.message}")

            // Notify failure
            scannerHandlerListener.onScannerRespHandler("FAILURE")
        }
    }

    fun startScanner(
        image: InputImage,
        onScanned: (qrCode: String) -> Unit,
        onError: (errorCode: Int, message: String) -> Unit
    ) {
        try {
            // Start scanning the image with ML Kit
            val result: Task<List<Barcode>> = barcodeScanner.process(image)
            result.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val barcodes = task.result
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue
                        if (rawValue != null) {
                            onScanned(rawValue)
                        }
                    }
                } else {
                    Log.e(TAG, "Failed to process image: ${task.exception?.message}")
                    onError(-1, "Failed to process image: ${task.exception?.message}")
                }
            }
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to start scanner: ${exception.message}")

            // Notify error
            onError(-1, "Failed to start scanner: ${exception.message}")
        }
    }

    fun stopScanner(
        scannerHandlerListener: IScannerHandlerListener
    ) {
        try {
            // Since ML Kit scanner doesn't have a specific stop method, this is a placeholder.
            // Optionally, you could clear resources or reset configurations if needed.
            Log.d(TAG, "Scanner stopped successfully.")

            // Notify success
            scannerHandlerListener.onScannerRespHandler("SUCCESS")
        } catch (exception: Exception) {
            Log.e(TAG, "Failed to stop scanner: ${exception.message}")

            // Notify failure
            scannerHandlerListener.onScannerRespHandler("FAILURE")
        }
    }
}


