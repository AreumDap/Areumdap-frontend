package com.example.areumdap.data.repository

import com.example.areumdap.data.model.ChatSummaryData
import com.example.areumdap.data.model.SendChatMessageResponse
import com.example.areumdap.data.api.ApiResponse

interface ChatRepository {
    suspend fun ask(content: String, threadId: Long): SendChatMessageResponse
    suspend fun stopChat(threadId: Long)

    suspend fun fetchSummary(accessToken:String, threadId: Long): Result<ChatSummaryData>

    suspend fun saveQuestion(chatHistoryId:Long):Result<ApiResponse<Unit>>
}