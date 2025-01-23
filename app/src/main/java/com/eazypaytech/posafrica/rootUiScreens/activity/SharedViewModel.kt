package com.eazypaytech.posafrica.rootUiScreens.activity

import androidx.lifecycle.ViewModel
import com.eazypaytech.paymentservicecore.models.PosConfig
import com.eazypaytech.securityframework.database.entity.BatchEntity
import com.eazypaytech.posafrica.rootModel.ObjRootAppPaymentDetails
import com.eazypaytech.posafrica.rootUiScreens.settings.config.PercentButton


class SharedViewModel:ViewModel() {
   /* Transaction Data */
   var objRootAppPaymentDetail: ObjRootAppPaymentDetails = ObjRootAppPaymentDetails()
   var objPosConfig: PosConfig? = null
   var batchEntity: BatchEntity = BatchEntity()

   /* UI flags for transaction states */
   var isTipButtonEnabled: Boolean = false
   var isServiceChargeButtonEnabled: Boolean = false
   var selectedTipButton: PercentButton = PercentButton.NONE
   var selectedServiceChargeButton: PercentButton = PercentButton.NONE
   var tipAmount: Double = 0.00
   var serviceCharge: Double = 0.00

   /* Supporting Functions */
   fun clearTransData()
   {
      objRootAppPaymentDetail = ObjRootAppPaymentDetails()
      isTipButtonEnabled = objPosConfig?.isTipEnabled == true
      isServiceChargeButtonEnabled = objPosConfig?.isServiceChargeEnabled == true
      selectedTipButton = PercentButton.NONE
      selectedServiceChargeButton = PercentButton.NONE
      tipAmount = 0.00
      serviceCharge = 0.00

      /* Copy Config Data */
      objRootAppPaymentDetail.terminalId = objPosConfig?.terminalId
      objRootAppPaymentDetail.merchantId = objPosConfig?.merchantId
      objRootAppPaymentDetail.batchId = objPosConfig?.batchId
      objRootAppPaymentDetail.cashierId = objPosConfig?.cashierId
      objRootAppPaymentDetail.loginId = objPosConfig?.loginId
      objRootAppPaymentDetail.isDemoMode = objPosConfig?.isDemoMode
   }
}