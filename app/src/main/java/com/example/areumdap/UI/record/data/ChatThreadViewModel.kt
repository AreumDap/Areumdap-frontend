package com.example.areumdap.UI.record.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.areumdap.Data.repository.ChatReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ChatThreadListUiState  {
    data object Idle : ChatThreadListUiState
    data object Loding : ChatThreadListUiState
    data class Success(
        val items : List<UserChatThread>,
        val hasNext : Boolean,
        val nextCursorTime: String?,
        val nextCursorId: Long?
    ) : ChatThreadListUiState
    data class Error(val message: String) : ChatThreadListUiState
}

class ChatThreadViewModel(
    private val repo: ChatReportRepository
) : ViewModel(){
    private val _threads = MutableStateFlow<List<UserChatThread>>(emptyList())
    val threads : StateFlow<List<UserChatThread>> = _threads.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error : StateFlow<String?> = _error.asStateFlow()

    fun loadThreads(favorite:Boolean = false){
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            repo.getChatThreads(
                favorite = favorite,
                size = 10
            ).onSuccess { data ->
                _threads.value = data.userChatThreads
            }.onFailure { e->
                _error.value = e.message ?: "알 수 없는 오료"
            }

            _loading.value = false
        }
    }
}

