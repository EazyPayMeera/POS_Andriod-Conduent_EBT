package com.analogics.securityframework.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.analogics.securityframework.database.dao.IBatchDao
import com.analogics.securityframework.database.dao.ITxnDao
import com.analogics.securityframework.database.dao.IUserManagementDao
import com.analogics.securityframework.database.constants.DBConstant
import com.analogics.securityframework.database.entity.BatchEntity
import com.analogics.securityframework.database.entity.TxnEntity
import com.analogics.securityframework.database.entity.UserManagementEntity
import com.analogics.securityframework.data.model.BatchStatus
import com.analogics.securityframework.data.model.TxnStatus
import com.analogics.securityframework.data.model.TxnType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject


/**
 * Repository responsible for:
 * - Batch lifecycle management (OPEN / CLOSE / SETTLEMENT)
 * - Transaction persistence and updates
 * - User management (clerk/admin handling)
 *
 * This acts as a single source of truth for database operations
 * in the payment/transaction system.
 */
class TxnDBRepository @Inject constructor(private val iBatchDao: IBatchDao, private val iTxnDao: ITxnDao, private val iUserManagementDao: IUserManagementDao) {


    /**
     * Inserts a new batch record into DB.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun  insertBatch(batchEntity: BatchEntity){
        iBatchDao.insert(batchEntity)
    }

    /**
     * Updates an existing batch record.
     */
    suspend fun  updateBatch(batchEntity: BatchEntity){
        iBatchDao.update(batchEntity)
    }

    suspend fun fetchAllVoidableTransactions(): List<TxnEntity> {
        return iTxnDao.fetchAllVoidableTransactions()
    }

    suspend fun  deleteOldTransactions(){
        iTxnDao.deleteOldTransactions()
    }

    /**
     * Checks whether any batch exists in DB.
     */
    suspend fun isBatchPresent(): List<String> {
        return iBatchDao.isBatchPresent() // Ensure this returns List<BatchEntity>
    }

    /**
     * Fetches last created batch ID.
     */
    suspend fun fetchLastBatchId(): String? {
        return iBatchDao.getLastBatchId()
    }


    /**
     * Fetches currently open batch ID if available.
     */
    suspend fun fetchOpenBatchId(): String? {
        return iBatchDao.getOpenBatchId()
    }

    /**
     * Fetches complete batch details by batchId.
     */
    suspend fun fetchBatchDetails(batchId: String?): BatchEntity? {
        return iBatchDao.fetchBatchDetails(batchId)
    }

    /**
     * Opens a batch:
     * - Creates new batch if not exists
     * - Updates status to OPEN if previously closed
     *
     * Ensures a transaction always belongs to an active batch.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun openBatch(txnEntity: TxnEntity) {
        txnEntity.batchId?.let {
            isBatchExist(it).let {
                if(it == false) {
                    insertBatch(
                        BatchEntity(
                            merchantId = txnEntity.merchantId,
                            terminalId = txnEntity.terminalId,
                            cashierId = txnEntity.cashierId,
                            batchId = txnEntity.batchId,
                            batchStatus = BatchStatus.OPEN.toString(),
                            openedDateTime = LocalDateTime.now().format(
                                DateTimeFormatter.ofPattern(
                                    DBConstant.DATE_TIME_FORMAT_BATCH
                                )
                            )
                        )
                    )
                }
                else if(isBatchOpen(txnEntity.batchId) == false)
                {
                    fetchBatchDetails(txnEntity.batchId)?.let {
                        it.batchStatus = BatchStatus.OPEN.toString()
                        it.openedDateTime = LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern(
                                DBConstant.DATE_TIME_FORMAT_BATCH
                            )
                        )
                        updateBatch(it)
                    }
                }
            }
        }
    }

    /**
     * Closes batch:
     * - If batchId provided → close specific batch
     * - Else → close all open batches
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun closeBatch(batchId: String?=null): Int {
        return if (batchId != null)
            iBatchDao.closeBatch(
                batchId,
                LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern(
                        DBConstant.DATE_TIME_FORMAT_BATCH
                    )
                )
            )
        else
            iBatchDao.closeOpenBatches(
                LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern(
                        DBConstant.DATE_TIME_FORMAT_BATCH
                    )
                )
            )
    }

    /**
     * Checks whether a batch is in OPEN state.
     */
    suspend fun isBatchOpen(batchId: String?=null) : Boolean
    {
        return iBatchDao.getBatchStatus(batchId?:fetchLastBatchId())?.equals("OPEN")==true
    }


