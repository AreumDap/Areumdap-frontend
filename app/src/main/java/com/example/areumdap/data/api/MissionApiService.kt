package com.example.areumdap.data.api

import com.example.areumdap.data.model.CreateMissionRequest
import com.example.areumdap.data.model.CreateMissionResponse
import com.example.areumdap.UI.Home.data.*
import retrofit2.Response
import retrofit2.http.*

interface MissionApiService {

    @POST("api/missions")
    suspend fun createMissions(
        @Body request: CreateMissionRequest
    ) : Response<ApiResponse<CreateMissionResponse>>
}