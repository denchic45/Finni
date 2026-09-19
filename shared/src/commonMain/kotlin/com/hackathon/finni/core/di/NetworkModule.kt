package com.hackathon.finni.core.di

import com.hackathon.finni.api.note.NoteApi
import com.hackathon.finni.api.profile.AuthApi
import com.hackathon.finni.api.profile.UserApi
import com.hackathon.finni.api.project.ProjectApi
import com.hackathon.finni.api.tag.TagApi
import com.hackathon.finni.api.task.TaskApi
import com.hackathon.finni.core.util.AuthLogger
import com.hackathon.finni.data.TokenManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val networkModule = module {
    singleOf(::TokenManager)
    single {
        val tokenManager = get<TokenManager>()

        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
            install(Logging) {
                level = LogLevel.ALL
            }
            defaultRequest {
                url("http://localhost:8080") // TODO: Replace with real base URL
                url("http://10.184.211.156:8080")
                contentType(ContentType.Application.Json)
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        val token = tokenManager.getToken() ?: return@loadTokens null

                        BearerTokens(
                            accessToken = token,
                            refreshToken = tokenManager.getRefreshToken()
                        )
                    }

                    refreshTokens {
                        AuthLogger.i { "Refreshing token..." }
                        tokenManager.refreshToken()?.let { response ->
                            AuthLogger.i { "Tokens refreshed" }
                            BearerTokens(response.accessToken, response.refreshToken)
                        }
                    }
                }
            }
        }
    }

    singleOf(::UserApi)
    singleOf(::ProjectApi)
    singleOf(::TaskApi)
    singleOf(::NoteApi)
    singleOf(::TagApi)

    singleOf(::AuthApi)
}
