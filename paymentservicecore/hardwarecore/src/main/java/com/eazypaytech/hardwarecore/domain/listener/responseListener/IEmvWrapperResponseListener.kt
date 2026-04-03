package com.eazypaytech.tpaymentcore.listener.responseListener



interface IEmvWrapperResponseListener {
      fun onEmvWrapperSuccess(uiData: String)
      fun onEmvWrapperError(uiData: String)
      fun onEmvWrapperDisplayMessage(uiData: String?)
}