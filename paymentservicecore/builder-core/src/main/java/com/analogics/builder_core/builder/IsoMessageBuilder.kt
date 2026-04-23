package com.analogics.builder_core.builder

import com.analogics.builder_core.data.model.SavedTxnData
/**
 * Singleton object used to store and retrieve the last transaction data.
 *
 * Purpose:
 * - Keeps the most recent transaction in memory
 * - Used for operations like reversal, void, or reference reuse
 *
 * ⚠️ Note:
 * - This is in-memory only (data will be lost if app is killed)
 * - Not thread-safe (consider synchronization if accessed from multiple threads)
 */
object IsoMessageBuilder {
    /**
     * Holds the last saved transaction data.
     */
    var lastTxnData: SavedTxnData? = null

    /**
     * Saves the latest transaction data.
     *
     * @param txn Transaction data to be stored
     */
    fun saveTxn(txn: SavedTxnData) {
        lastTxnData = txn
    }

    /**
     * Retrieves the last saved transaction.
     *
     * @return Last transaction data or null if none exists
     */
    fun getLastTxn(): SavedTxnData? {
        return lastTxnData
    }
}