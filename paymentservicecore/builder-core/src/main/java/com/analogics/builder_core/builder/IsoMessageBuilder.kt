package com.analogics.builder_core.builder

import com.analogics.builder_core.data.model.SavedTxnData

object IsoMessageBuilder {
    var lastTxnData: SavedTxnData? = null

    fun saveTxn(txn: SavedTxnData) {
        lastTxnData = txn
    }

    fun getLastTxn(): SavedTxnData? {
        return lastTxnData
    }
}