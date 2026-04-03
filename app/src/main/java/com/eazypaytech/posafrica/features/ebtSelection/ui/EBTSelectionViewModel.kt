package com.eazypaytech.posafrica.features.ebtSelection.ui

import androidx.lifecycle.ViewModel
import com.eazypaytech.paymentservicecore.repository.emvService.EmvServiceRepository
import com.eazypaytech.securityframework.database.dbRepository.TxnDBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EBTSelectionViewModel @Inject constructor(private var emvServiceRepository: EmvServiceRepository, val txnDBRepository: TxnDBRepository)  : ViewModel() {


}