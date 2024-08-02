package com.analogics.networkservicecore.listener

interface IOnGetResponseListener {
    fun getSuccessResponse(string: String)
    fun getFailureResponse(string: String)
}