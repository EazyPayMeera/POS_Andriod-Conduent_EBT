package com.eazypaytech.pos.core.utils.miscellaneous

import com.analogics.paymentservicecore.data.model.TxnStatus
import com.analogics.paymentservicecore.data.model.TxnType
import com.eazypaytech.pos.domain.model.ObjRootAppPaymentDetails

class ReportBuilder(val list: List<ObjRootAppPaymentDetails>?) {

    /**
     * Calculates total transaction amount (ttlAmount) for filtered transactions.
     *
     * Filters data by transaction type and status, then sums total amount.
     *
     * @param listTxnType Transaction types to include
     * @param listTxnStatus Transaction statuses to include
     * @return Sum of total transaction amounts
     */
    fun getTotal(listTxnType: List<TxnType>?= listOf(TxnType.FOOD_PURCHASE), listTxnStatus: List<TxnStatus>?= listOf(TxnStatus.APPROVED,TxnStatus.REFUNDED)) : Double {
        return list?.filter {
            listTxnType?.contains(it.txnType)==true && listTxnStatus?.contains(it.txnStatus)==true
        }?.sumOf {
            it.ttlAmount ?: 0.0
        }?:0.00
    }

    /**
     * Calculates subtotal (txnAmount) for filtered transactions.
     *
     * @param listTxnType Transaction types to include
     * @param listTxnStatus Transaction statuses to include
     * @return Sum of transaction base amounts
     */
    fun getSubTotal(listTxnType: List<TxnType>?= listOf(TxnType.FOOD_PURCHASE), listTxnStatus: List<TxnStatus>?= listOf(TxnStatus.APPROVED,TxnStatus.REFUNDED)) : Double {
        return list?.filter {
            listTxnType?.contains(it.txnType)==true && listTxnStatus?.contains(it.txnStatus)==true
        }?.sumOf {
            it.txnAmount ?: 0.0
        }?:0.00
    }

    /**
     * Calculates total tip amount for filtered transactions.
     *
     * @param listTxnType Transaction types to include
     * @param listTxnStatus Transaction statuses to include
     * @return Total tip amount
     */
    fun getTip(listTxnType: List<TxnType>?= listOf(TxnType.FOOD_PURCHASE), listTxnStatus: List<TxnStatus>?= listOf(TxnStatus.APPROVED,TxnStatus.REFUNDED)) : Double {
        return list?.filter {
            listTxnType?.contains(it.txnType)==true && listTxnStatus?.contains(it.txnStatus)==true
        }?.sumOf {
            it.tip ?: 0.0
        }?:0.00
    }

    /**
     * Counts number of transactions that include a tip (> 0).
     *
     * @param listTxnType Transaction types to include
     * @param listTxnStatus Transaction statuses to include
     * @return Number of tip-enabled transactions
     */
    fun getTipCount(listTxnType: List<TxnType>?= listOf(TxnType.FOOD_PURCHASE), listTxnStatus: List<TxnStatus>?= listOf(TxnStatus.APPROVED,TxnStatus.REFUNDED)) : Int {
        return list?.filter {
            listTxnType?.contains(it.txnType)==true && listTxnStatus?.contains(it.txnStatus)==true && (it.tip?:0.00) > 0.00
        }?.count()?:0
    }

    /**
     * Calculates total service charge for filtered transactions.
     *
     * @param listTxnType Transaction types to include
     * @param listTxnStatus Transaction statuses to include
     * @return Total service charge amount
     */
    fun getServiceCharge(listTxnType: List<TxnType>?= listOf(TxnType.FOOD_PURCHASE), listTxnStatus: List<TxnStatus>?= listOf(TxnStatus.APPROVED,TxnStatus.REFUNDED)) : Double {
        return list?.filter {
            listTxnType?.contains(it.txnType)==true && listTxnStatus?.contains(it.txnStatus)==true
        }?.sumOf {
            it.serviceCharge ?: 0.0
        }?:0.00
    }

