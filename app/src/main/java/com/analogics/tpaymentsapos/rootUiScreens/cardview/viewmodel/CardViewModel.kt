package com.analogics.tpaymentsapos.rootUiScreens.cardview.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.listeners.responseListener.IResultProviderListener
import com.analogics.paymentservicecore.repository.paymentService.PaymentServiceRepository
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CardViewModel : ViewModel() {

    fun navigateToApprovalScreen(navHostController: NavHostController) {
        viewModelScope.launch {
            navHostController.navigate(AppNavigationItems.ApprovedScreen.route) // Navigate to the desired screen
        }
    }

    fun navigateToDeclinedScreen(navHostController: NavHostController) {
        viewModelScope.launch {
            navHostController.navigate(AppNavigationItems.DeclineScreen.route) // Navigate to the desired screen
        }
    }

    suspend fun startPayment(context: Context, iResultProviderListener: IResultProviderListener)
    {
        PaymentServiceRepository().startPayment(context,iResultProviderListener)
    }
}