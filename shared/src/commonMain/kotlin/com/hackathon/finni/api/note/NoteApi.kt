package com.hackathon.finni.api.note

import com.hackathon.finni.api.note.model.NoteFilter
import com.hackathon.finni.api.note.model.NoteId
import com.hackathon.finni.api.note.model.NoteRequest
import com.hackathon.finni.api.note.model.NoteResponse
import com.hackathon.finni.api.response.ApiResult
import com.hackathon.finni.api.response.EmptyApiResult
import com.hackathon.finni.api.response.toResult
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class NoteApi(private val client: HttpClient) {

    suspend fun getList(
        offset: Int,
        limit: Int,
        filter: NoteFilter? = null
    ): ApiResult<List<NoteResponse>> =
        client.get("/notes") {
            parameter("offset", offset)
            parameter("limit", limit)
        }.toResult()

    suspend fun getById(id: NoteId): ApiResult<NoteResponse> =
        client.get("/notes/${id.value}").toResult()

    suspend fun create(request: NoteRequest): ApiResult<NoteResponse> =
        client.post("/notes") {
            setBody(request)
        }.toResult()

    suspend fun update(id: NoteId, request: NoteRequest): ApiResult<NoteResponse> =
        client.put("/notes/${id.value}") {
            setBody(request)
        }.toResult()

    suspend fun delete(id: NoteId): EmptyApiResult =
        client.delete("/notes/${id.value}").toResult()
}
