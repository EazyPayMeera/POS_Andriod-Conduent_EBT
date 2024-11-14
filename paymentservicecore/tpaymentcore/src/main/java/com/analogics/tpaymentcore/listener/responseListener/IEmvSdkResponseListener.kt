package com.analogics.tpaymentcore.listener.responseListener

import com.analogics.tpaymentcore.model.emv.EmvSdkResult

interface IEmvSdkResponseListener {
      fun onEmvSdkResponse(response: Any)
      fun onEmvSdkDisplayMessage(displayMsgId: EmvSdkResult.DisplayMsgId)
      fun onEmvSdkOnlineRequest(emvTags : HashMap<String,String>, onResponse : (HashMap<String,String>)->Unit)
}