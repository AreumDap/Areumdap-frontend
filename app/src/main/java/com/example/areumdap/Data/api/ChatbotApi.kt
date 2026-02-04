package com.example.areumdap.Data.api

import com.example.areumdap.domain.model.question.GetChatbotRecommendsResponse
import retrofit2.http.GET

interface ChatbotApi {
    @GET("api/chatbot/recommend")
    suspend fun getTodayRecommendations() : GetChatbotRecommendsResponse
}