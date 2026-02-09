package com.example.areumdap.Data.repository

import com.example.areumdap.Data.api.ChatReportApiService
import com.example.areumdap.UI.record.data.ChatReportDataDto
import com.example.areumdap.UI.record.data.ChatThreadHistoriesDto
import com.example.areumdap.UI.record.data.ChatThreadsData

class ChatReportRepositoryImpl(
    private val api: ChatReportApiService
) : ChatReportRepository {
    override suspend fun getChatThreads(favorite: Boolean, size: Int): Result<ChatThreadsData> =
        runCatching {
            val res = api.getChatThreads(
                favorite = favorite,
                size = size
            )

//            HTTP 레벨 성공 체크
            if (!res.isSuccessful) {
                error("HTTP ${res.code()} ${res.message()}")
            }

            val body = res.body() ?: error("응답 body가 비어있음")

            if (!body.isSuccess || body.data == null) {
                error("API 실패: isSuccess=${body.isSuccess}, data=${body.data}")
            }
            body.data
        }

    override suspend fun getThreadHistories(threadId: Long): Result<ChatThreadHistoriesDto> =
        runCatching {
            val res = api.getThreadHistories(threadId)

            if (!res.isSuccessful) {
                error("HTTP ${res.code()} ${res.message()}")
            }

            val body = res.body() ?: error("응답 body가 비어있음")
            if (!body.isSuccess || body.data == null) {
                error("API 실패: isSuccess=${body.isSuccess}, data=${body.data}")
            }
            body.data
        }

    override suspend fun fetchReport(reportId: Long): Result<ChatReportDataDto> =
        runCatching {
            val res = api.getChatReport(reportId)

            if (!res.isSuccessful) {
                error("HTTP ${res.code()} ${res.message()}")
            }

            val body = res.body() ?: error("응답 body가 비어있음")
            if (!body.isSuccess || body.data == null) {
                error("API 실패: isSuccess=${body.isSuccess}, data=${body.data}")
            }
            body.data
        }

}
