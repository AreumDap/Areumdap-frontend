package com.example.areumdap.Data

import com.example.areumdap.Data.api.SendChatMessageResponse

interface ChatRepository {
    suspend fun ask(content: String, threadId: Long): SendChatMessageResponse
}
