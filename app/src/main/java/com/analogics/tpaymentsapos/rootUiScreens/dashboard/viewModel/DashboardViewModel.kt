package com.analogics.tpaymentsapos.rootUiScreens.dashboard.viewModel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.compose.runtime.State
import com.analogics.paymentservicecore.listeners.rootListener.IOnRootAppPaymentListener
import com.analogics.paymentservicecore.repository.paymentService.PaymentServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(private var paymentServiceRepository:PaymentServiceRepository)  : ViewModel() {
    private val _selectedButton = mutableStateOf<String?>(null)
    val selectedButton: State<String?> get() = _selectedButton

    fun onButtonClick(text: String, onClick: () -> Unit) {
        _selectedButton.value = text
        onClick()
    }

    fun navigateTo(navHostController: NavHostController, route: String) {
        navHostController.navigate(route)
    }

    fun resetSelection() {
        _selectedButton.value = null
    }

    suspend fun initPaymentSDK(context: Context, iOnRootAppPaymentListener: IOnRootAppPaymentListener)
    {
        paymentServiceRepository.initPaymentSDK(context,iOnRootAppPaymentListener)
    }
}
