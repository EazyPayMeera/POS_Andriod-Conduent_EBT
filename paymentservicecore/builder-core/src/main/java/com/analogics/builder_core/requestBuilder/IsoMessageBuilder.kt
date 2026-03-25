package com.analogics.builder_core.requestBuilder


object IsoMessageBuilder {
    var lastTxnData: SavedTxnData? = null

    fun saveTxn(txn: SavedTxnData) {
        lastTxnData = txn
    }

    fun getLastTxn(): SavedTxnData? {
        return lastTxnData
    }
}