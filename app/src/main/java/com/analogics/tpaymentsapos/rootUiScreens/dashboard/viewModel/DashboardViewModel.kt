package com.analogics.tpaymentsapos.rootUiScreens.dashboard.viewModel

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.compose.runtime.State
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewModelScope
import com.analogics.paymentservicecore.listeners.rootListener.IOnRootAppPaymentListener
import com.analogics.paymentservicecore.model.error.PaymentServiceError
import com.analogics.paymentservicecore.repository.paymentService.PaymentServiceRepository
import com.analogics.securityframework.database.dao.ITxnDao
import com.analogics.securityframework.database.databaseClient.AppDatabaseClient
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.securityframework.database.entity.TxnDtlsEntity
import com.analogics.tpaymentsapos.R
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.schedule

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

    fun initPaymentSDK(context: Context, coroutineScope: CoroutineScope)
    {
            coroutineScope.launch {
                paymentServiceRepository.initPaymentSDK(context, object :
                    IOnRootAppPaymentListener {
                    override fun onPaymentSuccess(result: Any) {
                        if (result?.equals(true) == true)
                            Toast.makeText(
                                context,
                                R.string.emv_sdk_init_success,
                                Toast.LENGTH_SHORT
                            ).show()
                        else
                            Toast.makeText(
                                context,
                                R.string.emv_sdk_init_failure,
                                Toast.LENGTH_SHORT
                            ).show()
                    }

                    override fun onPaymentError(tError: PaymentServiceError) {
                        Toast.makeText(context, R.string.emv_sdk_init_failure, Toast.LENGTH_SHORT)
                            .show()
                        Log.e("EMV_APP", tError.errorMessage)
                    }
                })
            }
        }
}
