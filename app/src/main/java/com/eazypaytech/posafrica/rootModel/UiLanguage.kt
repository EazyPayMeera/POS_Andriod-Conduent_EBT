package com.eazypaytech.posafrica.rootModel

enum class UiLanguage(val languageCode: String) {
        ENGLISH("en"),
        HINDI("hi")
}

fun String.toUiLanguage() : UiLanguage
{
    return when(this) {
        UiLanguage.ENGLISH.languageCode -> UiLanguage.ENGLISH
        UiLanguage.HINDI.languageCode -> UiLanguage.HINDI
        else -> UiLanguage.ENGLISH
    }
}

