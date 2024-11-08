package com.analogics.builder_core.listener.responseListener



interface IBuilderServiceResponseListenerLyra {
  fun onBuilderSuccess(response: ByteArray)
  fun onBuilderFailure(error:Any)
}
