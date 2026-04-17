package com.analogics.securityframework.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(tableName = "BatchTable")
data class BatchEntity(
    @PrimaryKey(autoGenerate = true) var id: Long? = null,

    /* Batch Info */
    @ColumnInfo(name = "MerchantId") var merchantId: String? = null,
    @ColumnInfo(name = "TerminalId") var terminalId: String? = null,
    @ColumnInfo(name = "CashierId") var cashierId: String? = null,
    @ColumnInfo(name = "BatchId") var batchId: String? = null,
    @ColumnInfo(name = "BatchStatus") var batchStatus: String? = null,
    @ColumnInfo(name = "OpenedDateTime") var openedDateTime: String? = null,
    @ColumnInfo(name = "ClosedDateTime") var closedDateTime: String? = null,
    @ColumnInfo(name = "TimeZone") var timeZone: String? = null,
    @ColumnInfo(name = "IsDemoMode") var isDemoMode: Boolean? = false
    ) : Serializable