package com.example.areumdap.Data.repository

import com.example.areumdap.Data.api.ChatbotApiService

class ChatbotRepository(
    private val api: ChatbotApiService
) {
    suspend fun getTodayRecommendations() = api.getTodayRecommendations()

}