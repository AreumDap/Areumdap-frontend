package com.example.areumdap.data.api


import com.example.areumdap.data.model.ChatReportDataDto
import com.example.areumdap.data.model.ChatThreadHistoriesDto
import com.example.areumdap.data.model.ChatThreadsData
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

    @GET("api/chat/reports/{reportId}")
    suspend fun getChatReport(
        @Path("reportId") reportId: Long
    ) : Response<ApiResponse<ChatReportDataDto>>
}
