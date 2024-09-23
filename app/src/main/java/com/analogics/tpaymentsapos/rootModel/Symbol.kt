package com.analogics.tpaymentsapos.rootModel

class Symbol(val position: Position=Position.START, val type: Type=Type.CURRENCY, val currency: Currency=Currency.INR) {

    enum class Type {
        CURRENCY,
        PERCENT,
        NONE
    }

    enum class Currency {
        INR,
        USD
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
                }
            }

            Type.PERCENT -> "%%"
            Type.NONE -> ""
        }
    }
}

