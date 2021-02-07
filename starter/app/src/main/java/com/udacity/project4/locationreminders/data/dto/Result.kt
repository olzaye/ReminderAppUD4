package com.udacity.project4.locationreminders.data.dto

import java.lang.Exception


/**
 * A sealed class that encapsulates successful outcome with a value of type [T]
 * or a failure with message and statusCode
 */
sealed class Result<out T : Any> {
    data class Success<out T : Any>(val data: T) : Result<T>()
    data class Error(val message: String?, val statusCode: Int? = null) :
        Result<Nothing>()
}

sealed class ResultSAmple<T:Any> {
    data class Success<T:Any>(val data: T) : ResultSAmple<T>()
    data class Error(val message: String?,val e : Exception) : ResultSAmple<Nothing>()
}