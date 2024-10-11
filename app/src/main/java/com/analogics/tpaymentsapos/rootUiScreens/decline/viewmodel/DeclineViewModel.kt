package com.analogics.tpaymentsapos.rootUiScreens.decline.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.analogics.paymentservicecore.listeners.responseListener.IApiServiceResponseListener
import com.analogics.paymentservicecore.model.error.ApiServiceError
import com.analogics.paymentservicecore.repository.apiService.ApiServiceRepository
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

    override fun onApiSuccess(response: Any) {
        when (response) {
            is ObjRootAppPaymentDetails -> {
                objRoot.value = response
                updateTxnData(objRoot.value)
            }
        }
    }

    override fun onApiError(apiServiceError: ApiServiceError) {
        userApiErrorHolder.value = apiServiceError
    }

    override fun onDisplayProgress(
        show: Boolean,
        title: String?,
        subTitle: String?,
        message: String?
    ) {

    }
}