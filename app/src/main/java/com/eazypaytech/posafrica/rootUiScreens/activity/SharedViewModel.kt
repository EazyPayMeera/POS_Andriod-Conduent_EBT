package com.eazypaytech.posafrica.rootUiScreens.activity

import androidx.lifecycle.ViewModel
import com.eazypaytech.paymentservicecore.models.PosConfig
import com.eazypaytech.securityframework.database.entity.BatchEntity
import com.eazypaytech.posafrica.rootModel.ObjRootAppPaymentDetails
import com.eazypaytech.posafrica.rootUiScreens.settings.config.TipButton


class SharedViewModel:ViewModel() {
   /* Transaction Data */
   var objRootAppPaymentDetail: ObjRootAppPaymentDetails = ObjRootAppPaymentDetails()
   var objPosConfig: PosConfig? = null
   var batchEntity: BatchEntity = BatchEntity()

   /* UI flags for transaction states */
   var isTipButtonEnabled: Boolean = false
   var selectedTipButton: TipButton = TipButton.NONE
   var tipAmount: Double = 0.00

   /* Supporting Functions */
   fun clearTransData()
   {
      objRootAppPaymentDetail = ObjRootAppPaymentDetails()
      isTipButtonEnabled = objPosConfig?.isTipEnabled == true
      selectedTipButton = TipButton.NONE
      tipAmount = 0.00

      /* Copy Config Data */
      objRootAppPaymentDetail.terminalId = objPosConfig?.terminalId
      objRootAppPaymentDetail.merchantId = objPosConfig?.merchantId
      objRootAppPaymentDetail.batchId = objPosConfig?.batchId
      objRootAppPaymentDetail.cashierId = objPosConfig?.cashierId
      objRootAppPaymentDetail.loginId = objPosConfig?.loginId
      objRootAppPaymentDetail.isDemoMode = objPosConfig?.isDemoMode
   }
}