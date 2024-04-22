package com.sijuru.core.utils

/**
 * Created by Sainal Sultan on 11/07/2020.
 * kioser.com
 */
sealed class ResultWrapper<out T> {
    data class Success<out T>(val value: T) : ResultWrapper<T>()
    data class GenericError(val code : Int? = null, val error : String? = null) : ResultWrapper<Nothing>()
    object NetworkError : ResultWrapper<Nothing>()
}