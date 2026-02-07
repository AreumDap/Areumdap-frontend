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

// 대화 요약하기 api
data class ChatSummaryRequest(
    val userChatThreadId: Long
)

data class ChatSummaryData(
    val summaryContent : SummaryContent,
    val userChatThreadId : Long,
    val startedAt : String,
    val endedAt: String,
    val durationMinutes: Int,
    val messageCount: Int,
    val tag:String

)

data class SummaryContent(
    val title : String,
    val summary : String,
    val reflectionDepth : String,
    val keywords : List<String>,
    val discoveries : List<String>
)


