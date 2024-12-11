package com.analogics.builder_core.model

import com.google.gson.annotations.SerializedName

enum class CardEntryMode {
    @SerializedName("CONTACT")                  CONTACT,
    @SerializedName("CONTACLESS")               CONTACLESS,
    @SerializedName("CONTACLESS_MAGSTRIPE")     CONTACLESS_MAGSTRIPE,
    @SerializedName("MAGSTRIPE")                MAGSTRIPE,
    @SerializedName("FALLBACK_MAGSTRIPE")       FALLBACK_MAGSTRIPE,
    @SerializedName("MANUAL")                   MANUAL,
    @SerializedName("QRCODE")                   QRCODE,
    @SerializedName("UNSPECIFIED")              UNSPECIFIED
}