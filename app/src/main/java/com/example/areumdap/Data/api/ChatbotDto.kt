package com.example.areumdap.Data.api


// 대화 시작하기 api
data class StartChatRequest(
    val content: String,
    val userQuestionId: Long? = null
)

data class StartChatResponse(
    val content: String,
    val userChatThreadId: Long
)

// 메시지 전송 api
data class SendChatMessageRequest(
    val content: String,
    val userChatThreadId: Long
)

data class SendChatMessageResponse(
    val content: String,
    val userChatThreadId: Long,
    val isSessionEnd: Boolean
)
