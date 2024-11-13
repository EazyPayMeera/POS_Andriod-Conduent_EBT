package com.analogics.tpaymentsapos.rootUiScreens.decline.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.analogics.builder_core.model.PaymentServiceTxnDetails
import com.analogics.paymentservicecore.listeners.responseListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.model.error.ApiServiceError
import com.analogics.paymentservicecore.repository.apiService.ApiServiceRepository
import com.analogics.paymentservicecore.utils.PaymentServiceUtils
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.convertObjRootToTxnEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeclineViewModel @Inject constructor(private var dbRepository: TxnDBRepository,var apiServiceRepository: ApiServiceRepository): ViewModel(),
    IApiServiceResponseListener {

    private val objRoot = MutableStateFlow(ObjRootAppPaymentDetails())
    var userApiErrorHolder = MutableStateFlow(ApiServiceError())

    fun updateTxnData(objRootAppPaymentDetails: ObjRootAppPaymentDetails) = viewModelScope.launch {
        // Convert ObjRootAppPaymentDetails to JSON or entity object
        val txnEntity = convertObjRootToTxnEntity(objRootAppPaymentDetails)

        txnEntity.id?.let { id ->
            // Check if the entity exists in the database using the primary key (invoice no)
            val existingEntity = dbRepository.fetchTransactionDetailsTxn(id)

            if (existingEntity != null) {
                // Entry found, proceed with update
                dbRepository.updateTxn(txnEntity)
                Log.d("Record Update", "Record update successful for invoice no: $id")
            } else {
                // Entry not found, log the message
                dbRepository.insertTxn(txnEntity)
            }
        } ?: Log.d("Record Update", "Invoice No is null")
    }

    override fun onApiServiceSuccess(paymentServiceTxnDetails: PaymentServiceTxnDetails) {
        PaymentServiceUtils.transformObject<ObjRootAppPaymentDetails>(paymentServiceTxnDetails)?.let {
            objRoot.value = it
            updateTxnData(objRoot.value)
        }
    }

    override fun onApiServiceError(apiServiceError: ApiServiceError) {
        userApiErrorHolder.value = apiServiceError
    }

    override fun onApiServiceDisplayProgress(
        show: Boolean,
        title: String?,
        subTitle: String?,
        message: String?
    ) {

    }
}