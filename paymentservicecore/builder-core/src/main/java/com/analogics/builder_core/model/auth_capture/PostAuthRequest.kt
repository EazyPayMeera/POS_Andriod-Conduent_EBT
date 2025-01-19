package com.analogics.builder_core.model.auth_capture

import com.google.gson.annotations.SerializedName

data class PostAuthRequest (
    @SerializedName("MerchantId") 		var merchantId: String? = null,
    @SerializedName("TerminalId") 		var terminalId: String? = null,
    @SerializedName("CashierId")        var cashierId: String? = null,
    @SerializedName("DeviceSN") 		var deviceSN: String? = null,
    @SerializedName("DeviceMake") 		var deviceMake: String? = null,
    @SerializedName("DeviceModel") 		var deviceModel: String? = null,

    /* Transaction Info */
@SerializedName("BatchId")          var batchId: String? = null,
    @SerializedName("DateTime")         var dateTime: String? = null,
    @SerializedName("TimeZone")         var timeZone: String? = null,
    @SerializedName("TxnType")          var txnType: String? = null,
    @SerializedName("TxnCurrencyCode")  var txnCurrencyCode: String? = null,

    /* Original Txn data for Void Refund Capture */
@SerializedName("OriginalTxnType")  var originalTxnType: String? = null,
    @SerializedName("OriginalTxnAmount")    var originalTxnAmount: String? = null,
    @SerializedName("OriginalTip")      var originalTip: String? = null,
    @SerializedName("OriginalCashback") var originalCashback: String? = null,
    @SerializedName("OriginalVat")     var originalVat: String? = null,
    @SerializedName("OriginalSGST")     var originalSGST: String? = null,
    @SerializedName("OriginalTtlAmount")    var originalTtlAmount: String? = null,
    @SerializedName("OriginalTxnRef")   var originalTxnRef: String? = null,
    @SerializedName("OriginalHostTxnRef")   var originalHostTxnRef: String? = null)