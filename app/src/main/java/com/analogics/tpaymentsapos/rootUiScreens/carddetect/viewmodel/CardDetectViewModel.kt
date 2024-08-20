package com.analogics.tpaymentsapos.rootUiScreens.carddetect.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.listeners.responseListener.IResultProviderListener
import com.analogics.paymentservicecore.repository.paymentService.PaymentServiceRepository
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CardDetectViewModel : ViewModel() {

    // Function to handle the delay and navigation
    fun navigateAfterDelay(navHostController: NavHostController) {
        viewModelScope.launch {
            delay(2000) // Delay for 2 seconds (2000 milliseconds)
            navHostController.navigate(AppNavigationItems.PinScreen.route) // Navigate to the desired screen
        }
    }

    suspend fun startPayment(context: Context, iResultProviderListener: IResultProviderListener)
    {
        PaymentServiceRepository().startPayment(context,iResultProviderListener)
    }
}
