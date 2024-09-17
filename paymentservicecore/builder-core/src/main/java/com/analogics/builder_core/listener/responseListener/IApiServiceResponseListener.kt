package com.analogics.builder_core.listener.responseListener



interface IApiServiceResponseListener {
  fun onApiSuccessRes(response:String)
  fun onApiFailureRes(error:Any)
}
