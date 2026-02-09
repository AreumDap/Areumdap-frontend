package com.example.areumdap.Data.repository

import com.example.areumdap.Data.api.ChatbotApiService
import com.example.areumdap.Data.api.ReportRequest
import com.example.areumdap.Data.api.ReportResponse
import com.example.areumdap.UI.Home.data.ApiResponse

class ChatbotRepository(
    private val api: ChatbotApiService
) {
    suspend fun getTodayRecommendations() = api.getTodayRecommendations()


}