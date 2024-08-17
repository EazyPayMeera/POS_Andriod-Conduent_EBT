package com.analogics.tpaymentsapos.rootUiScreens.splash.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreenViewModel: ViewModel() {


    fun onSplashScreenFinished(onNavigate: () -> Unit) {
        viewModelScope.launch {
            delay(3000L) //  delay for splash screen
            onNavigate()
        }
    }
}