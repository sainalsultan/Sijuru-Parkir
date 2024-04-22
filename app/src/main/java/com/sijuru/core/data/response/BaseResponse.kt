package com.sijuru.core.data.response

data class BaseResponse(
    val responseCode: Int,
    val responseDescription: String,
    val responseStatus: String
)