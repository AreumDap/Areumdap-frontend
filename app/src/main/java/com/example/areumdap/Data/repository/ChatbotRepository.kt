package com.example.areumdap.Data.repository

import com.example.areumdap.Data.api.ChatbotApi

class ChatbotRepository(
    private val api: ChatbotApi
) {
    suspend fun getTodayRecommendations() = api.getTodayRecommendations()
}