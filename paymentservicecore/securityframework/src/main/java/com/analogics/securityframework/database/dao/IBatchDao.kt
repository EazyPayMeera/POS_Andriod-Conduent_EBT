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

    // Query to fetch a transaction by MerchantId
    @Query("SELECT DISTINCT BatchId FROM BatchTable WHERE BatchStatus = 'open' LIMIT 1")
    suspend fun getOpenBatchId(): String?

    @Query("SELECT BatchId FROM BatchTable ORDER BY BatchId DESC LIMIT 1")
    suspend fun getLastBatchId(): String?

    @Query("SELECT BatchId FROM BatchTable")
    suspend fun isBatchPresent(): List<String> // This returns all BatchId values

    @Query("UPDATE BatchTable SET BatchStatus = 'close' WHERE BatchStatus = 'open'")
    suspend fun closeOpenBatches(): Int // This returns the number of rows affected

    @Query("SELECT BatchStatus FROM BatchTable WHERE BatchStatus = 'open'")
    suspend fun isBatchOpen(): List<String> // This returns all BatchId values

    @Query("SELECT EXISTS(SELECT 1 FROM UserTable WHERE userId = :userId AND userType = 'ADMIN')")
    suspend fun isAdmin(userId: String): Boolean



}