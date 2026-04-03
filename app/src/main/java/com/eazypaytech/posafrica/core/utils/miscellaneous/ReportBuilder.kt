package com.eazypaytech.posafrica.core.utils.miscellaneous

import com.eazypaytech.paymentservicecore.models.TxnStatus
import com.eazypaytech.paymentservicecore.models.TxnType
import com.eazypaytech.posafrica.domain.model.ObjRootAppPaymentDetails

class ReportBuilder(val list: List<ObjRootAppPaymentDetails>?) {

    fun getTotal(listTxnType: List<TxnType>?= listOf(TxnType.FOOD_PURCHASE), listTxnStatus: List<TxnStatus>?= listOf(TxnStatus.APPROVED,TxnStatus.REFUNDED)) : Double {
        return list?.filter {
            listTxnType?.contains(it.txnType)==true && listTxnStatus?.contains(it.txnStatus)==true
        }?.sumOf {
            it.ttlAmount ?: 0.0
        }?:0.00
    }

    fun getSubTotal(listTxnType: List<TxnType>?= listOf(TxnType.FOOD_PURCHASE), listTxnStatus: List<TxnStatus>?= listOf(TxnStatus.APPROVED,TxnStatus.REFUNDED)) : Double {
        return list?.filter {
            listTxnType?.contains(it.txnType)==true && listTxnStatus?.contains(it.txnStatus)==true
        }?.sumOf {
            it.txnAmount ?: 0.0
        }?:0.00
    }

    fun getTip(listTxnType: List<TxnType>?= listOf(TxnType.FOOD_PURCHASE), listTxnStatus: List<TxnStatus>?= listOf(TxnStatus.APPROVED,TxnStatus.REFUNDED)) : Double {
        return list?.filter {
            listTxnType?.contains(it.txnType)==true && listTxnStatus?.contains(it.txnStatus)==true
        }?.sumOf {
            it.tip ?: 0.0
        }?:0.00
    }

    fun getTipCount(listTxnType: List<TxnType>?= listOf(TxnType.FOOD_PURCHASE), listTxnStatus: List<TxnStatus>?= listOf(TxnStatus.APPROVED,TxnStatus.REFUNDED)) : Int {
        return list?.filter {
            listTxnType?.contains(it.txnType)==true && listTxnStatus?.contains(it.txnStatus)==true && (it.tip?:0.00) > 0.00
        }?.count()?:0
    }

    fun getServiceCharge(listTxnType: List<TxnType>?= listOf(TxnType.FOOD_PURCHASE), listTxnStatus: List<TxnStatus>?= listOf(TxnStatus.APPROVED,TxnStatus.REFUNDED)) : Double {
        return list?.filter {
            listTxnType?.contains(it.txnType)==true && listTxnStatus?.contains(it.txnStatus)==true
        }?.sumOf {
            it.serviceCharge ?: 0.0
        }?:0.00
    }

    fun getServiceChargeCount(listTxnType: List<TxnType>?= listOf(TxnType.FOOD_PURCHASE), listTxnStatus: List<TxnStatus>?= listOf(TxnStatus.APPROVED,TxnStatus.REFUNDED)) : Int {
        return list?.filter {
            listTxnType?.contains(it.txnType)==true && listTxnStatus?.contains(it.txnStatus)==true && (it.serviceCharge?:0.00) > 0.00
        }?.count()?:0
    }

    fun getVAT(listTxnType: List<TxnType>?= listOf(TxnType.FOOD_PURCHASE), listTxnStatus: List<TxnStatus>?= listOf(TxnStatus.APPROVED,TxnStatus.REFUNDED)) : Double {
        return list?.filter {
            listTxnType?.contains(it.txnType)==true && listTxnStatus?.contains(it.txnStatus)==true
        }?.sumOf {
            it.VAT ?: 0.0
        }?:0.00
    }

    fun getVATCount(listTxnType: List<TxnType>?= listOf(TxnType.FOOD_PURCHASE), listTxnStatus: List<TxnStatus>?= listOf(TxnStatus.APPROVED,TxnStatus.REFUNDED)) : Int {
        return list?.filter {
            listTxnType?.contains(it.txnType)==true && listTxnStatus?.contains(it.txnStatus)==true && (it.VAT?:0.00) > 0.00
        }?.count()?:0
    }

    fun getCount(listTxnType: List<TxnType>?= listOf(TxnType.FOOD_PURCHASE), listTxnStatus: List<TxnStatus>?= listOf(TxnStatus.APPROVED,TxnStatus.REFUNDED)) : Int {
        return list?.filter {
            listTxnType?.contains(it.txnType)==true && listTxnStatus?.contains(it.txnStatus)==true
        }?.count()?:0
    }

    fun getPurchaseTotal() = getTotal()

    fun getRefundTotal() = getTotal(
        listTxnType = listOf(TxnType.FOODSTAMP_RETURN),
        listTxnStatus = listOf(
        TxnStatus.APPROVED
    ))

    fun getPurchaseCount() = getCount()

    fun getRefundCount() = getCount(
        listTxnType = listOf(TxnType.FOODSTAMP_RETURN),
        listTxnStatus = listOf(
            TxnStatus.APPROVED
        )
    )

    fun getNetTotal(): Double {
        return getPurchaseTotal() - getRefundTotal()
    }
}