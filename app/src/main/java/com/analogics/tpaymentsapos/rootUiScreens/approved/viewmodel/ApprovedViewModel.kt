package com.analogics.tpaymentsapos.rootUiScreens.approved.viewmodel


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import printReceipt
import javax.inject.Inject

@HiltViewModel
class ApprovedViewModel @Inject constructor(private var dbRepository: TxnDBRepository): ViewModel()
{

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
            objRootAppPaymentDetail,
            dbRepository
        )
    }
}
    
