package com.analogics.tpaymentsapos.rootUiScreens.decline.viewmodel

import androidx.lifecycle.ViewModel
import com.analogics.paymentservicecore.repository.apiService.ApiServiceRepository
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DeclineViewModel @Inject constructor(private var dbRepository: TxnDBRepository,var apiServiceRepository: ApiServiceRepository): ViewModel()
{

}