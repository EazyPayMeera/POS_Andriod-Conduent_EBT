package com.analogics.builder_core.model.rkl

import com.google.gson.annotations.SerializedName

data class RklRequest(
    @SerializedName("processingCode") var processingCode: Int,      /* Field 03,    N6,         Mandatory */
    @SerializedName("stan") var stan: Int,                          /* Field 11,    N6,         Mandatory */
    @SerializedName("time") var time: Int,                          /* Field 11,    N6,         Mandatory */
    @SerializedName("date") var date: Int,                          /* Field 12,    N4,         Mandatory */
    @SerializedName("nii")  var nii:  Int,                          /* Field 24,    N3,         Mandatory */
    @SerializedName("terminalId")  var terminalId:  String,         /* Field 41,    ANS8,       Mandatory */
    @SerializedName("merchantId")  var merchantId:  String,         /* Field 42,    ANS15,      Mandatory */
    @SerializedName("serialNumber")  var serialNumber:  String,     /* Field 60,    ANS...999,  Mandatory */
    @SerializedName("workingKey")  var workingKey:  String          /* Field 62,    ANS...999,  Mandatory */
)

data class RklResponse(
    @SerializedName("processingCode") var processingCode: Int,      /* Field 03,    N6,         Mandatory */
    @SerializedName("stan") var stan: Int,                          /* Field 11,    N6,         Mandatory */
    @SerializedName("time") var time: Int,                          /* Field 11,    N6,         Mandatory */
    @SerializedName("date") var date: Int,                          /* Field 12,    N4,         Mandatory */
    @SerializedName("nii")  var nii:  Int,                          /* Field 24,    N3,         Mandatory */
    @SerializedName("responseCode")  var responseCode:  String,     /* Field 39,    AN2,        Mandatory */
    @SerializedName("terminalId")  var terminalId:  String,         /* Field 41,    ANS8,       Mandatory */
    @SerializedName("merchantId")  var merchantId:  String?,        /* Field 42,    ANS15,      Optional */
    @SerializedName("addlData")  var addlData:  String,             /* Field 48,    ANS...999,  Mandatory */
    @SerializedName("workingKey")  var workingKey:  String          /* Field 62,    ANS...999,  Mandatory */
)
