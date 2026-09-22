package com.hackathon.finni.api.tag

import com.hackathon.finni.api.response.ApiResult
import com.hackathon.finni.api.response.EmptyApiResult
import com.hackathon.finni.api.response.toResult
import com.hackathon.finni.api.tag.model.TagId
import com.hackathon.finni.api.tag.model.TagRequest
import com.hackathon.finni.api.tag.model.TagResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class TagApi(private val client: HttpClient) {

    suspend fun getList(): ApiResult<List<TagResponse>> =
        client.get("/tags").toResult()

    suspend fun getById(id: TagId): ApiResult<TagResponse> =
        client.get("/tags/${id.value}").toResult()

    suspend fun create(request: TagRequest): ApiResult<TagResponse> =
        client.post("/tags") {
            setBody(request)
        }.toResult()

    suspend fun update(id: TagId, request: TagRequest): ApiResult<TagResponse> =
        client.put("/tags/${id.value}") {
            setBody(request)
        }.toResult()

    suspend fun delete(id: TagId): EmptyApiResult =
        client.delete("/tags/${id.value}").toResult()
}
