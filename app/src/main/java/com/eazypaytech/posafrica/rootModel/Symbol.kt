package com.eazypaytech.posafrica.rootModel

object DefaultSymbol {
    var currency: Symbol.Currency = Symbol.Currency.USD
    var position: Symbol.Position = Symbol.Position.START
    var noSpace : Boolean = false
}

class Symbol(val position: Position=DefaultSymbol.position, val type: Type=Type.CURRENCY, val currency: Currency=DefaultSymbol.currency, val noSpace : Boolean = DefaultSymbol.noSpace) {

    enum class Type {
        CURRENCY,
        PERCENT,
        NONE
    }

    enum class Currency {
        INR,
        USD,
        ZAR
    }

    enum class Position {
        START,
        END
    }

    fun get(): String {
        return when (type) {
            Type.CURRENCY -> {
                when (currency) {
                    Currency.INR -> "₹"
                    Currency.USD -> "$"
                    Currency.ZAR -> "R"
                }
            }

            Type.PERCENT -> "%%"
            Type.NONE -> ""
        }
    }


}

