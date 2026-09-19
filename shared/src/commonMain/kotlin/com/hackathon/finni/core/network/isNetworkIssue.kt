package com.hackathon.finni.core.network

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.*
import kotlinx.io.IOException

fun Throwable.isNetworkIssue(): Boolean = when (this) {
    is IOException ,
    is HttpRequestTimeoutException,
    is ConnectTimeoutException -> true // Не смогли даже достучаться
    else -> {
        cause?.isNetworkIssue() == true ||
                // Костыль для iOS, если Ktor пропустил нативную ошибку
        this.message?.contains("NSURLErrorDomain") == true
    }
}