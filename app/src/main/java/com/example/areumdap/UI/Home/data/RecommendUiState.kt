package com.example.areumdap.UI.Home.data

import com.example.areumdap.domain.model.question.GetChatbotRecommendsResponse

sealed interface RecommendUiState {
    data object Loading: RecommendUiState
    data class Success(val items:List<GetChatbotRecommendsResponse>) : RecommendUiState
    data class Error(val message:String): RecommendUiState
}