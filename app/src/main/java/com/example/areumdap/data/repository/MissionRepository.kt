package com.example.areumdap.data.repository

import com.example.areumdap.data.api.MissionApiService
import com.example.areumdap.data.model.CreateMissionRequest
import com.example.areumdap.data.model.CreateMissionResponse

class MissionRepository(private val api: MissionApiService) {

    suspend fun createMissions(threadId: Long): Result<CreateMissionResponse> {
        val req = CreateMissionRequest(threadId)
        val res = api.createMissions(req)

        if (res.isSuccessful) {
            val body = res.body() ?: return Result.failure(RuntimeException("Empty response body"))
            val data = body.data
                ?: return Result.failure(RuntimeException(body.message.ifBlank { "Response data is null" }))
            return Result.success(data)
        }

        val errorBody = res.errorBody()?.string()
        return Result.failure(
            when (res.code()) {
                400 -> IllegalStateException("Bad request")
                403 -> SecurityException("Forbidden")
                404 -> NoSuchElementException("Not found")
                else -> RuntimeException("Server error (${res.code()})")
            }
        )
    }
}