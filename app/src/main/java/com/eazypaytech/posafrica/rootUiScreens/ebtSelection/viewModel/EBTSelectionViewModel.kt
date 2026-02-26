package com.eazypaytech.posafrica.rootUiScreens.ebtSelection.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.eazypaytech.paymentservicecore.constants.AppConstants
import com.eazypaytech.paymentservicecore.models.TxnType
import com.eazypaytech.paymentservicecore.repository.emvService.EmvServiceRepository
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.getCurrentDateTime
import com.eazypaytech.posafrica.rootUtils.genericComposeUI.removeNonDigits
import com.eazypaytech.securityframework.database.dbRepository.TxnDBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EBTSelectionViewModel @Inject constructor(private var emvServiceRepository:EmvServiceRepository, val txnDBRepository: TxnDBRepository)  : ViewModel() {


}
