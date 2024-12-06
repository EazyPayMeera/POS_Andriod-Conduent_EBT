package com.analogics.paymentservicecore.model.emv

import com.google.gson.annotations.SerializedName

enum class CardEntryMode {
    @SerializedName("CONTACT")                  CONTACT,
    @SerializedName("CONTACLESS")               CONTACLESS,
    @SerializedName("CONTACLESS_MAGSTRIPE")     CONTACLESS_MAGSTRIPE,
    @SerializedName("MAGSTRIPE")                MAGSTRIPE,
    @SerializedName("MANUAL")                   MANUAL,
    @SerializedName("UNKNOWN")                  UNKNOWN
}