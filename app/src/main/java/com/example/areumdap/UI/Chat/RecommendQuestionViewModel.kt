package com.example.areumdap.UI.Chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.areumdap.data.api.ApiException
import com.example.areumdap.data.model.AssignedQuestionDto
import com.example.areumdap.data.repository.ChatbotRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

class RecommendQuestionViewModel(
    private val repo: ChatbotRepository
) : ViewModel() {
    private val _questions = MutableLiveData<List<AssignedQuestionDto>>()
    val questions: LiveData<List<AssignedQuestionDto>> = _questions

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private var isFetching = false

    fun fetchAssignedQuestionsOnClick() {
        if (isFetching) return
        viewModelScope.launch {
            isFetching = true
            _loading.value = true
            repo.fetchAssignedQuestionsOnClick()
                .onSuccess { list ->
                    _questions.value = list
                    _error.value = null
                }
                .onFailure { e ->
                    _error.value = mapErrorMessage(e)
                }
            _loading.value = false
            isFetching = false
        }
    }

    fun fetchAssignedQuestionsOnHome() {
        if (isFetching) return
        viewModelScope.launch {
            isFetching = true
            _loading.value = true
            val assignResult = repo.assignTodayRecommendOnLogin()
            if (assignResult.isFailure) {
                val e = assignResult.exceptionOrNull()
                if (e is ApiException && e.code == "USER_404") {
                    _error.value = mapErrorMessage(e)
                    _loading.value = false
                    isFetching = false
                    return@launch
                }
            }

            repo.fetchAssignedQuestionsOnClick()
                .onSuccess { list ->
                    _questions.value = list
                    _error.value = null
                }
                .onFailure { e ->
                    _error.value = mapErrorMessage(e)
                }
            _loading.value = false
            isFetching = false
        }
    }

    private fun mapErrorMessage(e: Throwable): String {
        return when (e) {
            is ApiException -> when (e.code) {
                "USER_404" -> "사용자 정보를 찾을 수 없어요."
                "CHATBOT_422" -> "오늘 배정 가능한 질문이 없어요."
                else -> e.message
            }
            is HttpException -> "서버 오류가 발생했어요. 잠시 후 다시 시도해 주세요."
            else -> "추천 질문 불러오기 실패"
        }
    }
}
