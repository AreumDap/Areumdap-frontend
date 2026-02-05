package com.example.areumdap.Data.repository

import com.example.areumdap.Data.ChatRepository
import com.example.areumdap.Data.api.SendChatMessageRequest
import com.example.areumdap.Data.api.SendChatMessageResponse
import com.example.areumdap.Network.RetrofitClient

class RealChatRepository : ChatRepository {
    override suspend fun ask(content: String, threadId: Long): SendChatMessageResponse {
        val res = RetrofitClient.chatbotApi.sendMessage(
            SendChatMessageRequest(
                content = content,
                userChatThreadId = threadId
            )
        )

        if (!res.isSuccessful) {
            val err = runCatching { res.errorBody()?.string() }.getOrNull()
            throw IllegalStateException("chatbot send failed code=${res.code()} err=$err")
        }

        val wrapper = res.body() ?: throw IllegalStateException("chatbot send empty body")
        val data = wrapper.data ?: throw IllegalStateException("chatbot send data=null")
        return data
    }
}
