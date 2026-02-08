package com.example.areumdap.Task

import com.example.areumdap.Task.CompletedMissionsRequest
import com.example.areumdap.Task.CompletedMissionsResponse
import com.example.areumdap.Network.model.BaseResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TaskApiService {
    @GET("api/missions/completed")
    suspend fun getCompletedMissions(
        @Query("tag") tag: String?,
        @Query("cursorTime") cursorTime: String?,
        @Query("cursorId") cursorId: Int?,
        @Query("size") size: Int?
    ): Response<BaseResponse<CompletedMissionsResponse>>

    @GET("api/missions/{missionId}")
    suspend fun getMissionDetail(
        @retrofit2.http.Path("missionId") missionId: Int
    ): Response<BaseResponse<MissionDetailResponse>>

    @POST("api/missions/complete")
    suspend fun postMissionComplete(
        @retrofit2.http.Body missionIdMap: Map<String, Int>
    ): Response<BaseResponse<Unit>>

    @GET("api/questions")
    suspend fun getSavedQuestions(
        @Query("tag") tag: String?,
        @Query("cursorTime") cursorTime: String?,
        @Query("cursorId") cursorId: Int?,
        @Query("size") size: Int?
    ): Response<BaseResponse<SavedQuestionsResponse>>

    @retrofit2.http.DELETE("api/missions/{missionId}")
    suspend fun deleteCompletedMission(
        @retrofit2.http.Path("missionId") missionId: Int
    ): Response<BaseResponse<Unit>>

    @retrofit2.http.DELETE("api/chatbot")
    suspend fun deleteSavedQuestion(
        @Query("userChatThreadId") threadId: Long
    ): Response<BaseResponse<Unit>>
}