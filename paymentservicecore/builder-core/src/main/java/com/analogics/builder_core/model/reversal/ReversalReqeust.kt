package com.analogics.builder_core.model.void

import com.google.gson.annotations.SerializedName

data class ReversalReqeust (
    @SerializedName("MerchantId") 		var merchantId: String? = null,
    @SerializedName("TerminalId") 		var terminalId: String? = null,
@SerializedName("CashierId")        var cashierId: String? = null ,
@SerializedName("DeviceSN") 		var deviceSN: String? = null ,
@SerializedName("DeviceMake") 		var deviceMake: String? = null ,
@SerializedName("DeviceModel") 		var deviceModel: String? = null ,

/* Transaction Info */
@SerializedName("BatchId")          var batchId: String? = null ,
@SerializedName("InvoiceNo")        var invoiceNo: String? = null ,
@SerializedName("PurchaseOrderNo")  var purchaseOrderNo: String? = null ,
@SerializedName("DateTime")         var dateTime: String? = null ,
@SerializedName("TimeZone")         var timeZone: String? = null ,
@SerializedName("TxnType")          var txnType: String? = null ,
@SerializedName("AccountType")      var accountType: String? = null ,
@SerializedName("TxnCurrencyCode")  var txnCurrencyCode: String? = null ,
@SerializedName("TxnAmount")        var txnAmount: String? = null ,
@SerializedName("Tip")              var tip: String? = null ,
@SerializedName("Cashback")         var cashback: String? = null ,
@SerializedName("CGST")             var CGST: String? = null ,
@SerializedName("SGST")             var SGST: String? = null ,
@SerializedName("TtlAmount")        var ttlAmount: String? = null ,

/* Card Details */
@SerializedName("CardEntryMode")    var cardEntryMode: String? = null ,
@SerializedName("CardMaskedPan")    var cardMaskedPan: String? = null ,
@SerializedName("CardBrand")        var cardBrand: String? = null ,
@SerializedName("CardAuthMethod")   var cardAuthMethod: String? = null ,
@SerializedName("CardAuthResult")   var cardAuthResult: String? = null ,
@SerializedName("CardCountryCode")  var cardCountryCode: String? = null ,
@SerializedName("CardLanguagePref") var cardLanguagePref: String? = null ,
@SerializedName("EmvData")          var emvData: String? = null ,

/* Original Txn data for Void Refund Capture */
@SerializedName("OriginalTxnType")  var originalTxnType: String? = null ,
@SerializedName("OriginalTxnAmount")    var originalTxnAmount: String? = null ,
@SerializedName("OriginalTip")      var originalTip: String? = null ,
@SerializedName("OriginalCashback") var originalCashback: String? = null ,
@SerializedName("OriginalCGST")     var originalCGST: String? = null ,
@SerializedName("OriginalSGST")     var originalSGST: String? = null ,
@SerializedName("OriginalTtlAmount")    var originalTtlAmount: String? = null ,
@SerializedName("OriginalTxnRef")   var originalTxnRef: String? = null)