package com.eazypaytech.posafrica.rootUiScreens.decline.viewmodel

import androidx.lifecycle.ViewModel
import com.eazypaytech.paymentservicecore.repository.apiService.ApiServiceRepository
import com.eazypaytech.securityframework.database.dbRepository.TxnDBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DeclineViewModel @Inject constructor(private var dbRepository: TxnDBRepository,var apiServiceRepository: ApiServiceRepository): ViewModel()
{

}