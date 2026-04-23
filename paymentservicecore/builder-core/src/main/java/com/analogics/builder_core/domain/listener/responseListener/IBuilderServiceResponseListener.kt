package com.analogics.builder_core.domain.listener.responseListener



interface IBuilderServiceResponseListener {
  fun onBuilderSuccess(response: ByteArray)
  fun onBuilderFailure(error:Any)
}
