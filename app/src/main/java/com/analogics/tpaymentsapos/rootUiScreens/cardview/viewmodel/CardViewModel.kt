package com.analogics.tpaymentsapos.rootUiScreens.cardview.viewmodel

import android.content.Context
import android.util.Log
import android.util.MutableBoolean
import androidx.compose.runtime.mutableStateOf
import android.util.MutableBoolean
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.listeners.rootListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.model.error.ApiServiceError
import com.analogics.paymentservicecore.repository.paymentService.ApiServiceRepository
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.listeners.rootListener.IOnRootAppPaymentListener
import com.analogics.paymentservicecore.logger.AppLogger
import com.analogics.paymentservicecore.model.error.PaymentServiceError
import com.analogics.paymentservicecore.repository.paymentService.PaymentServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.securityframework.database.entity.TxnEntity
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor(private  var paymentServiceRepository: ApiServiceRepository,var dbRepository: TxnDBRepository) : ViewModel() {

    var showProgress = mutableStateOf(false)

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
                IApiServiceResponseListener {
                override fun onApiSuccess(response: Any) {
                    if (response == true)
                        navigateToApprovalScreen(navHostController)
                    else
                        navigateToDeclinedScreen(navHostController)
                }

                override fun onApiError(apiServiceError: ApiServiceError) {
                    navigateToDeclinedScreen(navHostController)
                }

                override fun onDisplayProgress(
                    show: Boolean,
                    title: String?,
                    subTitle: String?,
                    message: String?
                ) {
                    showProgress.value = show
                    CustomDialogBuilder.SetProgressDialog(title = title, subtitle = subTitle, message = message)
                }

            })
        }
    }

    fun insertTxnData(objRootAppPaymentDetails: ObjRootAppPaymentDetails)=viewModelScope.launch{
        val json = Gson().toJson(objRootAppPaymentDetails) // Convert ObjRootAppPaymentDetails to JSON

        dbRepository.insertTxn(Gson().fromJson(json, TxnEntity::class.java))
        Log.d("password " +
                "record insert suc", Gson().fromJson(json, TxnEntity::class.java).toString())

    }
}