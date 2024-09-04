package com.analogics.tpaymentsapos.rootUiScreens.cardview.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.listeners.rootListener.IOnRootAppPaymentListener
import com.analogics.paymentservicecore.model.error.PaymentServiceError
import com.analogics.paymentservicecore.repository.paymentService.PaymentServiceRepository
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor(private  var paymentServiceRepository: PaymentServiceRepository) : ViewModel() {

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

    fun startPayment(context: Context, navHostController: NavHostController) {
        viewModelScope.launch {
            paymentServiceRepository.startPayment(context, object :
                IOnRootAppPaymentListener {
                override fun onPaymentSuccess(response: Any) {
                    if (response == true)
                        navigateToApprovalScreen(navHostController)
                    else
                        navigateToDeclinedScreen(navHostController)
                }

                override fun onPaymentError(tError: PaymentServiceError) {
                    navigateToDeclinedScreen(navHostController)
                }
            })
        }
    }
}