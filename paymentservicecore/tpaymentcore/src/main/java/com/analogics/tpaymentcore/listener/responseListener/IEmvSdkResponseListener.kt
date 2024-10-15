package com.analogics.tpaymentcore.listener.responseListener

interface IEmvSdkResponseListener {
      fun onEmvSdkResponse(response: Any)
      fun onEmvSdkDisplayMessage(uiData: String?)
}