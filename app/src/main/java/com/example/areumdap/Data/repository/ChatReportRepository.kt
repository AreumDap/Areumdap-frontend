package com.example.areumdap.Data.repository

import com.example.areumdap.UI.record.data.ChatThreadsData

interface ChatReportRepository {
    suspend fun getChatThreads(favorite : Boolean = false, size : Int = 10):Result<ChatThreadsData>
}