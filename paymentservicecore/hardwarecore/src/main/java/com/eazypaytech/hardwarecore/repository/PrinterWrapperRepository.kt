package com.eazypaytech.tpaymentcore.repository

import android.content.Context
import android.device.PrinterManager
import android.device.SEManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IInputActionListener
import android.text.TextUtils
import android.util.Log
import com.eazypaytech.paymentservicecore.constants.EmvConstants
import com.eazypaytech.tpaymentcore.constants.EncryptionConstants
import com.eazypaytech.tpaymentcore.listener.requestListener.IEmvWrapperRequestListener
import com.eazypaytech.tpaymentcore.listener.responseListener.IEmvSdkResponseListener
import com.eazypaytech.tpaymentcore.listener.responseListener.IPrinterSdkResponseListener
import com.eazypaytech.tpaymentcore.model.emv.AidConfig
import com.eazypaytech.tpaymentcore.model.emv.CAPKey
import com.eazypaytech.tpaymentcore.model.emv.CardCheckMode
import com.eazypaytech.tpaymentcore.model.emv.EmvSdkException
import com.eazypaytech.tpaymentcore.model.emv.EmvSdkResult
import com.eazypaytech.tpaymentcore.model.emv.EmvSdkResult.DisplayMsgId
import com.eazypaytech.tpaymentcore.model.emv.EmvSdkResult.InitResult
import com.eazypaytech.tpaymentcore.model.emv.EmvSdkResult.InitStatus
import com.eazypaytech.tpaymentcore.model.emv.EmvSdkResult.TransStatus
import com.eazypaytech.tpaymentcore.model.emv.PrinterSdkResult
import com.eazypaytech.tpaymentcore.model.emv.TransConfig
import com.eazypaytech.tpaymentcore.utils.TlvUtils
import com.urovo.i9000s.api.emv.ContantPara
import com.urovo.i9000s.api.emv.ContantPara.CardSlot
import com.urovo.i9000s.api.emv.EmvListener
import com.urovo.i9000s.api.emv.EmvNfcKernelApi
import com.urovo.i9000s.api.emv.Funs
import com.urovo.sdk.pinpad.PinPadProviderImpl
import com.urovo.sdk.pinpad.listener.PinInputListener
import com.urovo.sdk.print.PrinterProviderImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Hashtable
import java.util.Locale
import javax.inject.Inject
import kotlin.collections.contentToString
import kotlin.collections.set
import kotlin.text.decodeToString
import kotlin.text.substring
import kotlin.text.toInt
import kotlin.text.uppercase
import kotlin.toString

class PrinterWrapperRepository @Inject constructor(var iPrinterSdkResponseListener: IPrinterSdkResponseListener)
{
    fun initPrinter(context: Context)
    {
        try {
            when (PrinterProviderImpl.getInstance(context).initPrint()) {
                0 -> iPrinterSdkResponseListener.onPrinterSdkResponse(
                    PrinterSdkResult.InitResult(
                        PrinterSdkResult.InitStatus.SUCCESS
                    )
                )
                else ->
                    iPrinterSdkResponseListener.onPrinterSdkResponse(
                        PrinterSdkResult.InitResult(
                            PrinterSdkResult.InitStatus.FAILURE
                        )
                    )
            }
        } catch (exception: Exception) {
            iPrinterSdkResponseListener.onPrinterSdkResponse(PrinterSdkResult.InitResult(
                PrinterSdkResult.InitStatus.FAILURE))
        }
    }
}