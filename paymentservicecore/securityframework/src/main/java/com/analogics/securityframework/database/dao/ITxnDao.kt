package com.analogics.securityframework.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import com.analogics.securityframework.database.entity.TxnDtlsEntity
@Dao
interface ITxnDao {

    @Insert
    suspend fun insertAll(vararg txnDtlsEntity: TxnDtlsEntity)

    @Update
    suspend fun updateTxnEntity(vararg txnDtlsEntity: TxnDtlsEntity)
}