package com.eazypaytech.tpaymentcore.model.emv

import com.google.gson.annotations.SerializedName
import com.urovo.i9000s.api.emv.ContantPara

enum class CardCheckMode(val sdkValue: ContantPara.CheckCardMode) {
    @SerializedName("SWIPE")                    SWIPE(ContantPara.CheckCardMode.SWIPE),
    @SerializedName("INSERT")                   INSERT(ContantPara.CheckCardMode.INSERT),
    @SerializedName("TAP")                      TAP(ContantPara.CheckCardMode.TAP),
    @SerializedName("SWIPE_OR_INSERT")          SWIPE_OR_INSERT(ContantPara.CheckCardMode.SWIPE_OR_INSERT),
    @SerializedName("SWIPE_OR_TAP")             SWIPE_OR_TAP(ContantPara.CheckCardMode.SWIPE_OR_TAP),
    @SerializedName("INSERT_OR_TAP")            INSERT_OR_TAP(ContantPara.CheckCardMode.INSERT_OR_TAP),
    @SerializedName("SWIPE_OR_INSERT_OR_TAP")   SWIPE_OR_INSERT_OR_TAP(ContantPara.CheckCardMode.SWIPE_OR_INSERT_OR_TAP)
}