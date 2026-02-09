package com.example.areumdap.data.repository

import android.util.Log
import com.example.areumdap.data.api.MissionApiService
import com.example.areumdap.data.model.CreateMissionRequest
import com.example.areumdap.data.model.CreateMissionResponse

class MissionRepository(private val api: MissionApiService) {

    suspend fun createMissions(threadId: Long): Result<CreateMissionResponse> {
        val req = CreateMissionRequest(threadId)
        val res = api.createMissions(req)

        if (res.isSuccessful) {
            val body = res.body() ?: return Result.failure(RuntimeException("Empty response body"))
            Log.d("MissionRepository", "createMissions success: code=${body.code}, message=${body.message}, data=${body.data}")
            val data = body.data
                ?: return Result.failure(RuntimeException(body.message.ifBlank { "Response data is null" }))
            return Result.success(data)
        }

        val errorBody = res.errorBody()?.string()
        Log.e("MissionRepository", "createMissions failed: http=${res.code()}, errorBody=$errorBody")
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