package com.eazypaytech.tpaymentcore.repository

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.eazypaytech.paymentservicecore.constants.EmvConstants
import com.eazypaytech.tpaymentcore.listener.responseListener.IPrinterSdkResponseListener
import com.eazypaytech.tpaymentcore.model.emv.PrinterSdkResult
import com.eazypaytech.tpaymentcore.repository.PrinterSdkRequestRepository.Align
import com.eazypaytech.tpaymentcore.repository.PrinterSdkRequestRepository.FontName
import com.eazypaytech.tpaymentcore.repository.PrinterSdkRequestRepository.FontSize
import com.eazypaytech.tpaymentcore.repository.PrinterSdkRequestRepository.LineSpacing
import com.eazypaytech.tpaymentcore.repository.PrinterSdkRequestRepository.Style
import com.urovo.sdk.print.PrinterProviderImpl
import javax.inject.Inject

class PrinterWrapperRepository @Inject constructor(var iPrinterSdkResponseListener: IPrinterSdkResponseListener)
{
    val TAG : String = "PRINTER_WRAPPER"
    lateinit var context : Context

    fun init(context: Context) : Int
    {
        try {
            /* Initialize Printer First */
            this.context = context
            if(PrinterProviderImpl.getInstance(context).initPrint()!=0) {
                iPrinterSdkResponseListener.onPrinterSdkResponse(
                    PrinterSdkResult.Result(
                        PrinterSdkResult.Status.INIT_FAILURE
                    )
                )
                return -1
            }
        } catch (exception: Exception) {
            iPrinterSdkResponseListener.onPrinterSdkResponse(
                PrinterSdkResult.Result(
                    PrinterSdkResult.Status.INIT_FAILURE
                )
            )
            Log.e(TAG, exception.message.toString())
            return -1
        }

        return 0
    }

    private fun convertToUrovoBundle(format : Int) : Bundle
    {
        return Bundle().apply {
            /* Font Name */
            if(format.and(FontName.SIMSUN.value)==FontName.SIMSUN.value)
                putString(EmvConstants.UROVO_SDK_PRINTER_KEY_FONT_NAME, EmvConstants.UROVO_SDK_PRINTER_VAL_FONT_NAME_SIMSUN)

            /* Font Size */
            if(format.and(FontSize.EXTRA_SMALL.value)==FontSize.EXTRA_SMALL.value)
                putInt(EmvConstants.UROVO_SDK_PRINTER_KEY_FONT_SIZE, EmvConstants.UROVO_SDK_PRINTER_VAL_FONT_SIZE_EXTRA_SMALL)
            else if(format.and(FontSize.SMALL.value)==FontSize.SMALL.value)
                putInt(EmvConstants.UROVO_SDK_PRINTER_KEY_FONT_SIZE, EmvConstants.UROVO_SDK_PRINTER_VAL_FONT_SIZE_SMALL)
            else if(format.and(FontSize.MEDIUM.value)==FontSize.MEDIUM.value)
                putInt(EmvConstants.UROVO_SDK_PRINTER_KEY_FONT_SIZE, EmvConstants.UROVO_SDK_PRINTER_VAL_FONT_SIZE_MEDIUM)
            else if(format.and(FontSize.LARGE.value)==FontSize.LARGE.value)
                putInt(EmvConstants.UROVO_SDK_PRINTER_KEY_FONT_SIZE, EmvConstants.UROVO_SDK_PRINTER_VAL_FONT_SIZE_LARGE)
            else if(format.and(FontSize.EXTRA_LARGE.value)==FontSize.EXTRA_LARGE.value)
                putInt(EmvConstants.UROVO_SDK_PRINTER_KEY_FONT_SIZE, EmvConstants.UROVO_SDK_PRINTER_VAL_FONT_SIZE_EXTRA_LARGE)

            /* Alignment */
            if(format.and(Align.LEFT.value)==Align.LEFT.value)
                putInt(EmvConstants.UROVO_SDK_PRINTER_KEY_ALIGN, EmvConstants.UROVO_SDK_PRINTER_VAL_ALIGN_LEFT)
            else if(format.and(Align.CENTER.value)==Align.CENTER.value)
                putInt(EmvConstants.UROVO_SDK_PRINTER_KEY_ALIGN, EmvConstants.UROVO_SDK_PRINTER_VAL_ALIGN_CENTER)
            else if(format.and(Align.RIGHT.value)==Align.RIGHT.value)
                putInt(EmvConstants.UROVO_SDK_PRINTER_KEY_ALIGN, EmvConstants.UROVO_SDK_PRINTER_VAL_ALIGN_RIGHT)

            /* Bold Font */
            if(format.and(Style.BOLD.value)==Style.BOLD.value)
                putBoolean(EmvConstants.UROVO_SDK_PRINTER_KEY_BOLD_FONT, true)

            /* Line break */
            if(format.and(Style.NO_LINE_BREAK.value)==Style.NO_LINE_BREAK.value)
                putBoolean(EmvConstants.UROVO_SDK_PRINTER_KEY_NEW_LINE, false)

            /* Line Spacing */
            if(format.and(LineSpacing.EXTRA_SMALL.value)==LineSpacing.EXTRA_SMALL.value)
                putInt(EmvConstants.UROVO_SDK_PRINTER_KEY_LINE_HEIGHT, EmvConstants.UROVO_SDK_PRINTER_VAL_LINE_HEIGHT_EXTRA_SMALL)
            else if(format.and(LineSpacing.SMALL.value)==LineSpacing.SMALL.value)
                putInt(EmvConstants.UROVO_SDK_PRINTER_KEY_LINE_HEIGHT, EmvConstants.UROVO_SDK_PRINTER_VAL_LINE_HEIGHT_SMALL)
            else if(format.and(LineSpacing.MEDIUM.value)==LineSpacing.MEDIUM.value)
                putInt(EmvConstants.UROVO_SDK_PRINTER_KEY_LINE_HEIGHT, EmvConstants.UROVO_SDK_PRINTER_VAL_LINE_HEIGHT_MEDIUM)
            else if(format.and(LineSpacing.LARGE.value)==LineSpacing.LARGE.value)
                putInt(EmvConstants.UROVO_SDK_PRINTER_KEY_LINE_HEIGHT, EmvConstants.UROVO_SDK_PRINTER_VAL_LINE_HEIGHT_LARGE)
            else if(format.and(LineSpacing.EXTRA_LARGE.value)==LineSpacing.EXTRA_LARGE.value)
                putInt(EmvConstants.UROVO_SDK_PRINTER_KEY_LINE_HEIGHT, EmvConstants.UROVO_SDK_PRINTER_VAL_LINE_HEIGHT_EXTRA_LARGE)
        }
    }

