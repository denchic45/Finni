package com.hackathon.finni.api.profile

import com.hackathon.finni.api.profile.model.LoginRequest
import com.hackathon.finni.api.profile.model.RegisterRequest
import com.hackathon.finni.api.profile.model.TokensResponse
import com.hackathon.finni.api.profile.model.UserResponse
import com.hackathon.finni.api.response.ApiResult
import com.hackathon.finni.api.response.toResult
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class AuthApi(private val client: HttpClient) {

    suspend fun login(request: LoginRequest): ApiResult<TokensResponse> =
        client.post("/auth/login") {
            setBody(request)
        }.toResult()

    suspend fun register(request: RegisterRequest): ApiResult<TokensResponse> =
        client.post("/auth/register") {
            setBody(request)
        }.toResult()
}

class UserApi(private val client: HttpClient) {

    suspend fun getProfile(): ApiResult<UserResponse> =
        client.get("/users/me").toResult()
}
