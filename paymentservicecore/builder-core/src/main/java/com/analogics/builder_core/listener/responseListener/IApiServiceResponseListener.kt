package com.analogics.builder_core.listener.responseListener



interface IApiServiceResponseListener {
  fun onApiSuccessRes(response:Any)
  fun onApiFailureRes(error:Any)
}
