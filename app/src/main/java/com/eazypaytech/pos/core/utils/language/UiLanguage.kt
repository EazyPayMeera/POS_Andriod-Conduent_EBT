package com.eazypaytech.pos.core.utils.language

/**
 * Represents supported UI languages in the application.
 *
 * Each enum value maps to a standard language code used for locale switching.
 * This is used to control app-wide language selection and localization.
 *
 * Supported languages:
 * - ENGLISH ("en")
 * - HINDI ("hi")
 *
 * @property languageCode ISO-style language code used for locale configuration
 */
enum class UiLanguage(val languageCode: String) {
        ENGLISH("en"),
        HINDI("hi")
}

/**
 * Converts a language code string into a corresponding UiLanguage enum.
 *
 * If the input does not match any supported language code,
 * the default fallback is ENGLISH.
 *
 * Example:
 * "en" -> UiLanguage.ENGLISH
 * "hi" -> UiLanguage.HINDI
 *
 * @receiver String language code (e.g., "en", "hi")
 * @return Matching UiLanguage enum, or ENGLISH as default fallback
 */
fun String.toUiLanguage() : UiLanguage
{
    return when(this) {
        UiLanguage.ENGLISH.languageCode -> UiLanguage.ENGLISH
        UiLanguage.HINDI.languageCode -> UiLanguage.HINDI
        else -> UiLanguage.ENGLISH
    }
}

