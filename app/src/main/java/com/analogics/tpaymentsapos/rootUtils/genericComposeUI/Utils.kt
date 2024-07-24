package com.analogics.tpaymentsapos.rootUtils.genericComposeUI

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.DecimalFormat

fun calculateTax(amount: Double): Double {
    return amount * 0.15
}

fun calculateTip(amount: Double, tipPercentage: Int): Double {
    return amount * (tipPercentage / 100.0)
}


fun calculateTotalAmount(transactionAmount: Double, tipAmount: Double, sgstAmount: Double, igstAmount: Double): Double {
    return transactionAmount + tipAmount + sgstAmount + igstAmount
}


fun formatAmount(amount: Double): String {
    val decimalFormat = DecimalFormat("#.00")
    return decimalFormat.format(amount)
}

fun formatAmount(input: String): String {
    return if (input.isEmpty()) {
        "0.00"
    } else {
        val doubleValue = input.toDouble() / 100
        DecimalFormat("#0.00").format(doubleValue)
    }
}

fun createAmountTransformation(): VisualTransformation {
    return object : VisualTransformation {
        override fun filter(text: AnnotatedString): TransformedText {
            val formatted = formatAmount(text.text)
            val offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int = formatted.length
                override fun transformedToOriginal(offset: Int): Int = text.length
            }
            return TransformedText(AnnotatedString(formatted), offsetMapping)
        }
    }
}