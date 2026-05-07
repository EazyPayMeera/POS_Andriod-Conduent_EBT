package com.analogics.securityframework.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.analogics.securityframework.database.entity.BatchEntity
import com.analogics.securityframework.database.entity.TxnEntity

/**
 * DAO responsible for all transaction-related DB operations.
 *
 * Covers:
 * - Transaction CRUD
 * - Invoice queries
 * - Batch-based reporting
 * - Settlement-related aggregations
 */
@Dao
interface ITxnDao {

    /**
     * Inserts new transaction(s) into DB.
     */
    @Insert
    suspend fun insert(vararg txnEntity: TxnEntity)

    /**
     * Updates existing transaction(s).
     */
    @Update
    suspend fun update(vararg txnEntity: TxnEntity)

    // DAO
    @Query("UPDATE TxnTable SET cashEndBalance = :cash, snapEndBalance = :snap WHERE id = :id")
    suspend fun updateBalancesOnly(id: Long, cash: Double, snap: Double): Int

    /**
     * Fetch transaction by primary ID.
     */
    @Query("SELECT * FROM TxnTable WHERE Id = :id")
    suspend fun fetchTxnDetails(id: Long?): TxnEntity?

    @Query("SELECT * FROM TxnTable WHERE Id = :id")
    suspend fun getBatchDetailsTxn(id: Long): BatchEntity?

    /**
     * Returns all transactions.
     */
    @Query("SELECT * FROM TxnTable")
    suspend fun getAllTxnListData(): List<TxnEntity>?

    @Query("SELECT * FROM TxnTable WHERE substr(dateTime, 1, 16) <= :date")
     suspend fun getTransactionDetailsTxnBeforeTime(date: String): List<TxnEntity>

    @Query("""SELECT * FROM TxnTable WHERE TxnType NOT IN ('BALANCE_ENQUIRY_CASH','BALANCE_ENQUIRY_SNAP','CASH_WITHDRAWAL','PURCHASE_CASHBACK','VOID_LAST','E_VOUCHER') AND TxnStatus = 'APPROVED'AND isVoided = 0 ORDER BY id DESC """)
    suspend fun fetchAllVoidableTransactions(): List<TxnEntity>

    /**
     * Returns most recent transaction.
     */
    @Query("SELECT * FROM TxnTable ORDER BY id DESC LIMIT 1")
    suspend fun getLastTxnEntry(): TxnEntity?

    /**
     * Returns last approved financial transaction (excluding non-financial types).
     */
    @Query("""SELECT * FROM TxnTable WHERE TxnType NOT IN ('BALANCE_ENQUIRY_CASH', 'BALANCE_ENQUIRY_SNAP','FOODSTAMP_RETURN','CASH_WITHDRAWAL','PURCHASE_CASHBACK')AND TxnStatus = 'APPROVED' ORDER BY id DESC LIMIT 1 """)
    suspend fun getLastTxnEntryByTxnType(): TxnEntity?

    /**
     * Fetch transactions for a specific batch.
     */
    @Query("SELECT * FROM TxnTable WHERE batchId = :batchId ORDER BY id DESC")
    suspend fun fetchTxnListByBatchId(batchId: String): List<TxnEntity>?

    /**
     * Fetch total amount for an invoice.
     */
    @Query("SELECT TtlAmount FROM TxnTable WHERE InvoiceNo = :invoiceNo")
    suspend fun getTotalAmountByInvoiceNo(invoiceNo: String): String

    /**
     * Fetch transactions by invoice number.
     */
    @Query("SELECT * FROM TxnTable WHERE InvoiceNo = :invoiceNo")
    suspend fun fetchTrasactionByInvoiceNo(invoiceNo: String): List<TxnEntity>?

    /**
     * Fetch transaction using host reference.
     */
    @Query("SELECT * FROM TxnTable WHERE HostTxnRef = :hostTxnRef")
    suspend fun fetchTxnByHostTxnRef(hostTxnRef: String?): TxnEntity?

    /**
     * Fetch transaction time for invoice.
     */
    @Query("SELECT DateTime FROM TxnTable WHERE InvoiceNo = :invoiceNo")
    suspend fun getTimeDateByInvoiceNo(invoiceNo: String): String


    /**
     * Fetch transaction amount.
     */
    @Query("SELECT TxnAmount FROM TxnTable WHERE InvoiceNo = :invoiceNo")
    suspend fun getTxnAmountByInvoiceNo(invoiceNo: String): String

    /**
     * Fetch tip amount for invoice.
     */
    @Query("SELECT Tip FROM TxnTable WHERE InvoiceNo = :invoiceNo")
    suspend fun getTipAmountByInvoiceNo(invoiceNo: String): String

    /**
     * Get earliest transaction date for batch.
     * ⚠ returns List but query returns single value (BUG)
     */
    @Query("SELECT MIN(DateTime) FROM TxnTable WHERE batchId = :batchId")
    suspend fun getStartDateByBatchIds(batchId: String): List<String?>


    /**
     * Get latest transaction date for batch.
     * ⚠ same issue as above
     */
    @Query("SELECT MAX(DateTime) FROM TxnTable WHERE batchId = :batchId")
    suspend fun getEndDateByBatchIds(batchId: String): List<String?>

    /**
     * Fetch transactions between date range.
     */
    @Query("SELECT * FROM TxnTable WHERE DateTime BETWEEN :startDate AND :endDate")
    suspend fun getTransactionsByDateRange(startDate: String, endDate: String): List<TxnEntity>

    /**
     * Returns last invoice number in batch (as string).
     */
    @Query("SELECT InvoiceNo FROM TxnTable WHERE batchId = :batchId ORDER BY CAST(InvoiceNo AS INTEGER) DESC LIMIT 1")
    suspend fun getLastInvoiceNumber(batchId : String?): String?

    /**
     * Checks whether invoice exists in DB.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM TxnTable WHERE InvoiceNo = :invoiceNo)")
    suspend fun isRRLFound(invoiceNo: String): Boolean

}