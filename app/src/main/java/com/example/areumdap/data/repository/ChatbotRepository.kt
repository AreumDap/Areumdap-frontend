package com.example.areumdap.data.repository

import com.example.areumdap.data.api.ApiException
import com.example.areumdap.data.api.ChatbotApiService
import com.example.areumdap.data.model.AssignedQuestionDto
import com.example.areumdap.data.source.TokenManager



class ChatbotRepository(
    private val api: ChatbotApiService
) {
    suspend fun assignTodayRecommendOnLogin(): Result<Unit> {
        if (TokenManager.isTodayRecommendAssigned()) {
            return Result.success(Unit)
        }

        return runCatching { api.assignTodayRecommend() }
            .mapCatching { res ->
                if (res.isSuccessful) {
                    TokenManager.markTodayRecommendAssigned()
                    Unit
                } else {
                    throw ApiException("HTTP_${res.code()}", "배정 요청 실패")
                }
            }
    }

    suspend fun fetchAssignedQuestionsOnClick(): Result<List<AssignedQuestionDto>> {
        return runCatching { api.getAssignedQuestions() }
            .mapCatching { res ->
                if (!res.isSuccess) {
                    throw ApiException(res.code, res.message)
                }
                res.data?.questions ?: emptyList()
            }
    }
}
