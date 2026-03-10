package com.eazypaytech.posafrica.rootUiScreens.txnSel.viewModel

import androidx.lifecycle.ViewModel
import com.eazypaytech.paymentservicecore.repository.emvService.EmvServiceRepository
import com.eazypaytech.securityframework.database.dbRepository.TxnDBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TxnSelectionViewModel @Inject constructor(private var emvServiceRepository:EmvServiceRepository, val txnDBRepository: TxnDBRepository)  : ViewModel() {


}
