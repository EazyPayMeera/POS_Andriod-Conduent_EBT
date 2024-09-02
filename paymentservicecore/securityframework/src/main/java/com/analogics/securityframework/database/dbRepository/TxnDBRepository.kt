package com.analogics.securityframework.database.dbRepository

import com.analogics.securityframework.database.dao.ITxnDao
import com.analogics.securityframework.database.entity.TxnDtlsEntity
import javax.inject.Inject

class TxnDBRepository @Inject constructor(val iTxnDao: ITxnDao) {
    suspend fun  insert(txnDtlsEntity: TxnDtlsEntity){
        iTxnDao.insertAll(txnDtlsEntity)
    }
}