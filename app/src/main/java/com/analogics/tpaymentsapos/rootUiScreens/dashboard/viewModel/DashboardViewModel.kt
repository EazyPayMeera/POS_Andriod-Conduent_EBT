package com.analogics.tpaymentsapos.rootUiScreens.dashboard.viewModel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.listeners.rootListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.model.error.ApiServiceError
import com.analogics.paymentservicecore.repository.apiService.ApiServiceRepository
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getCurrentDateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(private var apiServiceRepository:ApiServiceRepository, val txnDBRepository: TxnDBRepository)  : ViewModel() {
    private val _selectedButton = mutableStateOf<String?>(null)
    val selectedButton: State<String?> get() = _selectedButton



//    fun insertData(txnDtlsEntity: TxnDtlsEntity)=viewModelScope.launch {
//        txnDBRepository.insert(txnDtlsEntity)
//    }


    fun onButtonClick(text: String, onClick: () -> Unit, sharedViewModel: SharedViewModel) {
        _selectedButton.value = text
        onClick()
        val currentDateTime = getCurrentDateTime()
        val formattedDate = currentDateTime.substring(0, 10).replace("-", "") // Extracts "20241005"

        sharedViewModel.objPosConfig?.apply { BatchId = formattedDate }?.saveToPrefs()
    }

    fun navigateTo(navHostController: NavHostController, route: String) {
        navHostController.navigate(route)
    }

    fun clearTransData(sharedViewModel: SharedViewModel) {
        _selectedButton.value = false.toString()
        sharedViewModel.clearTransData()
    }

    fun initPaymentSDK(context: Context, coroutineScope: CoroutineScope, sharedViewModel: SharedViewModel) {
        if(sharedViewModel.objPosConfig?.isPaymentSDKInit!=true) {
            coroutineScope.launch {
                apiServiceRepository.initPaymentSDK(context, object :
                    IApiServiceResponseListener {
                    override fun onApiSuccess(result: Any) {
                        if (result == true) {
                            sharedViewModel.objPosConfig?.apply { isPaymentSDKInit = true }?.saveToPrefs()
                            Toast.makeText(
                                context,
                                R.string.emv_sdk_init_success,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else {
                            sharedViewModel.objPosConfig?.apply { isPaymentSDKInit = false }?.saveToPrefs()
                            Toast.makeText(
                                context,
                                R.string.emv_sdk_init_failure,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onApiError(apiServiceError: ApiServiceError) {
                        sharedViewModel.objPosConfig?.apply { isPaymentSDKInit = false }?.saveToPrefs()
                        Toast.makeText(context, R.string.emv_sdk_init_failure, Toast.LENGTH_SHORT)
                            .show()
                        Log.e("EMV_APP", apiServiceError.errorMessage)
                    }

                    override fun onDisplayProgress(
                        show: Boolean,
                        title: String?,
                        subTitle: String?,
                        message: String?
                    ) {
                        CustomDialogBuilder.SetProgressDialog(title = title, subtitle = subTitle, message = message)
                    }
                })
            }
        }
    }
}
