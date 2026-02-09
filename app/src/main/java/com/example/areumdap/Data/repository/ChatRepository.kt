package com.example.areumdap.Data.repository

import com.example.areumdap.Data.api.ChatSummaryData
import com.example.areumdap.Data.api.ReportRequest
import com.example.areumdap.Data.api.ReportResponse
import com.example.areumdap.Data.api.SendChatMessageResponse
import com.example.areumdap.UI.Home.data.ApiResponse

interface ChatRepository {
    suspend fun ask(content: String, threadId: Long): SendChatMessageResponse
    suspend fun stopChat(threadId: Long)

    suspend fun fetchSummary(accessToken:String, threadId: Long): Result<ChatSummaryData>

    suspend fun saveQuestion(chatHistoryId:Long):Result<ApiResponse<Unit>>
    suspend fun createReport(threadId:Long) : Result<ReportResponse>
}