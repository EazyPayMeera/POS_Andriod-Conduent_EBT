package com.eazypaytech.securityframework.database.dbRepository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.eazypaytech.securityframework.database.dao.IBatchDao
import com.eazypaytech.securityframework.database.dao.ITxnDao
import com.eazypaytech.securityframework.database.dao.IUserManagementDao
import com.eazypaytech.securityframework.database.dbConstant.DBConstant
import com.eazypaytech.securityframework.database.entity.BatchEntity
import com.eazypaytech.securityframework.database.entity.TxnEntity
import com.eazypaytech.securityframework.database.entity.UserManagementEntity
import com.eazypaytech.securityframework.model.BatchStatus
import com.eazypaytech.securityframework.model.TxnStatus
import com.eazypaytech.securityframework.model.TxnType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class TxnDBRepository @Inject constructor(private val iBatchDao: IBatchDao, private val iTxnDao: ITxnDao, private val iUserManagementDao: IUserManagementDao) {

    /* Batch Management */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun  insertBatch(batchEntity: BatchEntity){
        iBatchDao.insert(batchEntity)
    }

    suspend fun  updateBatch(batchEntity: BatchEntity){
        iBatchDao.update(batchEntity)
    }

    suspend fun isBatchPresent(): List<String> {
        return iBatchDao.isBatchPresent() // Ensure this returns List<BatchEntity>
    }

    suspend fun fetchLastBatchId(): String? {
        return iBatchDao.getLastBatchId()
    }

    suspend fun fetchOpenBatchId(): String? {
        return iBatchDao.getOpenBatchId()
    }

    suspend fun fetchBatchDetails(batchId: String?): BatchEntity? {
        return iBatchDao.fetchBatchDetails(batchId)
    }

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

    suspend fun isBatchOpen(batchId: String?=null) : Boolean
    {
        return iBatchDao.getBatchStatus(batchId?:fetchLastBatchId())?.equals("OPEN")==true
    }

    suspend fun isBatchExist(batchId: String?): Boolean
    {
        return iBatchDao.isBatchExist(batchId)
    }

    suspend fun fetchBatchList(): List<BatchEntity>? {
        return iBatchDao.fetchBatchList()
    }

    /* Transaction Management */
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

    suspend fun updateTxn(txnEntity: TxnEntity){
        try {
            iTxnDao.update(txnEntity)
        }catch ( e : Exception)
        {
            Log.e("DATABASE",e.message.toString())
        }
    }

    suspend fun fetchTxnById(id: Long?): TxnEntity? {
        return iTxnDao.fetchTxnDetails(id)
    }

    suspend fun getAllTxnListData(): List<TxnEntity>?{
        return iTxnDao.getAllTxnListData()
    }

    suspend fun fetchLastTransaction(): TxnEntity? {
        return iTxnDao.getLastTxnEntry()
    }

    suspend fun fetchTxnListByBatchId(batchId:String): List<TxnEntity>? {
        return iTxnDao.fetchTxnListByBatchId(batchId)
    }

    suspend fun fetchTotalAmountByInvoiceNo(invoiceNo:String): String {
        return iTxnDao.getTotalAmountByInvoiceNo(invoiceNo)
    }

    suspend fun fetchTransactionByInvoiceNo(invoiceNo:String): List<TxnEntity>? {
        return iTxnDao.fetchTrasactionByInvoiceNo(invoiceNo)
    }

    suspend fun fetchTxnByHostTxnRef(hostTxnRef:String?): TxnEntity? {
        return iTxnDao.fetchTxnByHostTxnRef(hostTxnRef)
    }

    suspend fun fetchTimeDateByInvoiceNo(invoiceNo:String): String {
        return iTxnDao.getTimeDateByInvoiceNo(invoiceNo)
    }

    suspend fun fetchTxnAmountByInvoiceNo(invoiceNo:String): String {
        return iTxnDao.getTxnAmountByInvoiceNo(invoiceNo)
    }

    suspend fun fetchTipAmountByInvoiceNo(invoiceNo:String): String {
        return iTxnDao.getTipAmountByInvoiceNo(invoiceNo)
    }

    suspend fun getTransactionsByDateRange(startDate: String, endDate: String): List<TxnEntity>? {
        return iTxnDao.getTransactionsByDateRange(startDate, endDate)
    }

    suspend fun getLastInvoiceNumber() : Int {
        return iTxnDao.getLastInvoiceNumber(fetchLastBatchId())?.toIntOrNull()?:0
    }

    /* Clerk/User Management */
    suspend fun  insertUser(userManagementEntity: UserManagementEntity){
        iUserManagementDao.insert(userManagementEntity)
    }

    suspend fun  updateUser(userManagementEntity: UserManagementEntity){
        iUserManagementDao.update(userManagementEntity)
    }

    suspend fun getUserDetails(userId: String): UserManagementEntity? {
        return iUserManagementDao.getUserDetails(userId)
    }

    suspend fun getAllUserDetails(): List<UserManagementEntity>? {
        return iUserManagementDao.getAllUserDetails()
    }

    suspend fun getUserList(): List<String?> {
        return iUserManagementDao.getUserList()
    }

    suspend fun getUserCount(): Int {
        return iUserManagementDao.getUserCount()
    }

    suspend fun getAdminCount(): Int {
        return iUserManagementDao.getAdminCount()
    }

    suspend fun fetchPassword(user: String): String? {
        return iUserManagementDao.fetchPassword(user)
    }

    suspend fun removeUser(user: String) {
        iUserManagementDao.deleteUser(user)
    }

    suspend fun isAdmin(userId: String): Boolean {
        return iUserManagementDao.isAdmin(userId) // Returns the count of rows updated
    }

    suspend fun isRRLFound(invoiceNo: String): Boolean {
        return iTxnDao.isRRLFound(invoiceNo) // Returns the count of rows updated
    }
}