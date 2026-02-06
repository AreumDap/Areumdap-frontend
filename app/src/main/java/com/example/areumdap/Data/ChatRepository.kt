package com.example.areumdap.Data

import com.example.areumdap.Data.api.ChatSummaryData
import com.example.areumdap.Data.api.SendChatMessageResponse

interface ChatRepository {
    suspend fun ask(content: String, threadId: Long): SendChatMessageResponse
    suspend fun stopChat(threadId: Long)

    suspend fun fetchSummary(accessToken:String, threadId: Long): Result<ChatSummaryData>
}
