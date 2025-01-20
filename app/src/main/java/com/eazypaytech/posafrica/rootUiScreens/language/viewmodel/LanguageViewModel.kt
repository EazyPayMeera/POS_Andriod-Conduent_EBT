package com.eazypaytech.posafrica.rootUiScreens.language.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.eazypaytech.posafrica.rootModel.UiLanguage
import com.eazypaytech.posafrica.rootModel.toUiLanguage
import com.eazypaytech.posafrica.rootUiScreens.activity.SharedViewModel
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.setUiLanguage
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