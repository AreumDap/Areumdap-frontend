package com.example.areumdap.Data.api

import com.example.areumdap.UI.Chat.data.CreateMissionRequest
import com.example.areumdap.UI.Chat.data.CreateMissionResponse
import com.example.areumdap.UI.Home.data.*
import retrofit2.Response
import retrofit2.http.*

interface MissionApiService {

    @POST("api/missions")
    suspend fun createMissions(
        @Body request: CreateMissionRequest
    ) : Response<ApiResponse<CreateMissionResponse>>
}