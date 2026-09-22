package com.hackathon.finni.api.response

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.hackathon.finni.api.error.ApiError
import com.hackathon.finni.api.error.UnknownError
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

typealias ApiResult<T> = Either<ApiError, T>
typealias EmptyApiResult = ApiResult<Unit>

class ApiException(val error: ApiError) : Exception(error.message)

suspend inline fun <reified T> HttpResponse.toResult(): ApiResult<T> {
    return try {
        if (status.isSuccess()) {
            body<T>().right()
        } else {
            UnknownError(status.value, "Error ${status.value}").left()
        }
    } catch (e: Throwable) {
        UnknownError(status.value, e.message ?: "Failed to deserialize response").left()
    }
}
