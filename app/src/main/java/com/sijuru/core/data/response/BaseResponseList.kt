package com.sijuru.core.data.response

data class BaseResponseList<T>(
    val responseCode: Int,
    val responseDescription: String,
    val responseStatus: String,
    val responseData: List<T>
)