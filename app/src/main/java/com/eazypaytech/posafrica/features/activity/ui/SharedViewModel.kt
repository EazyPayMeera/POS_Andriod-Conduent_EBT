package com.eazypaytech.posafrica.features.activity.ui

import androidx.lifecycle.ViewModel
import com.analogics.paymentservicecore.data.model.PosConfig
import com.eazypaytech.posafrica.features.settings.ui.PercentButton
import com.eazypaytech.posafrica.domain.model.ObjRootAppPaymentDetails
import com.analogics.securityframework.database.entity.BatchEntity

class SharedViewModel: ViewModel() {
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
      objRootAppPaymentDetail.procId = objPosConfig?.procId
      objRootAppPaymentDetail.merchantId = objPosConfig?.merchantId
      objRootAppPaymentDetail.terminalId = objPosConfig?.terminalId
      objRootAppPaymentDetail.batchId = objPosConfig?.batchId
      objRootAppPaymentDetail.cashierId = objPosConfig?.cashierId
      objRootAppPaymentDetail.loginId = objPosConfig?.loginId
      objRootAppPaymentDetail.isDemoMode = objPosConfig?.isDemoMode
      objRootAppPaymentDetail.merchantNameLocation = objPosConfig?.merchantNameLocation
      objRootAppPaymentDetail.merchantBankName = objPosConfig?.merchantBankName
      objRootAppPaymentDetail.merchantType = objPosConfig?.merchantType
      objRootAppPaymentDetail.fnsNumber = objPosConfig?.fnsNumber
      objRootAppPaymentDetail.stateCode = objPosConfig?.stateCode
      objRootAppPaymentDetail.countyCode = objPosConfig?.countyCode
      objRootAppPaymentDetail.postalServiceCode = objPosConfig?.postalServiceCode
      objRootAppPaymentDetail.isEMVEnable = objPosConfig?.isEMVEnable
      objRootAppPaymentDetail.isTapEnable = objPosConfig?.isTapEnable

      /* Copy Receipt Data */
      objRootAppPaymentDetail.header1 = objPosConfig?.header1
      objRootAppPaymentDetail.header2 = objPosConfig?.header2
      objRootAppPaymentDetail.header3 = objPosConfig?.header3
      objRootAppPaymentDetail.header4 = objPosConfig?.header4
      objRootAppPaymentDetail.footer1 = objPosConfig?.footer1
      objRootAppPaymentDetail.footer2 = objPosConfig?.footer2
      objRootAppPaymentDetail.footer3 = objPosConfig?.footer3
      objRootAppPaymentDetail.footer4 = objPosConfig?.footer4

   }
}