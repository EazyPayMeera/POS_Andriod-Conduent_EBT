package com.analogics.tpaymentcore.listener



interface IPaymentSDKListener {
      fun onTPaymentSDKInit(uiData: String)
      fun onTPaymentSDKHandler(uiData: String)
      fun onTPaymentDisplayMessage(uiData: String?)
}