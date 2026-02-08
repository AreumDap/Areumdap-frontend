package com.example.areumdap.Data.api


import com.example.areumdap.UI.Home.data.ApiResponse
import com.example.areumdap.UI.record.data.ChatThreadHistoriesDto
import com.example.areumdap.UI.record.data.ChatThreadsData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatReportApiService{
    @GET("api/chat/threads")
    suspend fun getChatThreads(
        @Query("favorite") favorite: Boolean = false,
        @Query("size") size:Int = 10
    ) : Response<ApiResponse<ChatThreadsData>>

    @GET("api/chat/threads/{threadId}")
    suspend fun getThreadHistories(
        @Path("threadId") threadId:Long
    ):Response<ApiResponse<ChatThreadHistoriesDto>>
}