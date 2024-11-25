package com.analogics.tpaymentsapos.rootUiScreens.language.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.tpaymentsapos.rootModel.UiLanguage
import com.analogics.tpaymentsapos.rootModel.toUiLanguage
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import javax.inject.Inject

class LanguageViewModel @Inject constructor() : ViewModel() {
    var uiLanguage = mutableStateOf(AppConstants.DEFAULT_UI_LANGUAGE_CODE.toUiLanguage())

    fun onLoad(sharedViewModel: SharedViewModel) {
        sharedViewModel.objPosConfig?.language?.toUiLanguage()?.let { uiLanguage.value = it }
    }

    fun onLanguageChange(language: UiLanguage, sharedViewModel: SharedViewModel, context: Context) {
        uiLanguage.value = language
        sharedViewModel.objPosConfig?.apply { this.language = uiLanguage.value.languageCode }?.saveToPrefs()
        updateResources(context, language)
    }

    private fun updateResources(context: Context, language: UiLanguage) {
        val config = context.resources.configuration
        val locale = java.util.Locale(language.languageCode)
        java.util.Locale.setDefault(locale)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

}