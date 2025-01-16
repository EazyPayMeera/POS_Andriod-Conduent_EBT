package com.analogics.tpaymentsapos.rootUiScreens.dashboard.viewModel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.listeners.responseListener.IEmvServiceResponseListener
import com.analogics.paymentservicecore.model.emv.EmvServiceResult
import com.analogics.paymentservicecore.model.emv.TermConfig
import com.analogics.paymentservicecore.repository.emvService.EmvServiceRepository
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.securityframework.database.entity.TxnEntity
import com.analogics.tpaymentsapos.R
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import com.analogics.tpaymentsapos.rootUtils.miscellaneous.readAsset
import dagger.hilt.android.lifecycle.HiltViewModel
import fetchLastTransactions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(private var emvServiceRepository:EmvServiceRepository, val txnDBRepository: TxnDBRepository)  : ViewModel() {
    private val _selectedButton = mutableStateOf<String?>(null)
    val selectedButton: State<String?> get() = _selectedButton

    private val _lastTransactionList = MutableStateFlow<List<ObjRootAppPaymentDetails>>(emptyList())
    val lastTransactionList: StateFlow<List<ObjRootAppPaymentDetails>> = _lastTransactionList



    fun onButtonClick(text: String, onClick: () -> Unit) {
        _selectedButton.value = text
        onClick()
    }

    fun navigateTo(navHostController: NavHostController, route: String) {
        navHostController.navigate(route)
    }

    fun clearTransData(sharedViewModel: SharedViewModel) {
        _selectedButton.value = false.toString()
        sharedViewModel.clearTransData()
    }


    fun fetchLastTransactions(
        sharedViewModel: SharedViewModel,
        context: Context,
        customer: Boolean = false
    ) {
        Log.d("Fetch Last Transaction","In DashBoard View Model")
        viewModelScope.launch {
            // Inside this coroutine, you can now call suspend functions
            fetchLastTransactions(
                sharedViewModel,
                context,
                customer,
                txnDBRepository
            )
        }
    }


    fun setInvoiceNumber(sharedViewModel: SharedViewModel)
    {
        viewModelScope.launch{
            txnDBRepository.getLastInvoiceNumber().let {
                sharedViewModel.objRootAppPaymentDetail.invoiceNo = (it+1).toString()
            }
        }
    }

    fun initPaymentSDK(context: Context, sharedViewModel: SharedViewModel) {
        if(sharedViewModel.objPosConfig?.isPaymentSDKInit!=true) {
            viewModelScope.launch {
                emvServiceRepository.initPaymentSDK(
                    termConfig = TermConfig(
                        terminalIdentifier = sharedViewModel.objPosConfig?.terminalId,
                        merchantIdentifier = sharedViewModel.objPosConfig?.merchantId
                    ),
                    aidConfig = readAsset(context, AppConstants.DEFAULT_EMV_CONFIG_FILE_PATH),
                    capKeys = readAsset(context, AppConstants.DEFAULT_EMV_CAP_KEY_FILE_PATH),
                    iEmvServiceResponseListener =  object :
                    IEmvServiceResponseListener {
                    override fun onEmvServiceResponse(response: Any) {
                        if (response is EmvServiceResult.InitResult && response.status == EmvServiceResult.InitStatus.SUCCESS) {
                                    sharedViewModel.objPosConfig?.apply { isPaymentSDKInit = true }
                                        ?.saveToPrefs()
                                    CustomDialogBuilder.composeAlertDialog(
                                        title = context.resources.getString(
                                            R.string.emv_sdk_init_title
                                        ),
                                        subtitle = context.resources.getString(R.string.emv_sdk_init_success)
                                    )
                        }
                        else {
                            sharedViewModel.objPosConfig?.apply { isPaymentSDKInit = false }?.saveToPrefs()
                            CustomDialogBuilder.composeAlertDialog(title = context.resources.getString(R.string.emv_sdk_init_title), subtitle = context.resources.getString(R.string.emv_sdk_init_failure))
                       }
                    }

                    override fun onEmvServiceDisplayMessage(
                        displayMsgId: EmvServiceResult.DisplayMsgId
                    ) {
                        //CustomDialogBuilder.composeProgressDialog(show = show, title = title, subtitle = subTitle, message = message)
                    }
                })
            }
        }
    }

    fun onReactivate(navHostController : NavHostController, sharedViewModel: SharedViewModel) {
        viewModelScope.launch {
            if (txnDBRepository.isBatchOpen()) {
                CustomDialogBuilder.composeAlertDialog(
                    title = navHostController.context.resources.getString(
                        R.string.restricted
                    ),
                    subtitle = navHostController.context.resources.getString(R.string.batch_open)
                )
            }
            else{
                CustomDialogBuilder.composeAlertDialog(
                    title = navHostController.context.getString(R.string.reactivate_device),
                    message = navHostController.context.getString(R.string.confirm_reactivate_device),
                    okBtnText = navHostController.context.getString(R.string.yes),
                    onOkClick = {
                        sharedViewModel.objPosConfig?.apply { isActivationDone = false; isLoggedIn = false; isPaymentSDKInit = false }
                            ?.saveToPrefs()
                        navHostController.navigateAndClean(AppNavigationItems.ActivationScreen.route)
                    },
                    cancelBtnText = navHostController.context.getString(R.string.cancel_no),
                )
            }
        }
    }
}
