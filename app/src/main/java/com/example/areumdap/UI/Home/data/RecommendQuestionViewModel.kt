package com.example.areumdap.UI.Home.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.areumdap.Data.repository.ChatbotRepository
import com.example.areumdap.domain.model.question.GetChatbotRecommendResponse
import kotlinx.coroutines.launch

class RecommendQuestionViewModel(
    private val repo: ChatbotRepository
) : ViewModel() {

    private val _questions = MutableLiveData<List<GetChatbotRecommendResponse>>()
    val questions: LiveData<List<GetChatbotRecommendResponse>> = _questions

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun fetch() {
        viewModelScope.launch {
            _loading.value = true
            runCatching { repo.getTodayRecommendations() }
                .onSuccess { res ->
                    _questions.value = res.data?.questions ?: emptyList()
                    _error.value = null
                }
                .onFailure { e -> _error.value = e.message ?: "��õ ���� �ҷ����� ����" }
            _loading.value = false
        }
    }
}
