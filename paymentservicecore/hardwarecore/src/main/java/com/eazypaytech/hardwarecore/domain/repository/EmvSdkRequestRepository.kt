package com.eazypaytech.hardwarecore.domain.repository

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.eazypaytech.hardwarecore.domain.listener.requestListener.IEmvSdkRequestListener
import com.eazypaytech.hardwarecore.domain.listener.responseListener.IEmvSdkResponseListener
import com.eazypaytech.hardwarecore.data.model.AidConfig
import com.eazypaytech.hardwarecore.data.model.CAPKey
import com.eazypaytech.hardwarecore.data.model.EmvSdkException
import com.eazypaytech.hardwarecore.data.model.EmvSdkResult
import com.eazypaytech.hardwarecore.data.model.TransConfig
import com.eazypaytech.tpaymentcore.repository.EmvWrapperRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.toString

class EmvSdkRequestRepository @Inject constructor(@ApplicationContext context: Context, override var iEmvSdkResponseListener: IEmvSdkResponseListener) : IEmvSdkRequestListener {
    private var emvWrapper = EmvWrapperRepository(context, iEmvSdkResponseListener)

    override fun initPaymentSDK(
        aidConfig: AidConfig?,
        capKeys: List<CAPKey>?
    ) {
        try {
            emvWrapper.initializeSdk(aidConfig, capKeys)
        } catch (exception: Exception) {
            iEmvSdkResponseListener.onEmvSdkResponse(EmvSdkException(exception.message.toString()))
        }
    }

    override fun startPayment(
        context: Context,
        transConfig: TransConfig?,
        isTap: Boolean?,
        isChip: Boolean?
    ) {
        try {
            emvWrapper.startPayment(context, transConfig,isTap,isChip, iEmvSdkResponseListener);
        } catch (exception: Exception) {
            iEmvSdkResponseListener.onEmvSdkResponse(EmvSdkException(exception.message.toString()))
        }
    }

    override fun pinGeneration(
        pan: String?,
        amount: String,
        nResult: (pinBlock: ByteArray?) -> Unit
    ) {
        try {
            emvWrapper.inputManualPin(pan, amount,nResult);
        } catch (exception: Exception) {
            iEmvSdkResponseListener.onEmvSdkResponse(EmvSdkException(exception.message.toString()))
        }
    }

    override fun isCardExists(context: Context): Boolean {
        return runBlocking {
            try {
                emvWrapper.isCardExists(context)
            } catch (e: Exception) {
                Log.e("MOREFUN", "Error checking card: ${e.message}")
                false
            }
        }
    }

    override fun isCardDetected(context: Context): EmvSdkResult.CardCheckStatus {
        return runBlocking {
            try {
                emvWrapper.detectCard(context)
            } catch (e: Exception) {
                Log.e("MOREFUN", "Error checking card: ${e.message}")
                EmvSdkResult.CardCheckStatus.NO_CARD_DETECTED
            }
        }
    }


    override fun startLogCapture(context: Context): Boolean {
        return runBlocking {
            try {
                emvWrapper.startLogCapture(context)
            } catch (e: Exception) {
                Log.e("MOREFUN", "Error checking card: ${e.message}")
                false
            }
        }
    }

    override fun stopLogCapture(context: Context): Boolean {
        return runBlocking {
            try {
                emvWrapper.startLogCapture(context)
            } catch (e: Exception) {
                Log.e("MOREFUN", "Error checking card: ${e.message}")
                false
            }
        }
    }

    override fun abortPayment() {
        try {
            EmvWrapperRepository.Companion.abortPayment()
        } catch (exception: Exception) {
            iEmvSdkResponseListener.onEmvSdkResponse(EmvSdkException(exception.message.toString()))
        }
    }

    override fun getEmvTag(tag: String?): String? {
        return try {
            EmvWrapperRepository.Companion.getEmvTag(tag)
        }catch (exception: Exception)
        {
            exception.printStackTrace()
            null
        }
    }

    fun getEmvDataSafe(
        tagList: Array<String>,
        bundle: Bundle?
    ): ByteArray? {

        val outBuffer = ByteArray(2048)

        return try {

            val resultCode = emvWrapper.getEmvDataSafe(
                tagList,
                bundle
            ) ?: return null

            Log.d("EMV_READ", "Result code = $resultCode")
            Log.d("EMV_READ", "Tags requested = ${tagList.joinToString()}")

            // IMPORTANT: copy only valid bytes
            val cleanData = outBuffer.takeWhile { it.toInt() != 0 }.toByteArray()

            Log.d("EMV_READ", "Output size = ${cleanData.size}")

            cleanData

        } catch (e: Exception) {
            Log.e("EMV_READ", "Failed to read EMV data", e)
            null
        }
    }
}