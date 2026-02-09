package com.example.areumdap.Data.repository

import com.example.areumdap.UI.record.data.ChatReportDataDto
import com.example.areumdap.UI.record.data.ChatThreadsData
import com.example.areumdap.UI.record.data.ChatThreadHistoriesDto

interface ChatReportRepository {
    suspend fun getChatThreads(favorite : Boolean = false, size : Int = 10):Result<ChatThreadsData>
    suspend fun getThreadHistories(threadId: Long): Result<ChatThreadHistoriesDto>

    suspend fun fetchReport(reportId : Long) : Result<ChatReportDataDto>

}
