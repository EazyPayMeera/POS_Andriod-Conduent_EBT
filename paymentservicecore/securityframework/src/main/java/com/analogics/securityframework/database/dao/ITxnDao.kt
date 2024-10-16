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
    @Query("SELECT * FROM TxnTable WHERE Id = :id")
    suspend fun getTransactionDetailsTxn(id: Long): TxnEntity?

    @Query("SELECT * FROM TxnTable")
    suspend fun getAllTxnListData(): List<TxnEntity>

    @Query("SELECT * FROM TxnTable WHERE substr(dateTime, 1, 16) <= :date")
     suspend fun getTransactionDetailsTxnBeforeTime(date: String): List<TxnEntity>

    @Query("SELECT * FROM TxnTable ORDER BY id DESC LIMIT 1")
    suspend fun getLastTxnEntry(): TxnEntity?

    @Query("SELECT * FROM TxnTable WHERE batchId = :batchId")
    suspend fun getTrasactionsByBatchId(batchId: String): List<TxnEntity>

    @Query("SELECT DISTINCT batchId FROM TxnTable")
    suspend fun getDistinctBatchIds(): List<String>

    @Query("SELECT MIN(DateTime) FROM TxnTable WHERE batchId = :batchId")
    suspend fun getStartDateByBatchIds(batchId: String): List<String?>

    @Query("SELECT MAX(DateTime) FROM TxnTable WHERE batchId = :batchId")
    suspend fun getEndDateByBatchIds(batchId: String): List<String?>

}