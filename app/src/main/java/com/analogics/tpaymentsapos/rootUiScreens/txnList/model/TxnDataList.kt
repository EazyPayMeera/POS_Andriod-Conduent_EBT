package com.analogics.tpaymentsapos.rootUiScreens.txnList.model

import com.analogics.paymentservicecore.models.TxnType
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TxnDataList(@SerializedName("dateTime") var dateTime: String? = null,
                       @SerializedName("txnType") var txnType:TxnType?=null,
                       @SerializedName("ttlAmount") var ttlAmount: Double? = 0.00,
                       ):Serializable


