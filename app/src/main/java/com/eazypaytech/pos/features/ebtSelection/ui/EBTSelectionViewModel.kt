package com.eazypaytech.pos.features.ebtSelection.ui

import androidx.lifecycle.ViewModel
import com.analogics.paymentservicecore.domain.repository.emvService.EmvServiceRepository
import com.analogics.securityframework.data.repository.TxnDBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EBTSelectionViewModel @Inject constructor(private var emvServiceRepository: EmvServiceRepository, val txnDBRepository: TxnDBRepository)  : ViewModel() {


}