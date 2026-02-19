package com.example.areumdap.UI.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.areumdap.data.repository.ChatReportRepository
import com.example.areumdap.data.model.ChatReportDataDto
import com.example.areumdap.data.model.ChatThreadHistoriesDto
import com.example.areumdap.data.model.UserChatThread
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ChatThreadListUiState  {
    data object Idle : ChatThreadListUiState
    data object Loading : ChatThreadListUiState
    data class Success(
        val items : List<UserChatThread>,
        val hasNext : Boolean,
        val nextCursorTime: String?,
        val nextCursorId: Long?
    ) : ChatThreadListUiState
    data class Error(val message: String) : ChatThreadListUiState
}

sealed interface ThreadHistoriesUiState {
    data object Idle : ThreadHistoriesUiState
    data object Loading : ThreadHistoriesUiState
    data class Success(val data: ChatThreadHistoriesDto) : ThreadHistoriesUiState
    data class Error(val message: String) : ThreadHistoriesUiState
}

sealed interface ReportUiState {
    data object Idle : ReportUiState
    data object Loading : ReportUiState
    data class Success(val data: ChatReportDataDto) : ReportUiState
    data class Error(val message: String) : ReportUiState
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

    private val _historiesState = MutableStateFlow<ThreadHistoriesUiState>(ThreadHistoriesUiState.Idle)
    val historiesState : StateFlow<ThreadHistoriesUiState> = _historiesState.asStateFlow()

    private val _reportState = MutableStateFlow<ReportUiState>(ReportUiState.Idle)
    val reportState : StateFlow<ReportUiState> = _reportState.asStateFlow()

    fun loadThreads(favorite:Boolean = false){
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            repo.getChatThreads(
                favorite = favorite,
                size = 10
            ).onSuccess { data ->
                _threads.value = data.userChatThreads.filter { thread ->
                    val summary = thread.summary?.trim().orEmpty()
                    summary.isNotBlank() && !summary.equals("null", ignoreCase = true)
                }
            }.onFailure { e->
                _error.value = e.message ?: "알 수 없는 오류"
            }

            _loading.value = false
        }
    }

    fun loadThreadHistories(threadId:Long){
        viewModelScope.launch {
            _historiesState.value = ThreadHistoriesUiState.Loading

            repo.getThreadHistories(threadId)
                .onSuccess { dto ->
                    _historiesState.value = ThreadHistoriesUiState.Success(dto)
                }
                .onFailure { e->
                    _historiesState.value = ThreadHistoriesUiState.Error(e.message?: "알 수 없는 오류")
                }
        }
    }

    fun clearReportState(){_reportState.value = ReportUiState.Idle}
    fun clearHistoriesState() { _historiesState.value = ThreadHistoriesUiState.Idle }

    fun loadReport(reportId: Long) {
        viewModelScope.launch {
            _reportState.value = ReportUiState.Loading

            repo.fetchReport(reportId)
                .onSuccess { data ->
                    _reportState.value = ReportUiState.Success(data)
                }
                .onFailure { e ->
                    _reportState.value = ReportUiState.Error(e.message ?: "레포트 불러오기 실패")
                }
        }
    }

    fun toggleFavorite(threadId: Long) {
        viewModelScope.launch {
            _error.value = null

            // 1) 현재 상태 찾기
            val currentList = _threads.value
            val idx = currentList.indexOfFirst { it.threadId == threadId }
            if (idx == -1) return@launch

            val target = currentList[idx]
            val newState = !target.favorite

            // 2) UI 먼저 반영(낙관적 업데이트)
            val optimistic = currentList.toMutableList().apply {
                this[idx] = target.copy(favorite = newState)
            }
            _threads.value = optimistic

            // 3) 서버 반영 (실패하면 롤백)
            val ok = repo.toggleFavorite(threadId = threadId)
                .fold(onSuccess = { true }, onFailure = { false })

            if (!ok) {
                // 롤백
                val rollback = _threads.value.toMutableList().apply {
                    val nowIdx = indexOfFirst { it.threadId == threadId }
                    if (nowIdx != -1) {
                        val nowTarget = this[nowIdx]
                        this[nowIdx] = nowTarget.copy(favorite = !newState)
                    }
                }
                _threads.value = rollback
                _error.value = "즐겨찾기 반영 실패"
            }
        }
    }

}

