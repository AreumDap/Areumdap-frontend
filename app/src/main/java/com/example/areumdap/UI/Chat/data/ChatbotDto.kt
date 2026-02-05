package com.example.areumdap.UI.Chat.data


// 대화 시작하기 api
data class StartChatRequest(
    val content: String,
    val userQuestionId: Long
)

data class StartChatResponse(
    val content: String,
    val userChatThreadId: Long
)

// 메시지 전송 api