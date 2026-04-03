package com.analogics.builder_core.domain.listener.responseListener



interface IBuilderServiceResponseListenerLyra {
  fun onBuilderSuccess(response: ByteArray)
  fun onBuilderFailure(error:Any)
}
