package com.example.areumdap.Data.api

import com.example.areumdap.UI.Home.data.ApiResponse
import com.example.areumdap.domain.model.question.GetChatbotRecommendsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ChatbotApi {
    @GET("api/chatbot/recommend")
    suspend fun getTodayRecommendations(): GetChatbotRecommendsResponse

    @POST("api/chatbot/start")
    suspend fun startChat(
        @Body request: StartChatRequest
    ): Response<ApiResponse<StartChatResponse>>

    @POST("api/chatbot")
    suspend fun sendMessage(
        @Body request: SendChatMessageRequest
    ): Response<ApiResponse<SendChatMessageResponse>>
}
