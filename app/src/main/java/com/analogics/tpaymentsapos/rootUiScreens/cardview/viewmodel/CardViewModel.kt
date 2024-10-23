package com.analogics.tpaymentsapos.rootUiScreens.cardview.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.constants.AppConstants
import com.analogics.paymentservicecore.listeners.responseListener.IEmvServiceResponseListener
import com.analogics.paymentservicecore.model.emv.CardCheckMode
import com.analogics.paymentservicecore.model.emv.EmvServiceResult
import com.analogics.paymentservicecore.model.emv.TransConfig
import com.analogics.paymentservicecore.models.toEmvTransType
import com.analogics.paymentservicecore.repository.emvService.EmvServiceRepository
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.securityframework.database.entity.TxnEntity
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.BaseConstant
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.toAmountFormat
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.toDecimalFormat
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor(private  var emvServiceRepository: EmvServiceRepository, var dbRepository: TxnDBRepository) : ViewModel() {

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

    fun onUpiClick(navHostController: NavHostController) {
        navHostController.navigate(AppNavigationItems. BarcodeScreen.route)
        viewModelScope.launch {
            emvServiceRepository.abortPayment()
        }
    }

    fun onCancelClick(navHostController: NavHostController) {
        navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
        viewModelScope.launch {
            emvServiceRepository.abortPayment()
        }
    }

    fun getTransConfig(objRootAppPaymentDetails: ObjRootAppPaymentDetails) : TransConfig
    {
        return TransConfig(
            amount = objRootAppPaymentDetails.ttlAmount.toDecimalFormat(),
            cashbackAmount = objRootAppPaymentDetails.cashback.toDecimalFormat(),
            currencyCode = objRootAppPaymentDetails.txnCurrencyCode?: AppConstants.DEFAULT_CURRENCY_CODE,
            transactionType = objRootAppPaymentDetails.txnType?.toEmvTransType().toString(),
            cardCheckMode = CardCheckMode.SWIPE_OR_INSERT_OR_TAP,
            cardCheckTimeout = AppConstants.CARD_CHECK_TIMEOUT_S.toString(),
            supportDRL = false
        )
    }

    fun startPayment(
        context: Context,
        objRootAppPaymentDetails: ObjRootAppPaymentDetails,
        navHostController: NavHostController
    ) {
        viewModelScope.launch {
            emvServiceRepository.startPayment(context,
                transConfig = getTransConfig(objRootAppPaymentDetails),
                object :
                IEmvServiceResponseListener {
                override fun onEmvServiceResponse(response: Any) {
                    if (response is EmvServiceResult &&
                        (response.status == EmvServiceResult.TransStatus.APPROVED_ONLINE ||
                                response.status == EmvServiceResult.TransStatus.APPROVED_OFFLINE))
                        navigateToApprovalScreen(navHostController)
                    else
                        navigateToDeclinedScreen(navHostController)
                }

                override fun onEmvServiceDisplayProgress(
                    show: Boolean,
                    title: String?,
                    subTitle: String?,
                    message: String?
                ) {
                    CustomDialogBuilder.composeProgressDialog(show = show, title = title, subtitle = subTitle, message = message)
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