package com.eazypaytech.paymentservicecore.model.emv

import com.google.gson.annotations.SerializedName

enum class PosConditionCode {
    @SerializedName("NORMAL_PRESENTMENT")       NORMAL_PRESENTMENT,
    @SerializedName("CUSTOMER_NOT_PRESENT")     CUSTOMER_NOT_PRESENT,
    @SerializedName("CARD_NOT_PRESENT")         CARD_NOT_PRESENT,
    @SerializedName("CARD_PRESENT_BAD_MAG")     CARD_PRESENT_BAD_MAG,
    @SerializedName("MERCHANT_SUSPICIOUS")      MERCHANT_SUSPICIOUS,
    @SerializedName("ECR_INTERFACE")            ECR_INTERFACE,
    @SerializedName("PREAUTH")                  PREAUTH,
    @SerializedName("MOTO")                     MOTO,
    @SerializedName("UNSPECIFIED")              UNSPECIFIED
}