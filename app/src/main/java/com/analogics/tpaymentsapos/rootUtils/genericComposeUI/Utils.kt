package com.analogics.tpaymentsapos.rootUtils.genericComposeUI

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
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


fun formatAmountdouble(amount: Double): String {
    val decimalFormat = DecimalFormat("#.00")
    return decimalFormat.format(amount)
}

fun formatAmount(input: String): String {

    if (input.isEmpty()) {
        return "0.00"
    }
    val numericValue = input.toDouble()

    val doubleValue = numericValue / 100

    val format = DecimalFormat("#0.00")
    val formattedValue = format.format(doubleValue)

    val maxDigits = 12
    val totalDigits = formattedValue.replace(".", "").length

    return if (totalDigits <= maxDigits) {
        formattedValue
    } else {
        val truncatedValue = formattedValue.substring(0, maxDigits - 3) + ".00" // Adjust according to decimal precision
        truncatedValue
    }
}

fun calculateTip(amount: Double, tip: Double): Double {
    return amount * tip
}

fun createAmountTransformation(): VisualTransformation {
    return object : VisualTransformation {
        override fun filter(text: AnnotatedString): TransformedText {
            // Format the text using your formatAmount function
            val formatted = "₹${formatAmount(text.text)}"

            // Define the offset mapping
            val offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int = formatted.length
                override fun transformedToOriginal(offset: Int): Int = text.length
            }

            // Return the TransformedText object
            return TransformedText(AnnotatedString(formatted), offsetMapping)
        }
    }
}