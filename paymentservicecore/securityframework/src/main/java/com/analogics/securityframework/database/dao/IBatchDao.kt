package com.analogics.securityframework.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.analogics.securityframework.database.entity.BatchEntity

@Dao
interface IBatchDao {

    @Insert
    suspend fun insert(vararg batchEntity: BatchEntity)

    @Update
    suspend fun update(vararg batchEntity: BatchEntity)

    @Query("SELECT DISTINCT BatchId FROM BatchTable WHERE BatchStatus = 'OPEN' LIMIT 1")
    suspend fun getOpenBatchId(): String?

    @Query("SELECT BatchId FROM BatchTable ORDER BY id DESC LIMIT 1")
    suspend fun getLastBatchId(): String?

    @Query("SELECT BatchId FROM BatchTable")
    suspend fun isBatchPresent(): List<String> // This returns all BatchId values

    @Query("UPDATE BatchTable SET BatchStatus = 'CLOSED', ClosedDateTime = :dateTime WHERE BatchId = :batchId")
    suspend fun closeBatch(batchId : String?, dateTime : String?): Int // This returns the number of rows affected

    @Query("UPDATE BatchTable SET BatchStatus = 'CLOSED', ClosedDateTime = :dateTime WHERE BatchStatus = 'OPEN'")
    suspend fun closeOpenBatches(dateTime : String?): Int // This returns the number of rows affected

    @Query("SELECT BatchStatus FROM BatchTable WHERE BatchStatus = 'OPEN'")
    suspend fun isBatchOpen(): List<String> // This returns all BatchId values

    @Query("SELECT BatchStatus FROM BatchTable WHERE BatchId = :batchId")
    suspend fun getBatchStatus(batchId : String?): String?

    @Query("SELECT EXISTS(SELECT 1 FROM BatchTable WHERE batchId = :batchId)")
    suspend fun isBatchExist(batchId: String?): Boolean

    @Query("SELECT DISTINCT * FROM BatchTable ORDER BY id DESC")
    suspend fun fetchBatchList(): List<BatchEntity>?

    @Query("SELECT * FROM BatchTable WHERE batchId = :batchId ORDER BY id DESC LIMIT 1")
    suspend fun fetchBatchDetails(batchId: String?): BatchEntity?
}