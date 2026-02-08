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

