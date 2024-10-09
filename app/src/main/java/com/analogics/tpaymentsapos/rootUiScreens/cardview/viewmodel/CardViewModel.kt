package com.analogics.tpaymentsapos.rootUiScreens.cardview.viewmodel

import android.content.Context
import android.util.Log
import android.util.MutableBoolean
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
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
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor(private  var paymentServiceRepository: PaymentServiceRepository,var dbRepository: TxnDBRepository) : ViewModel(),IOnRootAppPaymentListener {
    private val objRoot = MutableStateFlow(ObjRootAppPaymentDetails())
    var userApiErrorHolder = MutableStateFlow(PaymentServiceError())
    var isNavigation=MutableBoolean(false)

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

    fun insertTxnData(objRootAppPaymentDetails: ObjRootAppPaymentDetails)=viewModelScope.launch{
        val json = Gson().toJson(objRootAppPaymentDetails) // Convert ObjRootAppPaymentDetails to JSON

        dbRepository.insertTxn(Gson().fromJson(json, TxnEntity::class.java))
        Log.d("password " +
                "record insert suc", Gson().fromJson(json, TxnEntity::class.java).toString())

    }

    fun onPurchaseApi(objRootAppPaymentDetail: ObjRootAppPaymentDetails) {
        viewModelScope.launch {
            try {
                val requestDetails =
                    PaymentServiceUtils.objectToJsonString(objRootAppPaymentDetail)
                paymentServiceRepository.apiServicePurchase(
                    PaymentServiceUtils.jsonStringToObject<PaymentServiceTxnDetails>(requestDetails), this@CardViewModel)
            } catch (e: Exception) {
                AppLogger.d(AppLogger.MODULE.APP_UI, e.message ?: "")
            }
        }
    }
    override fun onPaymentSuccess(response: Any) {
        when (response) {
            is ObjRootAppPaymentDetails -> {
                objRoot.value = response
            }
        }
    }

    override fun onPaymentError(paymentError: PaymentServiceError) {
        Log.e("API Response", paymentError.errorMessage)
        userApiErrorHolder.value = paymentError
    }
}