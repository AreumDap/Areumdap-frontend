package com.example.areumdap.domain.model.question

data class GetChatbotRecommendsResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val data: GetChatbotRecommendsData?
)

data class GetChatbotRecommendsData(
    val questions: List<GetChatbotRecommendResponse>
)

data class GetChatbotRecommendResponse(
    val userQuestionId: Long,
    val content: String,
    val tag: String
)
