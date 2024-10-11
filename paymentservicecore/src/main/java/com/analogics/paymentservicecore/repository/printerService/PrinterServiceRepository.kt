package com.analogics.tpaymentsapos.rootUtils.genericComposeUI

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.listeners.requestListener.PrinterRequestListener
import com.analogics.paymentservicecore.listeners.responseListener.IPrinterResultProviderListener
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
        alignment: List<Int>, // Assuming alignment is of type Int; adjust as necessary
        iPrinterResultProviderListener: IPrinterResultProviderListener
    ) {
        this.iPrinterResultProviderListener = iPrinterResultProviderListener
        try {
            // Pass the retrieved arguments to the PrinterHandler
            PrinterHandler.addReceiptDetails(format, barcodeString, receiptDetails, alignment, this) // Pass this as the listener
            Log.d(TAG, "Receipt printed successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to print receipt: ${e.message}")
            iPrinterResultProviderListener.onSuccess(false)
        }
    }

    override suspend fun printLeftCenterRightDetails(
        Transaction: List<String>,
        Count: List<String>,
        Total: List<String>, // Assuming alignment is of type Int; adjust as necessary
        iPrinterResultProviderListener: IPrinterResultProviderListener
    ) {
        this.iPrinterResultProviderListener = iPrinterResultProviderListener
        try {
            // Pass the retrieved arguments to the PrinterHandler
            PrinterHandler.addLeftCenterRightDetails(Transaction,Count,Total,this) // Pass this as the listener
            Log.d(TAG, "Receipt printed successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to print receipt: ${e.message}")
            iPrinterResultProviderListener.onSuccess(false)
        }
    }

    override suspend fun initPrinter(
        context: Context,
        iPrinterResultProviderListener: IPrinterResultProviderListener
    ) {
        Log.d(TAG, "Initializing printer in Payment Service Repository...")
        this.iPrinterResultProviderListener = iPrinterResultProviderListener
        try {
            PrinterHandler.initPrinter(context, this) // Pass this as the listener
            Log.d(TAG, "Printer initialized successfully in Payment Service Repository...")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize printer: ${e.message}")
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
