package com.example.areumdap.Data.repository

import com.example.areumdap.Data.repository.ChatRepository
import com.example.areumdap.Data.api.ChatSummaryData
import com.example.areumdap.Data.api.ChatSummaryRequest
import com.example.areumdap.Data.api.SendChatMessageRequest
import com.example.areumdap.Data.api.SendChatMessageResponse
import com.example.areumdap.Network.RetrofitClient

class ChatRepositoryImpl : ChatRepository {
    override suspend fun ask(content: String, threadId: Long): SendChatMessageResponse {
        val res = RetrofitClient.chatbotApiService.sendMessage(
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

    override suspend fun stopChat(threadId: Long) {
        val res = RetrofitClient.chatbotApiService.stopChat(threadId)

        if (!res.isSuccessful) {
            val err = runCatching { res.errorBody()?.string() }.getOrNull()
            throw IllegalStateException("chatbot stop failed code=${res.code()} err=$err")
        }

        val wrapper = res.body() ?: throw IllegalStateException("chatbot stop empty body")
        if (!wrapper.isSuccess) throw IllegalStateException("chatbot stop fail msg=${wrapper.message}")
    }

    override suspend fun fetchSummary(accessToken: String, threadId: Long):Result<ChatSummaryData> {
        return runCatching {
            val wrapper = RetrofitClient.chatbotApiService.getChatSummary(
                ChatSummaryRequest(userChatThreadId  = threadId)
            )

            if(!wrapper.isSuccess){
                throw  IllegalStateException("chatbot summary fail code=${wrapper.code} msg=${wrapper.message}")
            }
            wrapper.data ?: throw IllegalStateException("chatbot summary data=null")
        }
    }
}
