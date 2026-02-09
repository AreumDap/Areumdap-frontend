package com.example.areumdap.data.repository

import com.example.areumdap.data.api.ChatbotApiService


class ChatbotRepository(
    private val api: ChatbotApiService
) {
    suspend fun getTodayRecommendations() = api.getTodayRecommendations()

}