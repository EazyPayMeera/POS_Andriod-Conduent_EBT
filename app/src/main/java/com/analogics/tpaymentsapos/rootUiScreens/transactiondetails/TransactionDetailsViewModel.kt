package com.analogics.tpaymentsapos.rootUiScreens.transactiondetails

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.analogics.paymentservicecore.listeners.responseListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.model.error.ApiServiceError
import com.analogics.paymentservicecore.repository.apiService.ApiServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUiScreens.dialogs.CustomDialogBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import printReceipt
import javax.inject.Inject

@HiltViewModel
class TransactionDetailsViewModel @Inject constructor(private val dbRepository: TxnDBRepository, val apiServiceRepository: ApiServiceRepository) : ViewModel(),
    IApiServiceResponseListener {

    private val objRoot = MutableStateFlow(ObjRootAppPaymentDetails())
    var userApiServiceErrorHolder = MutableStateFlow(ApiServiceError())


    fun printReceipt(
        logoResId: Int,
        sharedViewModel: SharedViewModel,
        context: Context,
        customer: Boolean = false,
        objRootAppPaymentDetail: ObjRootAppPaymentDetails
    ) {
        viewModelScope.printReceipt(
            logoResId,
            sharedViewModel,
            context,
            customer,
            false,
            false,
            null,
            objRootAppPaymentDetail,
            lastTxn = false
        )
    }


    override fun onApiServiceSuccess(paymentServiceTxnDetails: PaymentServiceTxnDetails) {
        PaymentServiceUtils.transformObject<ObjRootAppPaymentDetails>(paymentServiceTxnDetails)?.let {
            objRoot.value = it
        }
    }

    override fun onApiServiceError(paymentError: ApiServiceError) {
        Log.e("API Response", paymentError.errorMessage)
        userApiServiceErrorHolder.value = paymentError
    }

    override fun onApiServiceDisplayProgress(
        show: Boolean,
        title: String?,
        subTitle: String?,
        message: String?
    ) {
        CustomDialogBuilder.composeProgressDialog(show = show, title = title, subtitle = subTitle, message = message)
    }
}