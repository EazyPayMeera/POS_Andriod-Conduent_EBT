package com.eazypaytech.posafrica.features.txnSel.ui

import androidx.lifecycle.ViewModel
import com.eazypaytech.paymentservicecore.repository.emvService.EmvServiceRepository
import com.analogics.securityframework.data.repository.TxnDBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TxnSelectionViewModel @Inject constructor(private var emvServiceRepository: EmvServiceRepository, val txnDBRepository: TxnDBRepository)  : ViewModel() {


}