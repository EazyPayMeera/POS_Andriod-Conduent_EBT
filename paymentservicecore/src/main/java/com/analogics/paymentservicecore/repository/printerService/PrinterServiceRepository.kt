package com.analogics.tpaymentsapos.rootUtils.genericComposeUI

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.analogics.paymentservicecore.listeners.requestListener.PrinterRequestListener
import com.analogics.paymentservicecore.listeners.responseListener.IPrinterResultProviderListener
import com.analogics.paymentservicecore.model.PaymentServiceTxnDetails
import com.analogics.tpaymentcore.listener.responseListener.IPrinterHandlerListener
import com.analogics.tpaymentcore.repository.PrinterHandler
import javax.inject.Inject

class PrinterServiceRepository @Inject constructor(
    paymentServiceTxnDetails: PaymentServiceTxnDetails?,// Inject the ReceiptBuilder
) : PrinterRequestListener, IPrinterHandlerListener {

    private val TAG = "PrinterServiceRepo"
    /*    val pdetails = paymentServiceTxnDetails()
        Log.d("Object Details","Transaction Details ${pdetails}")*/
    private lateinit var iPrinterResultProviderListener: IPrinterResultProviderListener


    override suspend fun printReceiptDetails(
        format: Bundle,
        barcodeString: String,
        receiptDetails: List<String>,
        descriptionList: List<String>,
        alignment: List<Int>, // Assuming alignment is of type Int; adjust as necessary
        fontsize: List<Int>,
        iPrinterResultProviderListener: IPrinterResultProviderListener
    ) {
        this.iPrinterResultProviderListener = iPrinterResultProviderListener

        // Create a new thread for printing receipt details
        Thread {
            try {
                // Pass the retrieved arguments to the PrinterHandler
                PrinterHandler.addReceiptDetails(format, barcodeString, receiptDetails,descriptionList, alignment,fontsize, this) // Pass this as the listener
                Log.d(TAG, "Receipt printed successfully.")

                iPrinterResultProviderListener.onSuccess(true)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to print receipt: ${e.message}")
                iPrinterResultProviderListener.onSuccess(false)

            }
        }.start() // Start the thread
    }


    override suspend fun printLeftCenterRightDetails(
        Transaction: List<String>,
        Count: List<String>,
        Total: List<String>, // Assuming alignment is of type Int; adjust as necessary
        fontsize: List<Int>,
        iPrinterResultProviderListener: IPrinterResultProviderListener
    ) {
        this.iPrinterResultProviderListener = iPrinterResultProviderListener

        // Create a new thread for printing left, center, right details
        Thread {
            try {
                // Pass the retrieved arguments to the PrinterHandler
                PrinterHandler.addLeftCenterRightDetails(Transaction, Count, Total,fontsize, this) // Pass this as the listener
                Log.d(TAG, "Receipt printed successfully.")
                iPrinterResultProviderListener.onSuccess(true)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to print receipt: ${e.message}")
                iPrinterResultProviderListener.onSuccess(false)
            }
        }.start() // Start the thread
    }

    override suspend fun printLeftRightDetails(
        label: List<String>,
        description: List<String>, // Assuming alignment is of type Int; adjust as necessary
        fontsize: List<Int>,
        iPrinterResultProviderListener: IPrinterResultProviderListener
    ) {
        this.iPrinterResultProviderListener = iPrinterResultProviderListener

        // Create a new thread for printing left, center, right details
        Thread {
            try {
                // Pass the retrieved arguments to the PrinterHandler
                PrinterHandler.addLeftRightDetails(label, description,fontsize, this) // Pass this as the listener
                Log.d(TAG, "Receipt printed successfully.")
                iPrinterResultProviderListener.onSuccess(true)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to print receipt: ${e.message}")
                iPrinterResultProviderListener.onSuccess(false)
            }
        }.start() // Start the thread
    }

    override suspend fun printImage(
        format: Bundle,imageData: ByteArray,
        iPrinterResultProviderListener: IPrinterResultProviderListener
    )
    {
        this.iPrinterResultProviderListener = iPrinterResultProviderListener

        // Create a new thread for printing left, center, right details
        Thread {
            try {
                // Pass the retrieved arguments to the PrinterHandler
                PrinterHandler.addImage(format,imageData, this) // Pass this as the listener
                Log.d(TAG, "Receipt printed successfully.")
                iPrinterResultProviderListener.onSuccess(true)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to print receipt: ${e.message}")
                iPrinterResultProviderListener.onSuccess(false)
            }
        }.start() // Start the thread

    }


    override suspend fun initPrinter(
        context: Context,
        iPrinterResultProviderListener: IPrinterResultProviderListener
    ) {
        Log.d(TAG, "Initializing printer in Payment Service Repository...")
        this.iPrinterResultProviderListener = iPrinterResultProviderListener

        // Create a new thread for printer initialization
        Thread {
            try {
                PrinterHandler.initPrinter(context, this) // Pass this as the listener
                Log.d(TAG, "Printer initialized successfully in Payment Service Repository...")
                // Notify success on the main thread
                // If you need to update UI or callback, use runOnUiThread or Handler
                (context as? Activity)?.runOnUiThread {
                    iPrinterResultProviderListener.onSuccess(true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize printer: ${e.message}")
                // Notify failure on the main thread
                (context as? Activity)?.runOnUiThread {
                    iPrinterResultProviderListener.onSuccess(false)
                }
            }
        }.start() // Start the thread
    }


    override suspend fun getStatus(iPrinterResultProviderListener: IPrinterResultProviderListener) {
        Log.d(TAG, "Get Status in Payment Service Repository...")
        this.iPrinterResultProviderListener = iPrinterResultProviderListener
        try {
            // Assuming PrinterHandler has a method to get status
            val status = PrinterHandler.getStatus(this) // Replace with the actual method to get status
            Log.d(TAG, "Printer status retrieved: $status")
            iPrinterResultProviderListener.onSuccess(status)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get printer status: ${e.message}")
            iPrinterResultProviderListener.onSuccess(false)
        }
    }

    override suspend fun stopPrinting(iPrinterResultProviderListener: IPrinterResultProviderListener) {
        Log.d(TAG, "Get Status in Payment Service Repository...")
        this.iPrinterResultProviderListener = iPrinterResultProviderListener
        try {
            // Assuming PrinterHandler has a method to get status
            val status = PrinterHandler.stopPrinting(this) // Replace with the actual method to get status
            Log.d(TAG, "Printer status retrieved: $status")
            iPrinterResultProviderListener.onSuccess(status)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get printer status: ${e.message}")
            iPrinterResultProviderListener.onSuccess(false)
        }
    }


    override fun onPrinterRespHandler(uiData: String) {
        Log.d(TAG, "Received printer response: $uiData")
        if (uiData == "SUCCESS") {
            Log.d(TAG, "Printer response is SUCCESS.")
            iPrinterResultProviderListener.onSuccess(true)
        } else {
            Log.d(TAG, "Printer response is FAILURE.")
            iPrinterResultProviderListener.onSuccess(false)
        }
    }
}