    /**
     * Checks if batch exists in DB.
     */
    suspend fun isBatchExist(batchId: String?): Boolean
    {
        return iBatchDao.isBatchExist(batchId)
    }

    /**
     * Returns full list of batches.
     */
    suspend fun fetchBatchList(): List<BatchEntity>? {
        return iBatchDao.fetchBatchList()
    }

    /* --------------------------------------------------------
    * TRANSACTION MANAGEMENT
    * -------------------------------------------------------- */

    /**
     * Inserts or updates transaction.
     *
     * Flow:
     * - Ensures batch is open
     * - Inserts new txn OR updates existing txn
     * - Applies VOID / REFUND / CAPTURE logic on linked transactions
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insertOrUpdateTxn(txnEntity: TxnEntity?) {
        try {
            txnEntity?.let {
                openBatch(it).let {
                    txnEntity.id?.let {
                        iTxnDao.fetchTxnDetails(it)?.let {
                            iTxnDao.update(txnEntity)
                        } ?: let {
                            iTxnDao.insert(txnEntity)
                        }
                    }.also {
                        /* Update original linked transaction */
                        when(txnEntity.txnType)
                        {
                            TxnType.VOID.toString() -> fetchTxnByHostTxnRef(txnEntity.hostTxnRef)?.let {
                                iTxnDao.update(it.copy(isVoided = true, txnStatus = TxnStatus.VOIDED.toString()))
                            }
                            TxnType.REFUND.toString() -> fetchTxnByHostTxnRef(txnEntity.hostTxnRef)?.let {
                                iTxnDao.update(it.copy(isRefunded = true, txnStatus = TxnStatus.REFUNDED.toString()))
                            }
                            TxnType.AUTHCAP.toString() -> fetchTxnByHostTxnRef(txnEntity.hostTxnRef)?.let {
                                iTxnDao.update(it.copy(isCaptured = true, txnStatus = TxnStatus.CAPTURED.toString()))
                            }
                        }
                    }
                }
            }
        }catch ( e : Exception)
        {
            Log.e("DATABASE",e.message.toString())
        }
    }

    /**
     * Updates existing transaction.
     */
    suspend fun updateTxn(txnEntity: TxnEntity) {
        try {
            val existing = iTxnDao.fetchTxnDetails(txnEntity.id)

            // ✅ Preserve receiptEmvData if null
            if (txnEntity.receiptEmvData == null) {
                txnEntity.receiptEmvData = existing?.receiptEmvData
            }

            // ✅ Preserve balances — String? comparison
            val existingCash = existing?.cashEndBalance
            val existingSnap = existing?.snapEndBalance

            if ((txnEntity.cashEndBalance == null || txnEntity.cashEndBalance == "0.0")
                && (existingCash != null && existingCash != "0.0")) {
                txnEntity.cashEndBalance = existingCash
                Log.w("DATABASE", "⚠ Preserved cashEndBalance=$existingCash")
            }

            if ((txnEntity.snapEndBalance == null || txnEntity.snapEndBalance == "0.0")
                && (existingSnap != null && existingSnap != "0.0")) {
                txnEntity.snapEndBalance = existingSnap
                Log.w("DATABASE", "⚠ Preserved snapEndBalance=$existingSnap")
            }

            Log.d("DATABASE", "▶ updateTxn — id=${txnEntity.id}, cash=${txnEntity.cashEndBalance}, snap=${txnEntity.snapEndBalance}")
            iTxnDao.update(txnEntity)
            Log.d("DATABASE", "✅ updateTxn success")

        } catch (e: Exception) {
            Log.e("DATABASE", "❌ updateTxn FAILED: ${e.message}")
        }
    }

    suspend fun updateBalancesOnly(id: Long, cash: Double, snap: Double) {
        try {
            val rows = iTxnDao.updateBalancesOnly(id, cash, snap)
            Log.d("DATABASE", "✅ updateBalancesOnly — id=$id, cash=$cash, snap=$snap, rows=$rows")
        } catch (e: Exception) {
            Log.e("DATABASE", "❌ updateBalancesOnly FAILED: ${e.message}")
        }
    }

    /**
     * Fetch transaction by primary ID.
     */
    suspend fun fetchTxnById(id: Long?): TxnEntity? {
        return iTxnDao.fetchTxnDetails(id)
    }

    /**
     * Fetch all transactions.
     */
    suspend fun getAllTxnListData(): List<TxnEntity>?{
        return iTxnDao.getAllTxnListData()
    }

    /**
     * Fetch last transaction entry.
     */
    suspend fun fetchLastTransaction(): TxnEntity? {
        return iTxnDao.getLastTxnEntry()
    }

    /**
     * Fetch last transaction filtered by type.
     */
    suspend fun fetchLastTransactionByTxnType(): TxnEntity? {
        return iTxnDao.getLastTxnEntryByTxnType()
    }

    /**
     * Fetch all transactions under a batch.
     */
    suspend fun fetchTxnListByBatchId(batchId:String): List<TxnEntity>? {
        return iTxnDao.fetchTxnListByBatchId(batchId)
    }

    /**
     * Get total transaction amount for invoice.
     */
    suspend fun fetchTotalAmountByInvoiceNo(invoiceNo:String): String {
        return iTxnDao.getTotalAmountByInvoiceNo(invoiceNo)
    }

    /**
     * Fetch transactions by invoice number.
     */
    suspend fun fetchTransactionByInvoiceNo(invoiceNo:String): List<TxnEntity>? {
        return iTxnDao.fetchTrasactionByInvoiceNo(invoiceNo)
    }

    /**
     * Fetch transaction using host reference ID.
     */
    suspend fun fetchTxnByHostTxnRef(hostTxnRef:String?): TxnEntity? {
        return iTxnDao.fetchTxnByHostTxnRef(hostTxnRef)
    }

    /**
     * Fetch timestamp for invoice.
     */
    suspend fun fetchTimeDateByInvoiceNo(invoiceNo:String): String {
        return iTxnDao.getTimeDateByInvoiceNo(invoiceNo)
    }

    /**
     * Fetch transaction amount.
     */
    suspend fun fetchTxnAmountByInvoiceNo(invoiceNo:String): String {
        return iTxnDao.getTxnAmountByInvoiceNo(invoiceNo)
    }

    /**
     * Fetch tip amount.
     */
    suspend fun fetchTipAmountByInvoiceNo(invoiceNo:String): String {
        return iTxnDao.getTipAmountByInvoiceNo(invoiceNo)
    }

    /**
     * Fetch transactions within date range.
     */
    suspend fun getTransactionsByDateRange(startDate: String, endDate: String): List<TxnEntity>? {
        return iTxnDao.getTransactionsByDateRange(startDate, endDate)
    }

    /**
     * Get last invoice number for current batch.
     */
    suspend fun getLastInvoiceNumber() : Int {
        return iTxnDao.getLastInvoiceNumber(fetchLastBatchId())?.toIntOrNull()?:0
    }

    /* --------------------------------------------------------
     * USER MANAGEMENT
     * -------------------------------------------------------- */

    /**
     * Inserts a new user.
     */
    suspend fun  insertUser(userManagementEntity: UserManagementEntity){
        iUserManagementDao.insert(userManagementEntity)
    }

    /**
     * Updates user details.
     */
    suspend fun  updateUser(userManagementEntity: UserManagementEntity){
        iUserManagementDao.update(userManagementEntity)
    }

    /**
     * Fetch user details by ID.
     */
    suspend fun getUserDetails(userId: String): UserManagementEntity? {
        return iUserManagementDao.getUserDetails(userId)
    }


    /**
     * Fetch all users.
     */
    suspend fun getAllUserDetails(): List<UserManagementEntity>? {
        return iUserManagementDao.getAllUserDetails()
    }

    /**
     * Returns list of all user IDs.
     */
    suspend fun getUserList(): List<String?> {
        return iUserManagementDao.getUserList()
    }

    /**
     * Count total users.
     */
    suspend fun getUserCount(): Int {
        return iUserManagementDao.getUserCount()
    }

    /**
     * Count admin users.
     */
    suspend fun getAdminCount(): Int {
        return iUserManagementDao.getAdminCount()
    }

    /**
     * Fetch stored password for user (⚠ security sensitive).
     */
    suspend fun fetchPassword(user: String): String? {
        return iUserManagementDao.fetchPassword(user)
    }

    /**
     * Deletes a user from DB.
     */
    suspend fun removeUser(user: String) {
        iUserManagementDao.deleteUser(user)
    }

    /**
     * Checks if user has admin role.
     */
    suspend fun isAdmin(userId: String): Boolean {
        return iUserManagementDao.isAdmin(userId) // Returns the count of rows updated
    }

    /**
     * Checks if RRL exists for invoice.
     */
    suspend fun isRRLFound(invoiceNo: String): Boolean {
        return iTxnDao.isRRLFound(invoiceNo) // Returns the count of rows updated
    }
}