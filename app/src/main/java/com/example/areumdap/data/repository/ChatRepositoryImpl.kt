package com.example.areumdap.data.repository

import android.util.Log
import com.example.areumdap.Data.api.ChatbotApiService
import com.example.areumdap.data.api.ApiResponse
import com.example.areumdap.data.model.ChatSummaryData
import com.example.areumdap.data.model.ChatSummaryRequest
import com.example.areumdap.data.model.ReportRequest
import com.example.areumdap.data.model.ReportResponse
import com.example.areumdap.data.model.SendChatMessageRequest
import com.example.areumdap.data.model.SendChatMessageResponse

class ChatRepositoryImpl(
    private val api: ChatbotApiService
) : ChatRepository {

    override suspend fun ask(content: String, threadId: Long): SendChatMessageResponse {
        val req = SendChatMessageRequest(content = content, userChatThreadId = threadId)
        val res = api.sendMessage(req)

        if (!res.isSuccessful) {
            val err = res.errorBody()?.string()
            throw RuntimeException("Chat API Fail: code=${res.code()} msg=$err")
        }

        val body = res.body()
        if (body == null || !body.isSuccess) {
            // body.message might be null, handle gracefully
            val msg = body?.message ?: "Unknown API error"
            throw RuntimeException("Chat API Error: $msg")
        }

        val data = body.data ?: throw RuntimeException("Null response data")
        return data
    }

    override suspend fun stopChat(threadId: Long) {
        val res = api.stopChat(threadId)
        if (!res.isSuccessful) {
            Log.e("ChatRepository", "stopChat failed: ${res.code()}")
        }
    }

    override suspend fun fetchSummary(accessToken: String, threadId: Long): Result<ChatSummaryData> {
        return try {
            val req = ChatSummaryRequest(userChatThreadId = threadId)
            val response = api.getChatSummary(req)
            
            if (response.isSuccess && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveQuestion(chatHistoryId: Long): Result<ApiResponse<Unit>> {
        return try {
            val res = api.saveQuestion(chatHistoryId)
            if (res.isSuccessful) {
                val body = res.body()
                if (body != null && body.isSuccess) {
                    Result.success(body)
                } else {
                    Result.failure(Exception(body?.message ?: "Unknown error"))
                }
            } else {
                Result.failure(Exception("HTTP ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createReport(threadId: Long): Result<ReportResponse> {
        return try {
            val req = ReportRequest(userChatThreadId = threadId)
            val res = api.createReport(req)
            
            if (res.isSuccessful) {
                val body = res.body()
                if (body != null) Result.success(body)
                else Result.failure(Exception("Empty body"))
            } else {
                Result.failure(Exception("HTTP ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}