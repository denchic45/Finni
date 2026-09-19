package com.hackathon.finni.data

import com.hackathon.finni.api.profile.model.RefreshTokenRequest
import com.hackathon.finni.api.profile.model.TokensResponse
import com.hackathon.finni.api.response.toResult
import com.hackathon.finni.core.network.safeFetch
import com.hackathon.finni.core.presentation.error.ApiFailure
import com.hackathon.finni.core.util.AuthLogger
import com.hackathon.finni.data.storage.AuthSettingsStorage
import io.ktor.client.plugins.auth.providers.RefreshTokensParams
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.flow.first

class TokenManager(
    private val storage: AuthSettingsStorage
) {

    suspend fun getToken(): String? = storage.token.first()
    suspend fun getRefreshToken(): String? = storage.refreshToken.first()

    context(params: RefreshTokensParams)
    suspend fun refreshToken(): TokensResponse? {
        val refreshToken = storage.refreshToken.first() ?: return null

        return safeFetch {
            params.client.post("/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(RefreshTokenRequest(refreshToken))
                with(params) {
                    markAsRefreshTokenRequest()
                }
            }.toResult<TokensResponse>()
        }.onRight { response ->
            storage.updateSettings {
                it.copy(
                    token = response.accessToken,
                    refreshToken = response.refreshToken
                )
            }
        }.onLeft { failure ->
            if (failure is ApiFailure && (failure.error.httpCode == HttpStatusCode.Unauthorized || failure.error.httpCode == HttpStatusCode.BadRequest)) {
                AuthLogger.e { "Refresh token is invalid, logging out..." }
                storage.updateSettings { it.copy(token = null, refreshToken = null) }
            }
        }.fold(
            ifLeft = { null },
            ifRight = { it }
        )
    }
}
