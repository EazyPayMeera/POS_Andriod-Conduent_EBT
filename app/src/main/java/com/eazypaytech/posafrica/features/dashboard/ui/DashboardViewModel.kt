package com.eazypaytech.posafrica.features.dashboard.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.data.listeners.responseListener.IEmvServiceResponseListener
import com.analogics.paymentservicecore.data.model.emv.EmvServiceResult
import com.analogics.paymentservicecore.data.model.emv.TermConfig
import com.analogics.paymentservicecore.domain.repository.apiService.ApiServiceRepository
import com.analogics.paymentservicecore.domain.repository.emvService.EmvServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.eazypaytech.posafrica.R
import com.eazypaytech.posafrica.features.activity.ui.SharedViewModel
import com.eazypaytech.posafrica.features.dialogs.ui.CustomDialogBuilder
import com.eazypaytech.posafrica.core.navigation.routes.AppNavigationItems
import com.eazypaytech.posafrica.domain.model.ObjRootAppPaymentDetails
import com.eazypaytech.posafrica.core.utils.navigateAndClean
import com.eazypaytech.posafrica.core.utils.miscellaneous.PrinterUtils
import com.eazypaytech.posafrica.core.utils.miscellaneous.readAsset
import com.analogics.securityframework.data.repository.TxnDBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(private var apiServiceRepository: ApiServiceRepository, private var emvServiceRepository: EmvServiceRepository, val txnDBRepository: TxnDBRepository)  : ViewModel() {

    private val _isAdmin = MutableStateFlow<Boolean>(false)
    val isAdmin : StateFlow<Boolean> = _isAdmin
    var sharedViewModel : SharedViewModel? = null
    fun clearTransData(sharedViewModel: SharedViewModel) {
        sharedViewModel.clearTransData()
        checkIfAdmin(sharedViewModel)
    }

    fun reprintLast(
        context: Context,
        isCustomer: Boolean = false
    ) {
        viewModelScope.launch {
            txnDBRepository.fetchLastTransaction()?.let {
                PrinterUtils.printReceipt(context, PaymentServiceUtils.transformObject<ObjRootAppPaymentDetails>(it)?: ObjRootAppPaymentDetails(),isCustomer)
            }?:let {
                CustomDialogBuilder.Companion.composeAlertDialog(
                    title = context.resources.getString(R.string.printer_Alert),
                    subtitle = context.resources.getString(R.string.printer_no_record)
                )
            }
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
                        terminalIdentifier = sharedViewModel.objPosConfig?.procId,
                        merchantIdentifier = sharedViewModel.objPosConfig?.merchantId,
                    ),
                    aidConfig = readAsset(context, AppConstants.DEFAULT_EMV_CONFIG_FILE_PATH),
                    capKeys = readAsset(context, AppConstants.DEFAULT_EMV_CAP_KEY_FILE_PATH),
                    iEmvServiceResponseListener =  object :
                        IEmvServiceResponseListener {
                    override fun onEmvServiceResponse(response: Any) {
                        if (response is EmvServiceResult.InitResult && response.status == EmvServiceResult.InitStatus.SUCCESS) {
                                    sharedViewModel.objPosConfig?.apply { isPaymentSDKInit = true }
                                        ?.saveToPrefs()
                                    /*CustomDialogBuilder.composeAlertDialog(
                                        title = context.resources.getString(
                                            R.string.emv_sdk_init_title
                                        ),
                                        subtitle = context.resources.getString(R.string.emv_sdk_init_success)
                                    )*/
                            Log.d("Initialization", "Payment SDK Initialized")
                        }
                        else {
                            sharedViewModel.objPosConfig?.apply { isPaymentSDKInit = false }?.saveToPrefs()
                            //CustomDialogBuilder.composeAlertDialog(title = context.resources.getString(R.string.emv_sdk_init_title), subtitle = context.resources.getString(R.string.emv_sdk_init_failure))
                            Log.d("Initialization", "Payment SDK Initialization Failed")
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
                CustomDialogBuilder.Companion.composeAlertDialog(
                    title = navHostController.context.resources.getString(
                        R.string.restricted
                    ),
                    subtitle = navHostController.context.resources.getString(R.string.batch_open)
                )
            }
            else if (!isAdmin.value) {
                CustomDialogBuilder.Companion.composeAlertDialog(
                    title = navHostController.context.resources.getString(
                        R.string.restricted
                    ),
                    subtitle = navHostController.context.resources.getString(R.string.for_admin)
                )
            }
            else{
                CustomDialogBuilder.Companion.composeAlertDialog(
                    title = navHostController.context.getString(R.string.reactivate_device),
                    message = navHostController.context.getString(R.string.confirm_reactivate_device),
                    okBtnText = navHostController.context.getString(R.string.yes),
                    onOkClick = {
                        sharedViewModel.objPosConfig?.apply { isActivationDone = false; isLoggedIn = false; isOnboardingComplete = false; isPaymentSDKInit = false }
                            ?.saveToPrefs()
                        navHostController.navigateAndClean(AppNavigationItems.ActivationScreen.route)
                    },
                    cancelBtnText = navHostController.context.getString(R.string.cancel_no),
                )
            }
        }
    }

    fun checkIfAdmin(sharedViewModel: SharedViewModel) {
        viewModelScope.launch {
            sharedViewModel.objPosConfig?.loginId?.let {
                txnDBRepository.isAdmin(it).let {
                    _isAdmin.value = it
                }
            }
        }
    }


}