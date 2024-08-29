package com.analogics.tpaymentcore.listener



interface IPaymentCoreHandlerListener {
      fun onTPaymentSDKInit(uiData: String)
      fun onTPaymentSDKHandler(uiData: String)
}