package com.eazypaytech.hardwarecore.domain.listener.responseListener

import com.eazypaytech.hardwarecore.data.model.EmvSdkResult

interface IEmvSdkResponseListener {
      fun onEmvSdkResponse(response: Any)
      fun onEmvSdkDisplayMessage(displayMsgId: EmvSdkResult.DisplayMsgId)
      fun onEmvSdkOnlineRequest(emvTags : HashMap<String,String>, onResponse : (HashMap<String,String>)->Unit)
}