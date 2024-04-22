package com.sijuru.core.utils

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Created by Sainal Sultan on 11/07/2020.
 * kioser.com
 */

open class SafeApiCaller {
    
    suspend fun <T> safeApiCall(
        dispatcher: CoroutineDispatcher,
        apiCall: suspend () -> T
    ): ResultWrapper<T> {
        return withContext(dispatcher) {
            try {
                ResultWrapper.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                when (throwable) {
                    is IOException -> {
                        ResultWrapper.NetworkError
                    }
                    is HttpException -> {
                        val code = throwable.code()
                        val errorResponse = throwable.message()
                        ResultWrapper.GenericError(code, errorResponse)
                    }
                    else -> {
                        ResultWrapper.GenericError(null, "$throwable")
                    }
                }
            }
        }
    }

}