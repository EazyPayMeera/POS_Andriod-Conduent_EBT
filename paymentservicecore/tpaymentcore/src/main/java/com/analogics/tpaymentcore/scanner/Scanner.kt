/*
package com.analogics.tpaymentcore.scanner

import android.content.Context
import android.device.ScanManager
import android.os.Bundle
import com.urovo.file.logfile
import com.urovo.i9000s.api.emv.Funs.context
import com.urovo.sdk.scanner.InnerScannerImpl
import com.urovo.sdk.scanner.listener.ScannerListener




class Scanner constructor() {

    companion object {
        @Volatile
        private var INSTANCE: Scanner? = null

        fun getInstance(): Scanner =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Scanner().also { INSTANCE = it }
            }
    }


    private var mScanner: InnerScannerImpl? = null
    private var mScannerMan:ScanManager? = null

    // To Initialize the Printer
    fun initScanner(context: Context) {
        // Log initialization attempt
        logfile.printLog("===initScanner in Scanner.kt")

        // Check if mScanner is null, then initialize it using ScannerProviderImpl
        if (this.mScanner == null) {
            // Ensure ScannerProviderImpl.getInstance returns a ScannerManager or compatible type
            this.mScanner = InnerScannerImpl.getInstance(context)
        }

    }

    fun startScanner(
        data: Bundle,
        cameraId: Int = 1,
        timeout: Long = 10000L, // default timeout set to 5 seconds
        onScanned: (qrCode: String) -> Unit,
        onError: (errorCode: Int, message: String) -> Unit,
        onTimeout: () -> Unit,
        onCancel: () -> Unit
    ) {
        val listener = object : ScannerListener {
            override fun onSuccess(qrCode: String) {
                onScanned(qrCode)
            }

            override fun onError(errorCode: Int, message: String) {
                onError(errorCode, message)
            }

            override fun onTimeout() {
                onTimeout()
            }

            override fun onCancel() {
                onCancel()
            }
        }

        try {
            // Call the startScan method from InnerScannerImpl
            mScanner?.startScan(context, data, cameraId, timeout, listener)
        } catch (e: Exception) {
            e.printStackTrace()
            onError(-1, "Failed to start scanner: ${e.message}")
        }
    }

    // Function to stop the scanner
    fun stopScanner() {
        try {
            // Safely call stopScan if scannerImpl is not null
            mScanner?.stopScan()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}




*/




package com.analogics.tpaymentcore.scanner

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Scanner {

    companion object {
        @Volatile
        private var INSTANCE: Scanner? = null

        fun getInstance(): Scanner =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Scanner().also { INSTANCE = it }
            }
    }

    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    // Initialize the Scanner
    fun initScanner(context: Context) {
        Log.d("Scanner", "Initializing ML Kit Barcode Scanner")
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    // Start the scanner with given context, onScanned, and onError handlers
    fun startScanner(
        context: Context,
        onScanned: (barcodeDetails: String) -> Unit,
        onError: (errorCode: Int, message: String) -> Unit
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Set up the camera preview
            val preview = Preview.Builder().build()

            // Configure the ImageAnalysis use case for the scanner
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(android.util.Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            // Set analyzer to process each frame
            imageAnalysis.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { imageProxy ->
                processImageProxy(context, imageProxy, onScanned, onError)
            })

            try {
                // Select back camera as default
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // Unbind all previously bound use cases and bind the new ones
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    context as androidx.lifecycle.LifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

            } catch (e: Exception) {
                Log.e("Scanner", "Failed to bind camera use cases", e)
                onError(-1, "Failed to start scanner")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(
        context: Context,
        imageProxy: ImageProxy,
        onScanned: (barcodeDetails: String) -> Unit,
        onError: (errorCode: Int, message: String) -> Unit
    ) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            // Configure scanner to scan QR and Aztec codes
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_AZTEC
                )
                .build()

            // Perform barcode scanning
            BarcodeScanning.getClient(options)
                .process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val barcodeDetails = when (barcode.valueType) {
                            Barcode.TYPE_WIFI -> {
                                val ssid = barcode.wifi?.ssid ?: "N/A"
                                val password = barcode.wifi?.password ?: "N/A"
                                val type = barcode.wifi?.encryptionType ?: "N/A"
                                "WIFI - SSID: $ssid, Password: $password, Type: $type"
                            }
                            Barcode.TYPE_URL -> {
                                val title = barcode.url?.title ?: "N/A"
                                val url = barcode.url?.url ?: "N/A"
                                "URL - Title: $title, URL: $url"
                            }
                            else -> barcode.rawValue ?: "No details"
                        }
                        onScanned(barcodeDetails)
                        return@addOnSuccessListener
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Scanner", "Failed to process image", e)
                    onError(-1, "Failed to process image: ${e.message}")
                }
                .addOnCompleteListener {
                    imageProxy.close() // Always close image proxy after processing
                }
        } else {
            imageProxy.close() // Close if no mediaImage is available
        }
    }

    // Stop the scanner and release resources
    fun stopScanner() {
        try {
            cameraExecutor.shutdown()
            Log.d("Scanner", "ML Kit Barcode Scanner stopped.")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


