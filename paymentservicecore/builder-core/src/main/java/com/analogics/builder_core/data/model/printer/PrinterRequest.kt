package com.analogics.builder_core.data.model.printer

import com.google.gson.annotations.SerializedName

class PrinterRequest (
    @SerializedName("MerchantId") 		var merchantId: String? = "",
    @SerializedName("TerminalId") 		var terminalId: String? = "",
    @SerializedName("CashierId")        var cashierId: String? = "",
    @SerializedName("DeviceSN") 		var deviceSN: String? = "",
    @SerializedName("DeviceMake") 		var deviceMake: String? = "",
    @SerializedName("DeviceModel") 		var deviceModel: String? = "",

    /* Transaction Info */
    @SerializedName("BatchId")          var batchId: String? = "",
    @SerializedName("InvoiceNo")        var invoiceNo: String? = "",
    @SerializedName("PurchaseOrderNo")  var purchaseOrderNo: String? = "",
    @SerializedName("DateTime")         var dateTime: String? = "",
    @SerializedName("TimeZone")         var timeZone: String? = "",
    @SerializedName("TxnType")          var txnType: String? = "",
    @SerializedName("AccountType")      var accountType: String? = "",
    @SerializedName("TxnCurrencyCode")  var txnCurrencyCode: String? = "",
    @SerializedName("TxnAmount")        var txnAmount: String? = "",
    @SerializedName("Tip")              var tip: String? = "",
    @SerializedName("Cashback")         var cashback: String? = "",
    @SerializedName("VAT")              var VAT: String? = "",
    @SerializedName("ServiceCharge")    var serviceCharge: String? = "",
    @SerializedName("TtlAmount")        var ttlAmount: String? = "",

    /* Card Details */
    @SerializedName("CardEntryMode")    var cardEntryMode: String? = "",
    @SerializedName("CardMaskedPan")    var cardMaskedPan: String? = "",
    @SerializedName("CardBrand")        var cardBrand: String? = "",
    @SerializedName("CardAuthMethod")   var cardAuthMethod: String? = "",
    @SerializedName("CardAuthResult")   var cardAuthResult: String? = "",
    @SerializedName("CardCountryCode")  var cardCountryCode: String? = "",
    @SerializedName("CardLanguagePref") var cardLanguagePref: String? = "",
    @SerializedName("EmvData")          var emvData: String? = null,

    /* Original Txn data for Void Refund Capture */
    @SerializedName("OriginalTxnType")  var originalTxnType: String? = "",
    @SerializedName("OriginalTxnAmount")    var originalTxnAmount: String? = "",
    @SerializedName("OriginalTip")      var originalTip: String? = "",
    @SerializedName("OriginalCashback") var originalCashback: String? = "",
    @SerializedName("OriginalVat")     var originalVat: String? = "",
    @SerializedName("OriginalServiceCharge")     var originalServiceCharge: String? = "",
    @SerializedName("OriginalTtlAmount")    var originalTtlAmount: String? = "",
    @SerializedName("OriginalTxnRef")   var originalTxnRef: String? = "" )
