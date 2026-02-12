package com.example.areumdap.data.repository


import com.example.areumdap.data.api.ApiResponse
import com.example.areumdap.data.model.ChatReportDataDto
import com.example.areumdap.data.model.ChatThreadHistoriesDto
import com.example.areumdap.data.model.ChatThreadsData

interface ChatReportRepository {
    suspend fun getChatThreads(favorite : Boolean = false, size : Int = 10):Result<ChatThreadsData>
    suspend fun getThreadHistories(threadId: Long): Result<ChatThreadHistoriesDto>

    suspend fun fetchReport(reportId : Long) : Result<ChatReportDataDto>

    suspend fun toggleFavorite(threadId: Long) : Result<ApiResponse<Unit>>
}
