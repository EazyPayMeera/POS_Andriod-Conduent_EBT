package com.analogics.tpaymentsapos.rootUiScreens.activity

import androidx.lifecycle.ViewModel
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


class SharedViewModel:ViewModel() {
   var objRootAppPaymentDetail:ObjRootAppPaymentDetails=ObjRootAppPaymentDetails()
}