package com.analogics.builder_core.listener.responseListener



interface IBuilderCoreResponseListener {
  fun onApiSuccessRes(respone:Any)
  fun onApiFailureRes(error:Any)
}
