package com.analogics.securityframework.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.analogics.securityframework.database.entity.TxnEntity
@Dao
interface ITxnDao {

    @Insert
    suspend fun insert(vararg txnEntity: TxnEntity)

    @Update
    suspend fun update(vararg txnEntity: TxnEntity)

    // Query to fetch a transaction by MerchantId
    @Query("SELECT * FROM TxnTable WHERE MerchantId = :merchantId")
    suspend fun getTransactionDetailsTxn(merchantId: String): TxnEntity?
}