    fun addText(col1:String?=null, col2:String?=null, col3:String?=null, format : Int) {
        try {
            if(col2!=null && col3!=null)
                PrinterProviderImpl.getInstance(context).addTextLeft_Center_Right(convertToUrovoBundle(format), col1, col2, col3)
            else if(col2!=null)
                PrinterProviderImpl.getInstance(context).addTextLeft_Right(convertToUrovoBundle(format), col1, col2)
            else if(col1!=null)
                PrinterProviderImpl.getInstance(context).addText(convertToUrovoBundle(format), col1)
        } catch (exception: Exception) {
            Log.e(TAG, exception.message.toString())
        }
    }

    fun feedLine(lines : Int?=1) {
        try {
            PrinterProviderImpl.getInstance(context).feedLine(lines?:1)
        } catch (exception: Exception) {
            Log.e(TAG, exception.message.toString())
        }
    }

    fun print()
    {
        try {
            /* Notify that printing has started */
            iPrinterSdkResponseListener.onPrinterSdkResponse(
                PrinterSdkResult.Result(
                    PrinterSdkResult.Status.PRINTING
                )
            )
            /* Start Printing */
            if(PrinterProviderImpl.getInstance(context).startPrint()!=0) {
                iPrinterSdkResponseListener.onPrinterSdkResponse(
                    PrinterSdkResult.Result(
                        PrinterSdkResult.Status.PRINT_FAILURE
                    )
                )
            }
            else
            {
                iPrinterSdkResponseListener.onPrinterSdkResponse(
                    PrinterSdkResult.Result(
                        PrinterSdkResult.Status.PRINT_SUCCESS
                    )
                )
            }
        } catch (exception: Exception) {
            iPrinterSdkResponseListener.onPrinterSdkResponse(
                PrinterSdkResult.Result(
                    PrinterSdkResult.Status.PRINT_FAILURE
                )
            )
            Log.e(TAG, exception.message.toString())
        }
    }
}