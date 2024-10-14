package com.analogics.tpaymentsapos.rootUiScreens.dashboard.viewModel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.listeners.responseListener.IEmvServiceResponseListener
import com.analogics.paymentservicecore.model.error.EmvServiceError
import com.analogics.paymentservicecore.repository.emvService.EmvServiceRepository
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.getCurrentDateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(private var emvServiceRepository:EmvServiceRepository, val txnDBRepository: TxnDBRepository)  : ViewModel() {
    private val _selectedButton = mutableStateOf<String?>(null)
    val selectedButton: State<String?> get() = _selectedButton
    var showProgress = mutableStateOf(false)
    var showAlert = mutableStateOf(false)



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

    fun initPaymentSDK(context: Context, sharedViewModel: SharedViewModel) {
        if(sharedViewModel.objPosConfig?.isPaymentSDKInit!=true) {
            viewModelScope.launch {
                emvServiceRepository.initPaymentSDK(context, object :
                    IEmvServiceResponseListener {
                    override fun onEmvSuccess(result: Any) {
                        if (result == true) {
                            sharedViewModel.objPosConfig?.apply { isPaymentSDKInit = true }?.saveToPrefs()
                            CustomDialogBuilder.composeAlertDialog(title = "SDK Initialization", subtitle = "SDK Initialization Successful")
                            showAlert.value = true
/*                            Toast.makeText(
                                context,
                                R.string.emv_sdk_init_success,
                                Toast.LENGTH_SHORT
                            ).show()*/
                        }
                        else {
                            sharedViewModel.objPosConfig?.apply { isPaymentSDKInit = false }?.saveToPrefs()
                            CustomDialogBuilder.composeAlertDialog(title = "SDK Initialization", subtitle = "SDK Initialization Failed")
                            showAlert.value = true
/*                            Toast.makeText(
                                context,
                                R.string.emv_sdk_init_failure,
                                Toast.LENGTH_SHORT
                            ).show()*/
                        }
                    }

                    override fun onEmvError(emvServiceError: EmvServiceError) {
                        sharedViewModel.objPosConfig?.apply { isPaymentSDKInit = false }?.saveToPrefs()
/*
                        Toast.makeText(context, R.string.emv_sdk_init_failure, Toast.LENGTH_SHORT)
                            .show()
*/
                        CustomDialogBuilder.composeAlertDialog(title = "SDK Initialization", subtitle = "SDK Initialization Failed")
                        showAlert.value = true
                        Log.e("EMV_APP", emvServiceError.errorMessage)
                    }

                    override fun onDisplayProgress(
                        show: Boolean,
                        title: String?,
                        subTitle: String?,
                        message: String?
                    ) {
                        //showProgress.value = show
                        //CustomDialogBuilder.setDialogText(title = title, subtitle = subTitle, message = message)
                        CustomDialogBuilder.composeProgressDialog(show = show, title = title, subtitle = subTitle, message = message)
                    }
                })
            }
        }
    }
}
