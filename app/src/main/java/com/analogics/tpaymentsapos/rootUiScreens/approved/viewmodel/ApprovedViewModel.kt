package com.analogics.tpaymentsapos.rootUiScreens.approved.viewmodel


import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.analogics.paymentservicecore.models.TxnStatus
import com.analogics.securityframework.database.dbRepository.TxnDBRepository
import com.analogics.tpaymentsapos.navigation.AppNavigationItems
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import com.analogics.tpaymentsapos.rootUiScreens.activity.SharedViewModel
import com.analogics.tpaymentsapos.rootUtils.genericComposeUI.navigateAndClean
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import printReceipt
import javax.inject.Inject

@HiltViewModel
class ApprovedViewModel @Inject constructor(private var dbRepository: TxnDBRepository): ViewModel()
{
    fun onDone(navHostController: NavHostController)
    {
        viewModelScope.launch {
            navHostController.navigateAndClean(AppNavigationItems.DashBoardScreen.route)
        }
    }

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
            false,
            false,
            null,
            objRootAppPaymentDetail,
            lastTxn = false
        )
    }
}

