package com.eazypaytech.tpaymentcore.model.emv

import com.google.gson.annotations.SerializedName
import com.urovo.i9000s.api.emv.ContantPara

enum class CardBrand(val sdkValue: ContantPara.NfcCardType) {
    @SerializedName("MASTERCARD")       MASTERCARD(ContantPara.NfcCardType.MasterCard),
    @SerializedName("VISA")             VISA(ContantPara.NfcCardType.VisaCard),
    @SerializedName("AMEX")             AMEX(ContantPara.NfcCardType.AmexCard),
    @SerializedName("JCB")              JCB(ContantPara.NfcCardType.JcbCard),
    @SerializedName("DISCOVER")         DISCOVER(ContantPara.NfcCardType.DiscoverCard),
    @SerializedName("PURE")             PURE(ContantPara.NfcCardType.PureCard),
    @SerializedName("RUPAY")            RUPAY(ContantPara.NfcCardType.RupayCard),
    @SerializedName("MIR")              MIR(ContantPara.NfcCardType.MirCard),
    @SerializedName("UPI")              UPI(ContantPara.NfcCardType.UpiCard),
    @SerializedName("ERROR")            ERROR(ContantPara.NfcCardType.ErrorType)
}