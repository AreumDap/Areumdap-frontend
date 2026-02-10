package com.example.areumdap.data.api

import com.example.areumdap.data.model.CompletedMissionsResponse
import com.example.areumdap.data.model.MissionDetailResponse
import com.example.areumdap.data.model.SavedQuestionsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
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
        @Path("missionId") missionId: Int
    ): Response<BaseResponse<MissionDetailResponse>>

    @POST("api/missions/complete")
    suspend fun postMissionComplete(
        @Body missionIdMap: Map<String, Int>
    ): Response<BaseResponse<Unit>>

    @GET("api/questions")
    suspend fun getSavedQuestions(
        @Query("tag") tag: String?,
        @Query("cursorTime") cursorTime: String?,
        @Query("cursorId") cursorId: Int?,
        @Query("size") size: Int?
    ): Response<BaseResponse<SavedQuestionsResponse>>

    @DELETE("api/missions/{missionId}")
    suspend fun deleteCompletedMission(
        @Path("missionId") missionId: Int
    ): Response<BaseResponse<Unit>>

    @DELETE("api/questions/{userQuestionId}")
    suspend fun deleteSavedQuestion(
        @Path("userQuestionId") userQuestionId: Long
    ): Response<BaseResponse<Unit>>
}