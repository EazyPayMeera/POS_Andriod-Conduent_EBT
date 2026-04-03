package com.analogics.builder_core.data.model.purchase

import com.google.gson.annotations.SerializedName

data class PurchaseRequest (
    @SerializedName("MerchantId") 		var merchantId: String? = "123456",
    @SerializedName("TerminalId") 		var terminalId: String? = "1111111",
    @SerializedName("CashierId")        var cashierId: String? = "12344555",
    @SerializedName("DeviceSN") 		var deviceSN: String? = "12455666",
    @SerializedName("DeviceMake") 		var deviceMake: String? = "12455666",
    @SerializedName("DeviceModel") 		var deviceModel: String? = "12455666",

    /* Transaction Info */
@SerializedName("BatchId")          var batchId: String? = "12455666",
    @SerializedName("InvoiceNo")        var invoiceNo: String? = "12455666",
    @SerializedName("PurchaseOrderNo")  var purchaseOrderNo: String? = "12455666",
    @SerializedName("DateTime")         var dateTime: String? = "12455666",
    @SerializedName("TimeZone")         var timeZone: String? = "12455666",
    @SerializedName("TxnType")          var txnType: String? = "12455666",
    @SerializedName("AccountType")      var accountType: String? = "12455666",
    @SerializedName("TxnCurrencyCode")  var txnCurrencyCode: String? = "12455666",
    @SerializedName("TxnAmount")        var txnAmount: String? = "12455666",
    @SerializedName("Tip")              var tip: String? = "12455666",
    @SerializedName("Cashback")         var cashback: String? = "12455666",
    @SerializedName("VAT")              var VAT: String? = "12455666",
    @SerializedName("ServiceCharge")    var serviceCharge: String? = "12455666",
    @SerializedName("TtlAmount")        var ttlAmount: String? = "12455666",

    /* Card Details */
@SerializedName("CardEntryMode")    var cardEntryMode: String? = "12455666",
    @SerializedName("CardMaskedPan")    var cardMaskedPan: String? = "12455666",
    @SerializedName("CardBrand")        var cardBrand: String? = "12455666",
    @SerializedName("CardAuthMethod")   var cardAuthMethod: String? = "12455666",
    @SerializedName("CardAuthResult")   var cardAuthResult: String? = "12455666",
    @SerializedName("CardCountryCode")  var cardCountryCode: String? = "12455666",
    @SerializedName("CardLanguagePref") var cardLanguagePref: String? = "12455666",
    @SerializedName("EmvData")          var emvData: String? = null,

    /* Original Txn data for Void Refund Capture */
    @SerializedName("OriginalTxnType")  var originalTxnType: String? = "12455666",
    @SerializedName("OriginalTxnAmount")    var originalTxnAmount: String? = "12455666",
    @SerializedName("OriginalTip")      var originalTip: String? = "12455666",
    @SerializedName("OriginalCashback") var originalCashback: String? = "12455666",
    @SerializedName("OriginalVat")     var originalVat: String? = "12455666",
    @SerializedName("OriginalServiceCharge")     var originalServiceCharge: String? = "12455666",
    @SerializedName("OriginalTtlAmount")    var originalTtlAmount: String? = "12455666",
    @SerializedName("OriginalTxnRef")   var originalTxnRef: String? = "12455666" )