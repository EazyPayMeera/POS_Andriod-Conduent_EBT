package com.eazypaytech.posafrica.rootUtils.genericComposeUI

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.eazypaytech.paymentservicecore.listeners.requestListener.PrinterRequestListener
import com.eazypaytech.paymentservicecore.listeners.responseListener.IPrinterResultProviderListener
import com.eazypaytech.paymentservicecore.model.PaymentServiceTxnDetails
import com.eazypaytech.tpaymentcore.listener.responseListener.IPrinterHandlerListener
import com.eazypaytech.tpaymentcore.repository.PrinterHandler
import javax.inject.Inject

class PrinterServiceRepository @Inject constructor(
    paymentServiceTxnDetails: PaymentServiceTxnDetails?,// Inject the ReceiptBuilder
) : PrinterRequestListener, IPrinterHandlerListener {

    private val TAG = "PrinterServiceRepo"
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
        Thread {
            try {
                PrinterHandler.addReceiptDetails(format, barcodeString, receiptDetails,descriptionList, alignment,fontsize, this)

                iPrinterResultProviderListener.onSuccess(true)

            } catch (e: Exception) {
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
        Thread {
            try {
                PrinterHandler.addLeftCenterRightDetails(Transaction, Count, Total,fontsize, this)
                iPrinterResultProviderListener.onSuccess(true)

            } catch (e: Exception) {
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
        Thread {
            try {
                PrinterHandler.addLeftRightDetails(label, description,fontsize, this)
                iPrinterResultProviderListener.onSuccess(true)

            } catch (e: Exception) {
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
        Thread {
            try {
                PrinterHandler.addImage(format,imageData, this)
                iPrinterResultProviderListener.onSuccess(true)

            } catch (e: Exception) {
                iPrinterResultProviderListener.onSuccess(false)
            }
        }.start() // Start the thread

    }


    override suspend fun initPrinter(
        context: Context,
        iPrinterResultProviderListener: IPrinterResultProviderListener
    ) {
        this.iPrinterResultProviderListener = iPrinterResultProviderListener
        Thread {
            try {
                PrinterHandler.initPrinter(context, this)
                (context as? Activity)?.runOnUiThread {
                    iPrinterResultProviderListener.onSuccess(true)
                }
            } catch (e: Exception) {
                (context as? Activity)?.runOnUiThread {
                    iPrinterResultProviderListener.onSuccess(false)
                }
            }
        }.start() // Start the thread
    }


    override suspend fun getStatus(iPrinterResultProviderListener: IPrinterResultProviderListener) {
        this.iPrinterResultProviderListener = iPrinterResultProviderListener
        try {
            val status = PrinterHandler.getStatus(this)
            iPrinterResultProviderListener.onSuccess(status)
        } catch (e: Exception) {
            iPrinterResultProviderListener.onSuccess(false)
        }
    }

    override suspend fun stopPrinting(iPrinterResultProviderListener: IPrinterResultProviderListener) {
        this.iPrinterResultProviderListener = iPrinterResultProviderListener
        try {
            val status = PrinterHandler.stopPrinting(this)
            iPrinterResultProviderListener.onSuccess(status)
        } catch (e: Exception) {
            iPrinterResultProviderListener.onSuccess(false)
        }
    }


    override fun onPrinterRespHandler(uiData: String) {
        if (uiData == "SUCCESS") {
            iPrinterResultProviderListener.onSuccess(true)
        } else {
            iPrinterResultProviderListener.onSuccess(false)
        }
    }
}
