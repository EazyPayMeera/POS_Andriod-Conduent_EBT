package com.eazypaytech.securityframework.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(tableName = "TxnTable")
data class TxnEntity(
    @PrimaryKey(autoGenerate = false) var id: Long? = null,

    /* Merchant & Cashier Info */
    @ColumnInfo(name = "MerchantId") var merchantId: String? = null,
    @ColumnInfo(name = "TerminalId") var terminalId: String? = null,
    @ColumnInfo(name = "CashierId") var cashierId: String? = null,
    @ColumnInfo(name = "LoginId") var loginId: String? = null,
    @ColumnInfo(name = "DeviceSN") var deviceSN: String? = null,

    /* Transaction Info */
    @ColumnInfo(name = "BatchId") var batchId: String? = null,
    @ColumnInfo(name = "InvoiceNo") var invoiceNo: String? = null,
    @ColumnInfo(name = "Stan") var stan: String? = null,
    @ColumnInfo(name = "originalData") var originalData: String? = null,
    @ColumnInfo(name = "originalDateTime") var originalDateTime: String? = null,
    @ColumnInfo(name = "rrn") var rrn: String? = null,
    @ColumnInfo(name = "PurchaseOrderNo") var purchaseOrderNo: String? = null,
    @ColumnInfo(name = "DateTime") var dateTime: String? = null,
    @ColumnInfo(name = "localTime") var localTime: String? = null,
    @ColumnInfo(name = "localDate") var localDate: String? = null,
    @ColumnInfo(name = "currencyCode") var currencyCode: String? = null,
    @ColumnInfo(name = "processingCode") var processingCode: String? = null,
    @ColumnInfo(name = "expiryDate") var expiryDate: String? = null,
    @ColumnInfo(name = "settlementDate") var settlementDate: String? = null,
    @ColumnInfo(name = "TimeZone") var timeZone: String? = null,
    @ColumnInfo(name = "TxnType") var txnType: String? = null,
    @ColumnInfo(name = "AccountType") var accountType: String? = null,
    @ColumnInfo(name = "TxnCurrencyCode") var txnCurrencyCode: String? = null,
    @ColumnInfo(name = "TxnAmount") var txnAmount: String? = null,
    @ColumnInfo(name = "Tip") var tip: String? = null,
    @ColumnInfo(name = "Cashback") var cashback: String? = null,
    @ColumnInfo(name = "posEntryMode") var posEntryMode: String? = null,
    @ColumnInfo(name = "VAT") var VAT: String? = null,
    @ColumnInfo(name = "ServiceCharge") var serviceCharge: String? = null,
    @ColumnInfo(name = "TtlAmount") var ttlAmount: String? = null,
    @ColumnInfo(name = "TxnStatus") var txnStatus: String? = null,

    /* Card Details */
    @ColumnInfo(name = "CardEntryMode") var cardEntryMode: String? = null,
    @ColumnInfo(name = "CardPan") var cardPan: String? = null,
    @ColumnInfo(name = "CardMaskedPan") var cardMaskedPan: String? = null,
    @ColumnInfo(name = "CardBrand") var cardBrand: String? = null,
    @ColumnInfo(name = "CardAuthMethod") var cardAuthMethod: String? = null,
    @ColumnInfo(name = "CardAuthResult") var cardAuthResult: String? = null,
    @ColumnInfo(name = "CardCountryCode") var cardCountryCode: String? = null,
    @ColumnInfo(name = "CardLanguagePref") var cardLanguagePref: String? = null,
    @ColumnInfo(name = "EmvData") var emvData: String? = null,
    @ColumnInfo(name = "PosConditionCode") var posConditionCode: String? = null,
    @ColumnInfo(name = "KSN") var ksn: String? = null,
    @ColumnInfo(name = "ReceiptEmvData") var receiptEmvData: String? = null,
    @ColumnInfo(name = "SignatureData") var signatureData: String? = null,

    /* Host Response */
    @ColumnInfo(name = "HostAuthCode") var hostAuthCode: String? = null,
    @ColumnInfo(name = "HostRespCode") var hostRespCode: String? = null,
    @ColumnInfo(name = "HostAuthResult") var hostAuthResult: String? = null,
    @ColumnInfo(name = "HostTxnRef") var hostTxnRef: String? = null,

    /* Original Txn data for Void, Refund, Capture */
    @ColumnInfo(name = "OriginalTxnAmount") var originalTxnAmount: String? = null,
    @ColumnInfo(name = "OriginalTxnType") var originalTxnType: String? = null,
    @ColumnInfo(name = "OriginalTip") var originalTip: String? = null,
    @ColumnInfo(name = "OriginalCashback") var originalCashback: String? = null,
    @ColumnInfo(name = "OriginalVat") var originalVat: String? = null,
    @ColumnInfo(name = "OriginalServiceCharge") var originalServiceCharge: String? = null,
    @ColumnInfo(name = "OriginalTtlAmount") var originalTtlAmount: String? = null,
    @ColumnInfo(name = "OriginalTxnRef") var originalTxnRef: String? = null,

    /* Other flags */
    @ColumnInfo(name = "IsFallback") var isFallback: Boolean? = false,
    @ColumnInfo(name = "IsCaptured") var isCaptured: Boolean? = false,
    @ColumnInfo(name = "IsVoided") var isVoided: Boolean? = false,
    @ColumnInfo(name = "IsRefunded") var isRefunded: Boolean? = false,
    @ColumnInfo(name = "IsDemoMode") var isDemoMode: Boolean? = false,

    /* Receipt Specific Config */
    @ColumnInfo(name = "Header1") var header1: String? = null,
    @ColumnInfo(name = "Header2") var header2: String? = null,
    @ColumnInfo(name = "Header3") var header3: String? = null,
    @ColumnInfo(name = "Header4") var header4: String? = null,
    @ColumnInfo(name = "Footer1") var footer1: String? = null,
    @ColumnInfo(name = "Footer2") var footer2: String? = null,
    @ColumnInfo(name = "Footer3") var footer3: String? = null,
    @ColumnInfo(name = "Footer4") var footer4: String? = null
    ) : Serializable