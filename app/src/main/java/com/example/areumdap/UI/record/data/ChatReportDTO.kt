package com.example.areumdap.UI.record.data


data class ChatThreadsData(
    val userChatThreads: List<UserChatThread>,
    val nextCursorTime: String?, // "2026-01-26T01:44:27.394324"
    val nextCursorId: Long?,
    val hasNext: Boolean
)

data class UserChatThread(
    val threadId: Long,
    val tag: String,
    val content: String,
    val createdAt: String, // ISO string
    val summary: String?,
    val favorite: Boolean
)

data class ChatThreadHistoriesDto(
    val threadId: Long,
    val histories: List<HistoryDto>
)

data class HistoryDto(
    val id: Long,
    val content: String,
    val senderType: String,
    val createdAt: String
)

data class ChatReportResponse(
    val isSuccess: Boolean,
    val code : String,
    val message:String,
    val data : ChatReportDataDto?
)

class ChatReportDataDto(
    val title: String,
    val messageCount: Int,
    val depth: String,
    val durationMinutes: Int,
    val summaryContent: String,
    val reportTags: List<ReportTagDto>,
    val insightContents: List<InsightDto>,
    val missions: List<MissionDto>,
    val createdAt: String
)

data class ReportTagDto(
    val tag: String
)

data class InsightDto(
    val insightId: Long,
    val content: String
)

data class MissionDto(
    val missionId: Long,
    val tag: String,      // "CAREER" 같은 enum 문자열
    val title: String,
    val content: String,
    val status: String    // "COMPLETED" 같은 enum 문자열
)


