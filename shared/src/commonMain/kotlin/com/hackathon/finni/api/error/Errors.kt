package com.hackathon.finni.api.error

import io.ktor.http.HttpStatusCode

sealed interface ApiError {
    val httpCode: HttpStatusCode
    val message: String
}

sealed interface AuthError : ApiError
data object InvalidToken : AuthError {
    override val httpCode = HttpStatusCode.Unauthorized
    override val message = "Invalid token"
}

data object InvalidRefreshToken : AuthError {
    override val httpCode = HttpStatusCode.Unauthorized
    override val message = "Invalid refresh token"
}

data object UnauthorizedError : AuthError {
    override val httpCode = HttpStatusCode.Unauthorized
    override val message = "Unauthorized"
}

data object InvalidCredentials : AuthError {
    override val httpCode = HttpStatusCode.BadRequest
    override val message = "Invalid credentials"
}

data class EmailAlreadyUse(override val message: String = "Email already in use") : AuthError {
    override val httpCode = HttpStatusCode.Conflict
}

sealed interface UserError : ApiError
data class UserNotFound(val id: String? = null) : UserError {
    override val httpCode = HttpStatusCode.NotFound
    override val message = "User not found"
}

data class ProjectNotFound(val id: String? = null) : ApiError {
    override val httpCode = HttpStatusCode.NotFound
    override val message = "Project not found"
}

data class NotFoundError(override val message: String = "Not found") : ApiError {
    override val httpCode = HttpStatusCode.NotFound
}

sealed class BadRequestError(override val message: String) : ApiError {
    override val httpCode = HttpStatusCode.BadRequest

    data class General(override val message: String = "Bad request") : BadRequestError(message)
}

data class InvalidRequest(override val message: String = "Invalid request") : ApiError {
    override val httpCode = HttpStatusCode.BadRequest
}

data class UnprocessableEntityError(override val message: String = "Unprocessable entity") :
    ApiError {
    override val httpCode = HttpStatusCode.UnprocessableEntity
}

data class BadGatewayError(override val message: String = "Bad gateway") : ApiError {
    override val httpCode = HttpStatusCode.BadGateway
}

data class InternalServerError(override val message: String = "Internal server error") : ApiError {
    override val httpCode = HttpStatusCode.InternalServerError
}

data class UnknownError(val code: Int = 500, override val message: String = "Unknown error") :
    ApiError {
    override val httpCode = HttpStatusCode.fromValue(code)
    val httpCodeValue: Int get() = code
}

interface FailedValidation : ApiError {
    override val httpCode: HttpStatusCode get() = HttpStatusCode.BadRequest
    override val message: String get() = "Validation failed"
}
