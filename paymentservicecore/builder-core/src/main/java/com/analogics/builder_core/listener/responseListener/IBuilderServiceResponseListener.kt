package com.analogics.builder_core.listener.responseListener



interface IBuilderServiceResponseListener {
  fun onBuilderSuccess(response:String)
  fun onBuilderFailure(error:Any)
}
