package com.analogics.securityframework.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.analogics.securityframework.database.entity.BatchEntity

/**
 * DAO responsible for Batch lifecycle operations.
 *
 * Handles:
 * - Batch creation
 * - Batch status updates (OPEN / CLOSED)
 * - Batch lookup operations
 */
@Dao
interface IBatchDao {

    /**
     * Inserts a new batch record into the database.
     */
    @Insert
    suspend fun insert(vararg batchEntity: BatchEntity)

    /**
     * Updates an existing batch record.
     */
    @Update
    suspend fun update(vararg batchEntity: BatchEntity)

    /**
     * Returns the currently OPEN batch ID (if any).
     */
    @Query("SELECT DISTINCT BatchId FROM BatchTable WHERE BatchStatus = 'OPEN' LIMIT 1")
    suspend fun getOpenBatchId(): String?

    /**
     * Returns the most recently created batch ID.
     */
    @Query("SELECT BatchId FROM BatchTable ORDER BY id DESC LIMIT 1")
    suspend fun getLastBatchId(): String?

    /**
     * Returns all batch IDs in the system.
     * ⚠ Despite name, this is NOT a "presence check".
     */
    @Query("SELECT BatchId FROM BatchTable")
    suspend fun isBatchPresent(): List<String> // This returns all BatchId values

    /**
     * Closes a specific batch by ID.
     * Updates:
     * - Status → CLOSED
     * - ClosedDateTime → timestamp
     */
    @Query("UPDATE BatchTable SET BatchStatus = 'CLOSED', ClosedDateTime = :dateTime WHERE BatchId = :batchId")
    suspend fun closeBatch(batchId : String?, dateTime : String?): Int // This returns the number of rows affected


    /**
     * Closes all OPEN batches in the system.
     */
    @Query("UPDATE BatchTable SET BatchStatus = 'CLOSED', ClosedDateTime = :dateTime WHERE BatchStatus = 'OPEN'")
    suspend fun closeOpenBatches(dateTime : String?): Int // This returns the number of rows affected

    /**
     * Returns list of batches currently marked OPEN.
     * ⚠ Function name is misleading (returns status list, not batch list)
     */
    @Query("SELECT BatchStatus FROM BatchTable WHERE BatchStatus = 'OPEN'")
    suspend fun isBatchOpen(): List<String> // This returns all BatchId values

    /**
     * Returns batch status for a given batch ID.
     */
    @Query("SELECT BatchStatus FROM BatchTable WHERE BatchId = :batchId")
    suspend fun getBatchStatus(batchId : String?): String?

    /**
     * Checks if a batch exists in the database.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM BatchTable WHERE batchId = :batchId)")
    suspend fun isBatchExist(batchId: String?): Boolean

    /**
     * Returns all batches sorted by latest first.
     */
    @Query("SELECT DISTINCT * FROM BatchTable ORDER BY id DESC")
    suspend fun fetchBatchList(): List<BatchEntity>?

    /**
     * Returns latest batch details for a given batch ID.
     */
    @Query("SELECT * FROM BatchTable WHERE batchId = :batchId ORDER BY id DESC LIMIT 1")
    suspend fun fetchBatchDetails(batchId: String?): BatchEntity?
}