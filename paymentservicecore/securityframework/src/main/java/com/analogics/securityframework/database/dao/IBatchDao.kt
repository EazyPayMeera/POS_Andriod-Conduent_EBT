package com.analogics.securityframework.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.analogics.securityframework.database.entity.BatchEntity
import com.analogics.securityframework.database.entity.TxnEntity

@Dao
interface IBatchDao {

    @Insert
    suspend fun insert(vararg batchEntity: BatchEntity)

    @Update
    suspend fun update(vararg batchEntity: BatchEntity)

    // Query to fetch a transaction by MerchantId
    @Query("SELECT * FROM TxnTable WHERE MerchantId = :merchantId")
    suspend fun getTransactionDetailsTxnBatch(merchantId: String): TxnEntity?
}