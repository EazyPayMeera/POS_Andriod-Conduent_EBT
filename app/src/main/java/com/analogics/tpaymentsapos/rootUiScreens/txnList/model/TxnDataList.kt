package com.analogics.tpaymentsapos.rootUiScreens.txnList.model

data class TxnDataList(  val id: Int,
                         val date: String,
                         val type: String,
                         val amount: Double,
                         val isPositive: Boolean)


