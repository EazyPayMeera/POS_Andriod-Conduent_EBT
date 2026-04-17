package com.eazypaytech.posafrica.features.language.ui

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.eazypaytech.posafrica.features.activity.ui.SharedViewModel
import com.eazypaytech.posafrica.core.utils.setUiLanguage
import com.eazypaytech.posafrica.core.utils.language.UiLanguage
import com.eazypaytech.posafrica.core.utils.language.toUiLanguage
import javax.inject.Inject

class LanguageViewModel @Inject constructor() : ViewModel() {
    var uiLanguage = mutableStateOf(AppConstants.DEFAULT_UI_LANGUAGE_CODE.toUiLanguage())

    fun onLoad(sharedViewModel: SharedViewModel) {
        sharedViewModel.objPosConfig?.language?.toUiLanguage()?.let { uiLanguage.value = it }
    }

    fun onLanguageChange(language: UiLanguage, sharedViewModel: SharedViewModel, context: Context) {
        uiLanguage.value = language
        sharedViewModel.objPosConfig?.apply { this.language = uiLanguage.value.languageCode }?.saveToPrefs()
        setUiLanguage(context, language)
    }
}