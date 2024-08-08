package com.analogics.tpaymentcore.EMV

import android.content.Context
import com.urovo.i9000s.api.emv.ContantPara
import com.urovo.i9000s.api.emv.EmvNfcKernelApi
import java.util.Hashtable

class EMV {
    companion object {
        fun initialize(context: Context) {
            Thread {
                try {
                    val data = Hashtable<String, Any>()
                    data["checkCardMode"] = ContantPara.CheckCardMode.SWIPE_OR_INSERT_OR_TAP //
                    data["currencyCode"] = "682" //682
                    data["emvOption"] = ContantPara.EmvOption.START // START_WITH_FORCE_ONLINE
                    data["amount"] = "0.01"
                    data["cashbackAmount"] = "0"
                    data["checkCardTimeout"] = "30" // Check Card time out .Second
                    data["transactionType"] = "00" //00-goods 01-cash 09-cashback 20-refund
                    data["isEnterAmtAfterReadRecord"] = false
                    data["FallbackSwitch"] = "0" //0- close fallback 1-open fallback
                    data["supportDRL"] = true // support Visa DRL?
                    EmvNfcKernelApi.getInstance().setContext(context)
                    EmvNfcKernelApi.getInstance().startKernel(data)
                    EmvNfcKernelApi.getInstance().getEMVLibVers(ContantPara.CardSlot.PICC)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }
    }
}