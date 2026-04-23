package com.eazypaytech.pos.features.language.ui

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.eazypaytech.pos.features.activity.ui.SharedViewModel
import com.eazypaytech.pos.core.utils.setUiLanguage
import com.eazypaytech.pos.core.utils.language.UiLanguage
import com.eazypaytech.pos.core.utils.language.toUiLanguage
import javax.inject.Inject

class LanguageViewModel @Inject constructor() : ViewModel() {
    var uiLanguage = mutableStateOf(AppConstants.DEFAULT_UI_LANGUAGE_CODE.toUiLanguage())

    /**
     * Initializes the selected UI language from stored configuration.
     *
     * Behavior:
     * - Reads language code from POS config
     * - Converts it to UI language model
     * - Updates Compose state if available
     *
     * @param sharedViewModel Shared ViewModel containing POS configuration
     */
    fun onLoad(sharedViewModel: SharedViewModel) {
        sharedViewModel.objPosConfig?.language?.toUiLanguage()?.let { uiLanguage.value = it }
    }

    /**
     * Handles language selection change and applies it across the app.
     *
     * Behavior:
     * - Updates UI state with selected language
     * - Persists selected language in configuration
     * - Applies language change to app context
     *
     * @param language Selected UI language
     * @param sharedViewModel Shared ViewModel for saving configuration
     * @param context Application context used to update locale
     */
    fun onLanguageChange(language: UiLanguage, sharedViewModel: SharedViewModel, context: Context) {
        uiLanguage.value = language
        sharedViewModel.objPosConfig?.apply { this.language = uiLanguage.value.languageCode }?.saveToPrefs()
        setUiLanguage(context, language)
    }
}