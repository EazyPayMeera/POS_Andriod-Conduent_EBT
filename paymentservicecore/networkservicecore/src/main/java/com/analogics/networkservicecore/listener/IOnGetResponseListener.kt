package com.eazypaytech.networkservicecore.listener

interface IOnGetResponseListener {
    fun getSuccessResponse(string: String)
    fun getFailureResponse(string: String)
}