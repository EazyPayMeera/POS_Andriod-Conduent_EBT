package com.eazypaytech.paymentservicecore.model.emv

import com.google.gson.annotations.SerializedName

enum class CardCheckMode {
    @SerializedName("SWIPE")                    SWIPE,
    @SerializedName("INSERT")                   INSERT,
    @SerializedName("TAP")                      TAP,
    @SerializedName("SWIPE_OR_INSERT")          SWIPE_OR_INSERT,
    @SerializedName("SWIPE_OR_TAP")             SWIPE_OR_TAP,
    @SerializedName("INSERT_OR_TAP")            INSERT_OR_TAP,
    @SerializedName("SWIPE_OR_INSERT_OR_TAP")   SWIPE_OR_INSERT_OR_TAP
}