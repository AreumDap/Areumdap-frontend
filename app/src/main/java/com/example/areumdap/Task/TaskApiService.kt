package com.example.areumdap.Task

import com.example.areumdap.Task.CompletedMissionsRequest
import com.example.areumdap.Task.CompletedMissionsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TaskApiService {
    @GET("api/missions/completed")
    suspend fun getCompletedMissions(
        @Query("tag") tag: String?,
        @Query("cursorTime") cursorTime: String,
        @Query("cursorId") cursorId: Int,
        @Query("size") size: Int
    ): Response<CompletedMissionsResponse>

    @GET("api/questions")
    suspend fun getSavedQuestions(
        @Query("tag") tag: String?,
        @Query("cursorTime") cursorTime: String,
        @Query("cursorId") cursorId: Int,
        @Query("size") size: Int
    ): Response<SavedQuestionsResponse>
}