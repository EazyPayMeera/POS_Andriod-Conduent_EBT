package com.analogics.securityframework.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.analogics.securityframework.database.entity.TxnEntity
import java.util.Date

@Dao
interface ITxnDao {

    @Insert
    suspend fun insert(vararg txnEntity: TxnEntity)

    @Update
    suspend fun update(vararg txnEntity: TxnEntity)

    // Query to fetch a transaction by MerchantId
    @Query("SELECT * FROM TxnTable WHERE Id = :id")
    suspend fun getTransactionDetailsTxn(id: Long): TxnEntity?

    @Query("SELECT * FROM TxnTable")
    suspend fun getAllTxnListData(): List<TxnEntity>

    @Query("SELECT * FROM TxnTable WHERE DateTime <= :dateTime")
    suspend fun getTransactionDetailsTxnBeforeTime(dateTime: Date): List<TxnEntity>
}