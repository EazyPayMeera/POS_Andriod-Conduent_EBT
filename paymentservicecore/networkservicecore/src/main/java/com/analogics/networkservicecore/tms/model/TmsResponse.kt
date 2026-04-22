package com.analogics.networkservicecore.tms.model

data class TmsResponse(
    val result: Int,
    val total: Int?,
    val data: DataWrapper?
)

data class DataWrapper(
    val terminal_param: TerminalParam?
)

data class TerminalParam(
    val ebt_device_config: Map<String, Map<String, String>>?
)