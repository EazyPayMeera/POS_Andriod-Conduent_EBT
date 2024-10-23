package com.analogics.securityframework.database.dbRepository

import android.util.Log
import com.analogics.securityframework.database.dao.IBatchDao
import com.analogics.securityframework.database.dao.ITxnDao
import com.analogics.securityframework.database.dao.IUserManagementDao
import com.analogics.securityframework.database.entity.BatchEntity
import com.analogics.securityframework.database.entity.TxnEntity
import com.analogics.securityframework.database.entity.UserManagementEntity
import javax.inject.Inject

class TxnDBRepository @Inject constructor(private val iBatchDao: IBatchDao, private val iTxnDao: ITxnDao,private val iUserManagementDao: IUserManagementDao) {
    suspend fun  insertBatch(batchEntity: BatchEntity){
        iBatchDao.insert(batchEntity)
    }
    suspend fun  updateBatch(batchEntity: BatchEntity){
        iBatchDao.update(batchEntity)
    }
    // Get Transaction Details From Batch Using Merchant-Id
    suspend fun fetchTransactionDetailsBatch(merchantId: String): TxnEntity? {
        return iBatchDao.getTransactionDetailsTxnBatch(merchantId)
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

    suspend fun getAllTxnListData(): List<TxnEntity>{
        return iTxnDao.getAllTxnListData()
    }

    suspend fun fetchTransactionDetailsTxnByDate(date: String): List<TxnEntity> {
        return iTxnDao.getTransactionDetailsTxnBeforeTime(date)
    }

    suspend fun fetchLastTransaction(): TxnEntity? {
        return iTxnDao.getLastTxnEntry()
    }

    suspend fun fetchTransactionByBatch(batchId:String): List<TxnEntity> {
        return iTxnDao.getTrasactionsByBatchId(batchId)
    }

    suspend fun fetchTransactionDetailsByBatchId(): List<String> {
        return iTxnDao.getDistinctBatchIds()
    }

    suspend fun getStartDate(batchId: String): List<String?> {
        return iTxnDao.getStartDateByBatchIds(batchId)
    }

    suspend fun getEndDate(batchId: String): List<String?> {
        return iTxnDao.getEndDateByBatchIds(batchId)
    }

    suspend fun  insertUser(userManagementEntity: UserManagementEntity){
        iUserManagementDao.insert(userManagementEntity)
    }
    suspend fun  updateUser(userManagementEntity: UserManagementEntity){
        iUserManagementDao.update(userManagementEntity)
    }
    // Get Transaction Details From Batch Using Merchant-Id
    suspend fun getUserDetails(userId: String): UserManagementEntity? {
        return iUserManagementDao.getUserDetails(userId)
    }

}