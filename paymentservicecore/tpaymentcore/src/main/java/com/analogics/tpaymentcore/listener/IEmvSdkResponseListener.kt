package com.analogics.tpaymentcore.listener



interface IEmvSdkResponseListener {
      fun onEmvSdkSuccess(uiData: String)
      fun onEmvSdkError(uiData: String)
      fun onEmvSdkDisplayMessage(uiData: String?)
}