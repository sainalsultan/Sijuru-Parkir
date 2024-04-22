package com.sijuru.core.data.response

data class BaseResponseObject<T>(
    val responseCode: Int,
    val responseDescription: String,
    val responseStatus: String,
    val responseData: T
)