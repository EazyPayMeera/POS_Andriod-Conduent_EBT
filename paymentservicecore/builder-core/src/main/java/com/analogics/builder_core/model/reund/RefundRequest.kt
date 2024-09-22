package com.analogics.builder_core.model.reund

import com.google.gson.annotations.SerializedName

data class RefundRequest(
    @SerializedName("MerchantId") var merchantId: String? = null,
    @SerializedName("TerminalId") var terminalId: String? = null,
    @SerializedName("CashierId") var cashierId: String? = null,
    @SerializedName("DeviceSN") var deviceSN: String? = null,
    @SerializedName("DeviceMake") var deviceMake: String? = null,
    @SerializedName("DeviceModel") var deviceModel: String? = null,

    /* Transaction Info */
    @SerializedName("BatchId") var batchId: String? = null,
    @SerializedName("InvoiceNo") var invoiceNo: String? = null,
    @SerializedName("PurchaseOrderNo") var purchaseOrderNo: String? = null,
    @SerializedName("DateTime") var dateTime: String? = null,
    @SerializedName("TimeZone") var timeZone: String? = null,
    @SerializedName("TxnType") var txnType: String? = null,
    @SerializedName("AccountType") var accountType: String? = null,
    @SerializedName("TxnCurrencyCode") var txnCurrencyCode: String? = null,
    @SerializedName("TxnAmount") var txnAmount: String? = null,

    /* Card Details */
    @SerializedName("CardEntryMode") var cardEntryMode: String? = null,
    @SerializedName("CardMaskedPan") var cardMaskedPan: String? = null,
    @SerializedName("CardBrand") var cardBrand: String? = null,
    @SerializedName("CardAuthMethod") var cardAuthMethod: String? = null,
    @SerializedName("CardAuthResult") var cardAuthResult: String? = null,
    @SerializedName("CardCountryCode") var cardCountryCode: String? = null,
    @SerializedName("CardLanguagePref") var cardLanguagePref: String? = null,
    @SerializedName("EmvData") var emvData: String? = null,

    /* Original Txn data for Void Refund Capture */
    @SerializedName("OriginalTxnRef") var originalTxnRef: String? = null
)