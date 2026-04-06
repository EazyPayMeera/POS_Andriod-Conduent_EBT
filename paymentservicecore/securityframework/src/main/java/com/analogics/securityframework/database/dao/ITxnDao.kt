package com.eazypaytech.securityframework.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.eazypaytech.securityframework.database.entity.BatchEntity
import com.eazypaytech.securityframework.database.entity.TxnEntity

@Dao
interface ITxnDao {

    @Insert
    suspend fun insert(vararg txnEntity: TxnEntity)

    @Update
    suspend fun update(vararg txnEntity: TxnEntity)


    @Query("SELECT * FROM TxnTable WHERE Id = :id")
    suspend fun fetchTxnDetails(id: Long?): TxnEntity?

    @Query("SELECT * FROM TxnTable WHERE Id = :id")
    suspend fun getBatchDetailsTxn(id: Long): BatchEntity?

    @Query("SELECT * FROM TxnTable")
    suspend fun getAllTxnListData(): List<TxnEntity>?

    @Query("SELECT * FROM TxnTable WHERE substr(dateTime, 1, 16) <= :date")
     suspend fun getTransactionDetailsTxnBeforeTime(date: String): List<TxnEntity>

    @Query("SELECT * FROM TxnTable ORDER BY id DESC LIMIT 1")
    suspend fun getLastTxnEntry(): TxnEntity?

    @Query("""SELECT * FROM TxnTable WHERE TxnType NOT IN ('BALANCE_ENQUIRY_CASH', 'BALANCE_ENQUIRY_SNAP','FOODSTAMP_RETURN','CASH_WITHDRAWAL','PURCHASE_CASHBACK')AND TxnStatus = 'APPROVED' ORDER BY id DESC LIMIT 1 """)
    suspend fun getLastTxnEntryByTxnType(): TxnEntity?

    @Query("SELECT * FROM TxnTable WHERE batchId = :batchId ORDER BY id DESC")
    suspend fun fetchTxnListByBatchId(batchId: String): List<TxnEntity>?

    @Query("SELECT TtlAmount FROM TxnTable WHERE InvoiceNo = :invoiceNo")
    suspend fun getTotalAmountByInvoiceNo(invoiceNo: String): String

    @Query("SELECT * FROM TxnTable WHERE InvoiceNo = :invoiceNo")
    suspend fun fetchTrasactionByInvoiceNo(invoiceNo: String): List<TxnEntity>?

    @Query("SELECT * FROM TxnTable WHERE HostTxnRef = :hostTxnRef")
    suspend fun fetchTxnByHostTxnRef(hostTxnRef: String?): TxnEntity?

    @Query("SELECT DateTime FROM TxnTable WHERE InvoiceNo = :invoiceNo")
    suspend fun getTimeDateByInvoiceNo(invoiceNo: String): String

    @Query("SELECT TxnAmount FROM TxnTable WHERE InvoiceNo = :invoiceNo")
    suspend fun getTxnAmountByInvoiceNo(invoiceNo: String): String

    @Query("SELECT Tip FROM TxnTable WHERE InvoiceNo = :invoiceNo")
    suspend fun getTipAmountByInvoiceNo(invoiceNo: String): String

    @Query("SELECT MIN(DateTime) FROM TxnTable WHERE batchId = :batchId")
    suspend fun getStartDateByBatchIds(batchId: String): List<String?>

    @Query("SELECT MAX(DateTime) FROM TxnTable WHERE batchId = :batchId")
    suspend fun getEndDateByBatchIds(batchId: String): List<String?>

    @Query("SELECT * FROM TxnTable WHERE DateTime BETWEEN :startDate AND :endDate")
    suspend fun getTransactionsByDateRange(startDate: String, endDate: String): List<TxnEntity>

    @Query("SELECT InvoiceNo FROM TxnTable WHERE batchId = :batchId ORDER BY CAST(InvoiceNo AS INTEGER) DESC LIMIT 1")
    suspend fun getLastInvoiceNumber(batchId : String?): String?

    @Query("SELECT EXISTS(SELECT 1 FROM TxnTable WHERE InvoiceNo = :invoiceNo)")
    suspend fun isRRLFound(invoiceNo: String): Boolean

}