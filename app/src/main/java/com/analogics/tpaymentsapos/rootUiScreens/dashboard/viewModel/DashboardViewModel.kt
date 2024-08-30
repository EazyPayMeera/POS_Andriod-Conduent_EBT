package com.analogics.tpaymentsapos.rootUiScreens.dashboard.viewModel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.compose.runtime.State
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewModelScope
import com.analogics.paymentservicecore.listeners.rootListener.IOnRootAppPaymentListener
import com.analogics.paymentservicecore.repository.paymentService.PaymentServiceRepository
import com.analogics.securityframework.database.dao.ITxnDao
import com.analogics.securityframework.database.databaseClient.AppDatabaseClient
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.securityframework.database.entity.TxnDtlsEntity
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(private var paymentServiceRepository:PaymentServiceRepository,val txnDBRepository: TxnDBRepository)  : ViewModel() {
    private val _selectedButton = mutableStateOf<String?>(null)
    val selectedButton: State<String?> get() = _selectedButton



//    fun insertData(txnDtlsEntity: TxnDtlsEntity)=viewModelScope.launch {
//        txnDBRepository.insert(txnDtlsEntity)
//    }


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
