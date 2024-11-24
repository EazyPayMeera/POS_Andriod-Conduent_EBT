package com.analogics.securityframework.database.dbRepository

import android.os.Build
import android.text.format.DateUtils
import android.util.Log
import android.util.TimeUtils
import androidx.annotation.RequiresApi
import com.analogics.securityframework.database.dao.IBatchDao
import com.analogics.securityframework.database.dao.ITxnDao
import com.analogics.securityframework.database.dao.IUserManagementDao
import com.analogics.securityframework.database.dbConstant.DBConstant
import com.analogics.securityframework.database.entity.BatchEntity
import com.analogics.securityframework.database.entity.TxnEntity
import com.analogics.securityframework.database.entity.UserManagementEntity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class TxnDBRepository @Inject constructor(private val iBatchDao: IBatchDao, private val iTxnDao: ITxnDao,private val iUserManagementDao: IUserManagementDao) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun  insertBatch(batchEntity: BatchEntity){
        batchEntity.batchStatus = "open"
        batchEntity.openedDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(
            DBConstant.DATE_TIME_FORMAT_BATCH
        ))
        iBatchDao.insert(batchEntity)
    }
    suspend fun  updateBatch(batchEntity: BatchEntity){
        iBatchDao.update(batchEntity)
    }

    suspend fun isBatchPresent(): List<String> {
        return iBatchDao.isBatchPresent() // Ensure this returns List<BatchEntity>
    }

    suspend fun getLastBatch(): String? {
        return iBatchDao.getLastBatchId()
    }

    suspend fun openBatchId(): String? {
        return iBatchDao.getOpenBatchId()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun closeBatch(): Int {
        return iBatchDao.closeOpenBatches(LocalDateTime.now().format(DateTimeFormatter.ofPattern(
            DBConstant.DATE_TIME_FORMAT_BATCH
        )))
    }

    suspend fun isBatchOpen(): List<String> {
        return iBatchDao.isBatchOpen() // Returns the count of rows updated
    }

    suspend fun isBatchOpen(batchId: String?) : Boolean
    {
        return iBatchDao.getBatchStatus(batchId)?.equals("open")==true
    }

    suspend fun isAdmin(userId: String): Boolean {
        return iBatchDao.isAdmin(userId) // Returns the count of rows updated
    }


    suspend fun  insertTxn(txnEntity: TxnEntity){
        try {
            iTxnDao.insert(txnEntity)
        }catch ( e : Exception)
        {
            Log.e("DATABASE",e.message.toString())
        }

    }


    suspend fun  updateTxn(txnEntity: TxnEntity){
        iTxnDao.update(txnEntity)
    }


    // Get Transaction Details From Transaction Using Merchant-Id
    suspend fun fetchTransactionDetailsTxn(id: Long): TxnEntity? {
        return iTxnDao.getTransactionDetailsTxn(id)
    }

    // Get Transaction Details From Transaction Using Merchant-Id
    suspend fun fetchBatchDetailsTxn(id: Long): BatchEntity? {
        return iTxnDao.getBatchDetailsTxn(id)
    }

    suspend fun getAllTxnListData(): List<TxnEntity>?{
        return iTxnDao.getAllTxnListData()
    }

    suspend fun fetchTransactionDetailsTxnByDate(date: String): List<TxnEntity> {
        return iTxnDao.getTransactionDetailsTxnBeforeTime(date)
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

    suspend fun fetchTxnAmountByInvoiceNo(invoiceNo:String): String {
        return iTxnDao.getTxnAmountByInvoiceNo(invoiceNo)
    }

    suspend fun fetchTipAmountByInvoiceNo(invoiceNo:String): String {
        return iTxnDao.getTipAmountByInvoiceNo(invoiceNo)
    }

    suspend fun removeUser(user: String) {
        iTxnDao.deleteTransactionsByUserId(user)
    }

    suspend fun fetchBatchList(): List<BatchEntity>? {
        return iTxnDao.fetchBatchList()
    }

    suspend fun getStartDate(batchId: String): List<String?> {
        return iTxnDao.getStartDateByBatchIds(batchId)
    }

    suspend fun fetchPassword(user: String): String? {
        return iTxnDao.fetchPassword(user)
    }

    suspend fun getUserList(): List<String?> {
        return iTxnDao.getUserList()
    }

    suspend fun getBatchStatus(batchId: String): List<String?> {
        return iTxnDao.getBatchStatus(batchId)
    }

    suspend fun getEndDate(batchId: String): List<String?> {
        return iTxnDao.getEndDateByBatchIds(batchId)
    }

    suspend fun getTransactionsByDateRange(startDate: String, endDate: String): List<TxnEntity>? {
        return iTxnDao.getTransactionsByDateRange(startDate, endDate)
    }

    suspend fun getLastInvoiceNumber() : Int
    {
        return iTxnDao.getLastInvoiceNumber(openBatchId())?.toIntOrNull()?:0
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

    suspend fun getUserCount(): Int {
        return iUserManagementDao.getUserCount()
    }
}