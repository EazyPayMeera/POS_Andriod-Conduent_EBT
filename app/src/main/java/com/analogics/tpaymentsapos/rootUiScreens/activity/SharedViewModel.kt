package com.analogics.tpaymentsapos.rootUiScreens.activity

import androidx.lifecycle.ViewModel
import com.analogics.paymentservicecore.models.PosConfig
import com.analogics.tpaymentsapos.rootModel.ObjRootAppPaymentDetails


class SharedViewModel:ViewModel() {
   /* Transaction Data */
   var objRootAppPaymentDetail: ObjRootAppPaymentDetails = ObjRootAppPaymentDetails()
   var objPosConfig: PosConfig? = null

   /* UI flags for transaction states */
   var isTipButtonEnabled: Boolean = false
   var selectedTipButton: Int = 0
   var tipAmount: Double = 0.00

   /* Supporting Functions */
   fun clearTransData()
   {
      objRootAppPaymentDetail = ObjRootAppPaymentDetails()
      isTipButtonEnabled = objPosConfig?.isTipEnabled == true
      selectedTipButton = 0
      tipAmount = 0.00
   }
}