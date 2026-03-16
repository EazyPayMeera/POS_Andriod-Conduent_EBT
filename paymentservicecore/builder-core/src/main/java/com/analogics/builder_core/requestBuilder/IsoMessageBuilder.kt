package com.analogics.builder_core.requestBuilder

import com.eazypaytech.builder_core.model.BuilderServiceTxnDetails

object IsoMessageBuilder {

    private var lastTxn: BuilderServiceTxnDetails? = null

    fun saveTxn(txn: BuilderServiceTxnDetails?) {
        lastTxn = txn?.copy()
    }

    fun getLastTxn(): BuilderServiceTxnDetails? {
        return lastTxn
    }
}