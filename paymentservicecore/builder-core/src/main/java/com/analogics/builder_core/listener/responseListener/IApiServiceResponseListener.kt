package com.analogics.builder_core.listener.responseListener



interface IApiServiceResponseListener {
  fun onApiSuccess(response:String)
  fun onApiFailure(error:Any)
}
