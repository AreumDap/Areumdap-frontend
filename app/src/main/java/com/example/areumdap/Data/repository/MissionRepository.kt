package com.example.areumdap.Data.repository


import com.example.areumdap.Data.api.MissionApiService
import com.example.areumdap.Network.RetrofitClient
import com.example.areumdap.UI.Chat.data.CreateMissionRequest
import com.example.areumdap.UI.Chat.data.Mission

class MissionRepository {
    private val api: MissionApiService = RetrofitClient.missionApi

    suspend fun createMission(threadId: Long, accessToken:String) : Result<List<Mission>>{

        if (accessToken.isBlank()) {
            return Result.failure(Exception("로그인이 필요합니다"))
        }
        return try {
            val response = api.createMissions(
                authorization = "Bearer $accessToken",
                request = CreateMissionRequest(threadId)
            )

            if(response.isSuccessful && response.body()?.isSuccess == true){
                val missions: List<Mission> = response.body()?.data?.missions ?: emptyList()
                Result.success(missions)
            } else {
                Result.failure(Exception("API 호출 실패 : ${response.code()}"))
            }
        } catch(e: Exception){
            Result.failure(e)
        }
    }
}