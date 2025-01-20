package com.eazypaytech.posafrica.rootUiScreens.txnList.model

import com.eazypaytech.paymentservicecore.models.TxnType
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TxnDataList(@SerializedName("dateTime") var dateTime: String? = null,
                       @SerializedName("txnType") var txnType:TxnType?=null,
                       @SerializedName("ttlAmount") var ttlAmount: Double? = 0.00,
                       ):Serializable


