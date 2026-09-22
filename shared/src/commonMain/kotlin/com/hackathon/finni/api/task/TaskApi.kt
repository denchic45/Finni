package com.hackathon.finni.api.task

import com.hackathon.finni.api.project.model.ProjectId
import com.hackathon.finni.api.response.ApiResult
import com.hackathon.finni.api.response.EmptyApiResult
import com.hackathon.finni.api.response.toResult
import com.hackathon.finni.api.task.model.TaskId
import com.hackathon.finni.api.task.model.TaskRequest
import com.hackathon.finni.api.task.model.TaskResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class TaskApi(private val client: HttpClient) {

    suspend fun getList(projectId: ProjectId): ApiResult<List<TaskResponse>> =
        client.get("/tasks") {
            parameter("projectId", projectId.value)
        }.toResult()

    suspend fun getById(id: TaskId): ApiResult<TaskResponse> =
        client.get("/tasks/${id.value}").toResult()

    suspend fun create(request: TaskRequest): ApiResult<TaskResponse> =
        client.post("/tasks") {
            setBody(request)
        }.toResult()

    suspend fun update(id: TaskId, request: TaskRequest): ApiResult<TaskResponse> =
        client.put("/tasks/${id.value}") {
            setBody(request)
        }.toResult()

    suspend fun delete(id: TaskId): EmptyApiResult =
        client.delete("/tasks/${id.value}").toResult()
}
