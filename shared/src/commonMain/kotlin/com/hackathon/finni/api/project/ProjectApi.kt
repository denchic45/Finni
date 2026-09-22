package com.hackathon.finni.api.project

import com.hackathon.finni.api.project.model.ProjectId
import com.hackathon.finni.api.project.model.ProjectRequest
import com.hackathon.finni.api.project.model.ProjectResponse
import com.hackathon.finni.api.response.ApiResult
import com.hackathon.finni.api.response.toResult
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class ProjectApi(private val client: HttpClient) {

    suspend fun getList(offset: Int, limit: Int): ApiResult<List<ProjectResponse>> =
        client.get("/projects") {
            parameter("offset", offset)
            parameter("limit", limit)
        }.toResult()

    suspend fun getById(id: ProjectId): ApiResult<ProjectResponse> =
        client.get("/projects/${id.value}").toResult()

    suspend fun create(request: ProjectRequest): ApiResult<ProjectResponse> =
        client.post("/projects") {
            setBody(request)
        }.toResult()

    suspend fun update(id: ProjectId, request: ProjectRequest): ApiResult<ProjectResponse> =
        client.put("/projects/${id.value}") {
            setBody(request)
        }.toResult()

    suspend fun delete(id: ProjectId): ApiResult<Unit> =
        client.delete("/projects/${id.value}").toResult()
}
