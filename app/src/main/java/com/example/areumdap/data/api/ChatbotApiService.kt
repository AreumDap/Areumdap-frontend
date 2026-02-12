package com.example.areumdap.data.api

import com.example.areumdap.data.model.AssignedQuestionsResponse
import com.example.areumdap.data.model.ChatSummaryData
import com.example.areumdap.data.model.ChatSummaryRequest
import com.example.areumdap.data.model.ReportRequest
import com.example.areumdap.data.model.ReportResponse
import com.example.areumdap.data.model.SendChatMessageRequest
import com.example.areumdap.data.model.SendChatMessageResponse
import com.example.areumdap.data.model.StartChatRequest
import com.example.areumdap.data.model.StartChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatbotApiService {
    @POST("api/chatbot/recommend")
    suspend fun assignTodayRecommend(): Response<Unit>

    @GET("api/chatbot/assigned")
    suspend fun getAssignedQuestions(): ApiResponse<AssignedQuestionsResponse>

    @POST("api/chat/thread")
    suspend fun startChat(
        @Body request: StartChatRequest
    ): Response<ApiResponse<StartChatResponse>>

    @POST("api/chatbot")
    suspend fun sendMessage(
        @Body request: SendChatMessageRequest
    ): Response<ApiResponse<SendChatMessageResponse>>

    @DELETE("api/chat")
    suspend fun stopChat(
        @Query("userChatThreadId") threadId: Long
    ) : Response<ApiResponse<Unit>>

    @POST("api/chatbot/summary")
    suspend fun getChatSummary(
        @Body body: ChatSummaryRequest
    ) : ApiResponse<ChatSummaryData>

    @POST("api/chat/report")
    suspend fun createReport(
        @Body req: ReportRequest
    ) : Response<ReportResponse>

    @POST("api/questions/{chatHistoryId}")
    suspend fun saveQuestion(
        @Path("chatHistoryId") chatHistoryId:Long
    ) : Response<ApiResponse<Unit>>
}
