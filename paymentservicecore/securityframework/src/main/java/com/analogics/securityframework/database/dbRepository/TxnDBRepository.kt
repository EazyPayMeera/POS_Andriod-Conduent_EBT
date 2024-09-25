package com.analogics.securityframework.database.dbRepository

import com.analogics.securityframework.database.dao.IBatchDao
import com.analogics.securityframework.database.dao.ITxnDao
import com.analogics.securityframework.database.entity.BatchEntity
import com.analogics.securityframework.database.entity.TxnEntity
import javax.inject.Inject

class TxnDBRepository @Inject constructor(private val iBatchDao: IBatchDao, private val iTxnDao: ITxnDao) {
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
        iTxnDao.insert(txnEntity)
    }
    suspend fun  updateTxn(txnEntity: TxnEntity){
        iTxnDao.update(txnEntity)
    }
    // Get Transaction Details From Transaction Using Merchant-Id
    suspend fun fetchTransactionDetailsTxn(merchantId: String): TxnEntity? {
        return iTxnDao.getTransactionDetailsTxn(merchantId)
    }

    suspend fun getAllTxnListData(): List<TxnEntity>{
        return iTxnDao.getAllTxnListData()
    }
}