    /**
     * Counts transactions that include a service charge (> 0).
     *
     * @param listTxnType Transaction types to include
     * @param listTxnStatus Transaction statuses to include
     * @return Number of service charge transactions
     */
    fun getServiceChargeCount(listTxnType: List<TxnType>?= listOf(TxnType.FOOD_PURCHASE), listTxnStatus: List<TxnStatus>?= listOf(TxnStatus.APPROVED,TxnStatus.REFUNDED)) : Int {
        return list?.filter {
            listTxnType?.contains(it.txnType)==true && listTxnStatus?.contains(it.txnStatus)==true && (it.serviceCharge?:0.00) > 0.00
        }?.count()?:0
    }

    /**
     * Calculates total VAT amount for filtered transactions.
     *
     * @param listTxnType Transaction types to include
     * @param listTxnStatus Transaction statuses to include
     * @return Total VAT amount
     */
    fun getVAT(listTxnType: List<TxnType>?= listOf(TxnType.FOOD_PURCHASE), listTxnStatus: List<TxnStatus>?= listOf(TxnStatus.APPROVED,TxnStatus.REFUNDED)) : Double {
        return list?.filter {
            listTxnType?.contains(it.txnType)==true && listTxnStatus?.contains(it.txnStatus)==true
        }?.sumOf {
            it.VAT ?: 0.0
        }?:0.00
    }

    /**
     * Counts transactions that include VAT (> 0).
     *
     * @param listTxnType Transaction types to include
     * @param listTxnStatus Transaction statuses to include
     * @return Number of VAT applicable transactions
     */
    fun getVATCount(listTxnType: List<TxnType>?= listOf(TxnType.FOOD_PURCHASE), listTxnStatus: List<TxnStatus>?= listOf(TxnStatus.APPROVED,TxnStatus.REFUNDED)) : Int {
        return list?.filter {
            listTxnType?.contains(it.txnType)==true && listTxnStatus?.contains(it.txnStatus)==true && (it.VAT?:0.00) > 0.00
        }?.count()?:0
    }

    /**
     * Counts total number of transactions matching filters.
     *
     * @param listTxnType Transaction types to include
     * @param listTxnStatus Transaction statuses to include
     * @return Number of matching transactions
     */
    fun getCount(listTxnType: List<TxnType>?= listOf(TxnType.FOOD_PURCHASE), listTxnStatus: List<TxnStatus>?= listOf(TxnStatus.APPROVED,TxnStatus.REFUNDED)) : Int {
        return list?.filter {
            listTxnType?.contains(it.txnType)==true && listTxnStatus?.contains(it.txnStatus)==true
        }?.count()?:0
    }

    /**
     * Returns total purchase amount using default filters.
     *
     * @return Total purchase amount
     */
    fun getPurchaseTotal() = getTotal()

    /**
     * Returns total refund amount for FOODSTAMP_RETURN transactions.
     *
     * @return Total refund amount
     */
    fun getRefundTotal() = getTotal(
        listTxnType = listOf(TxnType.FOODSTAMP_RETURN),
        listTxnStatus = listOf(
        TxnStatus.APPROVED
    ))

    /**
     * Returns count of refund transactions (FOODSTAMP_RETURN + APPROVED).
     *
     * @return Number of refund transactions
     */
    fun getPurchaseCount() = getCount()

    /**
     * Returns count of refund transactions (FOODSTAMP_RETURN + APPROVED).
     *
     * @return Number of refund transactions
     */
    fun getRefundCount() = getCount(
        listTxnType = listOf(TxnType.FOODSTAMP_RETURN),
        listTxnStatus = listOf(
            TxnStatus.APPROVED
        )
    )

    /**
     * Calculates net total amount (Purchase - Refund).
     *
     * @return Net transaction total
     */
    fun getNetTotal(): Double {
        return getPurchaseTotal() - getRefundTotal()
    }
}