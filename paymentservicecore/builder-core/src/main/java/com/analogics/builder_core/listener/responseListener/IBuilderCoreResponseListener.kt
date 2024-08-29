package com.analogics.builder_core.listener.responseListener



interface IBuilderCoreResponseListener {
  fun onApiSuccessRes(response:Any)
  fun onApiFailureRes(error:Any)
}
