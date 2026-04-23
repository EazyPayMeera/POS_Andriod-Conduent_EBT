package com.analogics.builder_core.utils

import android.util.Log
import com.analogics.builder_core.data.model.Symbol
import java.util.Locale
import kotlin.math.pow

/**
 * Converts formatted amount string into Double.
 *
 * Example:
 * "1234" → 12.34 (if decimalPlaces = 2)
 *
 * @param amount Raw numeric string (can include symbols)
 * @param decimalPlaces Number of decimal digits
 * @return Parsed amount as Double
 */
fun transformToAmountDouble(amount: String, decimalPlaces: Int = 2): Double {
    return formatAmount(amount,decimalPlaces, Symbol(type = Symbol.Type.NONE),withSeparator=false).toDoubleOrNull()?:0.00
}

/**
 * Formats Double amount into currency string.
 *
 * @param amount Input amount as Double
 * @param decimalPlaces Number of decimal digits
 * @param symbol Currency symbol configuration
 * @param withSeparator Enable thousand separator
 * @return Formatted currency string
 */
fun formatAmount(amount: Double, decimalPlaces: Int = 2, symbol: Symbol?=Symbol(), withSeparator: Boolean = true): String {
    return formatAmount("%.${decimalPlaces}f".format(amount),decimalPlaces,symbol,withSeparator)
}

/**
 * Formats raw string amount into currency format.
 *
 * Features:
 * - Removes non-digit characters
 * - Applies decimal shifting
 * - Adds currency symbol (optional)
 * - Supports thousand separators
 *
 * Example:
 * Input: "1234"
 * Output: "12.34"
 *
 * @param input Raw amount string
 * @param decimalPlaces Number of decimal digits
 * @param symbol Currency symbol configuration
 * @param withSeparator Enable thousand separator
 * @return Formatted string
 */
fun formatAmount(input: String, decimalPlaces: Int = 2, symbol: Symbol?=Symbol(), withSeparator: Boolean = true): String {
    try {
        val amount  = removeNonDigits(input).take(12)
        val dAmount: Double = amount.toDoubleOrNull()?:0.00
        val currency = symbol?.get()?:""
        val separator : String = if(withSeparator) "," else ""
        val spaceChar : String = if(symbol?.noSpace==true || symbol?.type==Symbol.Type.NONE) "" else " "
        return when(symbol?.position) {
            Symbol.Position.START -> "$currency$spaceChar%${separator}.${decimalPlaces}f".format(Locale.ENGLISH, dAmount / 10.0.pow(decimalPlaces))
            Symbol.Position.END -> "%.${decimalPlaces}f$spaceChar$currency".format(Locale.ENGLISH, dAmount / 10.0.pow(decimalPlaces))
            else -> "%.${decimalPlaces}f".format(Locale.ENGLISH, dAmount / 10.0.pow(decimalPlaces))
        }
    } catch (e: Exception) {
        Log.e("BUILDER_CORE", e.message.toString())
    }
    return ""
}

/**
 * Extension: Formats nullable Double into currency string.
 *
 * @param decimalPlaces Number of decimal digits
 * @param symbol Currency symbol configuration
 * @param withSeparator Enable thousand separator
 */
fun Double?.toDecimalFormat(decimalPlaces: Int = 2, symbol: Symbol?=Symbol(type = Symbol.Type.NONE), withSeparator: Boolean = false): String
{
    return formatAmount(this?:0.00,decimalPlaces,symbol,withSeparator)
}

/**
 * Converts Double amount into ISO8583-compatible Long.
 *
 * Example:
 * 12.34 → 1234
 *
 * @param decimalPlaces Number of decimal digits
 * @return Amount in smallest currency unit
 */
fun Double?.toCurrencyLong(decimalPlaces: Int = 2): Long
{
    return formatAmount(this?:0.00,decimalPlaces,Symbol(type = Symbol.Type.NONE),false).replace(",","").replace(".","").toLong()
}

/**
 * Converts String amount into ISO8583-compatible Long.
 *
 * Example:
 * "12.34" → 1234
 *
 * @param decimalPlaces Number of decimal digits
 * @return Amount in smallest currency unit
 */
fun String?.toCurrencyLong(decimalPlaces: Int = 2): Long
{
    return formatAmount(this?:"0.00",decimalPlaces,Symbol(type = Symbol.Type.NONE),false).replace(",","").replace(".","").toLong()
}

/**
 * Removes all non-digit characters from input string.
 *
 * Example:
 * "12.34$" → "1234"
 *
 * @param input Raw string
 * @return Digits-only string
 */
fun removeNonDigits(input: String): String {
    val re = Regex("[^0-9]")
    return re.replace(input, "")
}

/**
 * Converts integer into BCD (Binary-Coded Decimal) byte.
 *
 * Commonly used in ISO8583 encoding.
 *
 * Example:
 * 12 → 0x12
 *
 * @return BCD encoded byte
 */
fun Int?.toBcd() : Byte
{
    var bcd = this?:0
    return ((bcd + (bcd/10)*6)%255).toByte()
}
