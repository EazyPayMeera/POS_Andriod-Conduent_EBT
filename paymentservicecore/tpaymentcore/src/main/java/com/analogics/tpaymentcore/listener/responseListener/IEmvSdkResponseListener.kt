package com.analogics.tpaymentcore.listener.responseListener



interface IEmvSdkResponseListener {
      fun onEmvSdkSuccess(uiData: String)
      fun onEmvSdkError(uiData: String)
      fun onEmvSdkDisplayMessage(uiData: String?)
}