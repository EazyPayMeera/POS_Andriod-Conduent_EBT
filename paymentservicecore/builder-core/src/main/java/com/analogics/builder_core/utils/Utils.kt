

package com.analogics.builder_core.utils

import android.util.Log
import com.analogics.builder_core.model.Symbol
import java.util.Locale
import kotlin.experimental.or
import kotlin.math.pow

fun transformToAmountDouble(amount: String, decimalPlaces: Int = 2): Double {
    return formatAmount(amount,decimalPlaces, Symbol(type = Symbol.Type.NONE),withSeparator=false).toDoubleOrNull()?:0.00
}

fun formatAmount(amount: Double, decimalPlaces: Int = 2, symbol: Symbol?=Symbol(), withSeparator: Boolean = true): String {
    return formatAmount("%.${decimalPlaces}f".format(amount),decimalPlaces,symbol,withSeparator)
}

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

fun Double?.toDecimalFormat(decimalPlaces: Int = 2, symbol: Symbol?=Symbol(type = Symbol.Type.NONE), withSeparator: Boolean = false): String
{
    return formatAmount(this?:0.00,decimalPlaces,symbol,withSeparator)
}

fun Double?.toCurrencyLong(decimalPlaces: Int = 2): Long
{
    return formatAmount(this?:0.00,decimalPlaces,Symbol(type = Symbol.Type.NONE),false).replace(",","").replace(".","").toLong()
}

fun String?.toCurrencyLong(decimalPlaces: Int = 2): Long
{
    return formatAmount(this?:"0.00",decimalPlaces,Symbol(type = Symbol.Type.NONE),false).replace(",","").replace(".","").toLong()
}

fun removeNonDigits(input: String): String {
    val re = Regex("[^0-9]")
    return re.replace(input, "")
}


fun Int?.toBcd() : Byte
{
    var bcd = this?:0
    return ((bcd + (bcd/10)*6)%255).toByte()
}
