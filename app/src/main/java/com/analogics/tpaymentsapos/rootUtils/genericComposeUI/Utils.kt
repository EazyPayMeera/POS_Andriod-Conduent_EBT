

package com.analogics.tpaymentsapos.rootUtils.genericComposeUI

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.analogics.paymentservicecore.logger.AppLogger
import com.analogics.securityframework.database.entity.TxnEntity
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.google.gson.Gson
import kotlin.math.pow

fun calculateTax(amount: Double): Double {
    return amount * 0.15
}

fun calculateTip(amount: Double, tipPercentage: Int): Double {
    return amount * (tipPercentage / 100.0)
}


fun calculateTotalAmount(transactionAmount: Double, tipAmount: Double, sgstAmount: Double, igstAmount: Double): Double {
    return transactionAmount + tipAmount + sgstAmount + igstAmount
}

fun formatAmount(amount: Double, decimalPlaces: Int = 2, withSymbol: Boolean = true, withSeparator: Boolean = true): String {
    return formatAmount("%.${decimalPlaces}f".format(amount),decimalPlaces,withSymbol,withSeparator)
}

fun formatAmount(input: String, decimalPlaces: Int = 2, withSymbol: Boolean = true, withSeparator: Boolean = true): String {
    try {
        val amount  = removeNonDigits(input).take(12)
        val dAmount: Double = amount.toDoubleOrNull()?:0.00
        val currency : String = if(withSymbol) "₹" else ""
        val separator : String = if(withSeparator) "," else ""
        return "$currency %${separator}.${decimalPlaces}f".format(java.util.Locale.ENGLISH, dAmount/10.0.pow(decimalPlaces))
    } catch (e: Exception) {
        AppLogger.e(AppLogger.MODULE.APP_UI, e.message.toString())
    }
    return ""
}


fun calculateTip(amount: Double, tip: Double): Double {
    return amount * tip
}

fun removeNonDigits(input: String): String {
    val re = Regex("[^0-9]")
    return re.replace(input, "")
}

fun createAmountTransformation(): VisualTransformation {
    return object : VisualTransformation {
        override fun filter(text: AnnotatedString): TransformedText {
            // Format the text using your formatAmount function
            val formatted = formatAmount(text.text)

            // Define the offset mapping
            val offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int = formatted.length
                override fun transformedToOriginal(offset: Int): Int = text.length
            }

            // Return the TransformedText object
            return TransformedText(AnnotatedString(formatted), offsetMapping)
        }
    }

    fun convertToEntity(objRootAppPaymentDetails: ObjRootAppPaymentDetails): TxnEntity {
        val json = Gson().toJson(objRootAppPaymentDetails) // Convert ObjRootAppPaymentDetails to JSON
        return Gson().fromJson(json, TxnEntity::class.java) // Convert JSON to TxnEntity
    }

}