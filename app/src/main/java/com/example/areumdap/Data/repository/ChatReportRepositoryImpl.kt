package com.example.areumdap.Data.repository

import com.example.areumdap.Data.api.ChatReportApiService
import com.example.areumdap.UI.record.data.ChatThreadsData

class ChatReportRepositoryImpl(
    private val api: ChatReportApiService
) : ChatReportRepository {
    override suspend fun getChatThreads(favorite: Boolean, size: Int): Result<ChatThreadsData> =
        runCatching {
            val res = api.getChatTreads(
                favorite = favorite,
                size = size
            )

//            HTTP 레벨 성공 체크
            if (!res.isSuccessful) {
                error("HTTP ${res.code()} ${res.message()}")
            }

            val body = res.body() ?: error("응답 body가 비어있음")

            if (!body.isSuccess || body.data == null) {
            }
            body.data!!
        }
}