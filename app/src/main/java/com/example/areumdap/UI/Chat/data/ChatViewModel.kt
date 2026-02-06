package com.example.areumdap.UI.Chat.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.areumdap.Data.ChatRepository
import com.example.areumdap.Data.api.ChatSummaryData
import com.example.areumdap.Data.api.StartChatRequest
import com.example.areumdap.Data.repository.ChatRepositoryImpl
import com.example.areumdap.Network.RetrofitClient
import com.example.areumdap.domain.model.ChatMessage
import com.example.areumdap.domain.model.Sender
import com.example.areumdap.domain.model.Status
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface SummaryUiState{
    data object Idle: SummaryUiState
    data object Loading : SummaryUiState
    data class Success(val data : ChatSummaryData) : SummaryUiState
    data class Error(val message:String) : SummaryUiState
}

class ChatViewModel(
    private val repo: ChatRepository = ChatRepositoryImpl()
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val message: StateFlow<List<ChatMessage>> = _messages

    private val _endEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val endEvent: SharedFlow<Unit> = _endEvent

    private var threadId: Long? = null
    private var lastEndedThreadId: Long? = null

    private val _summaryState = MutableStateFlow<SummaryUiState>(SummaryUiState.Idle)
    val summaryState: StateFlow<SummaryUiState> = _summaryState

    fun getThreadId(): Long? = threadId
    fun getLastEndedThreadId(): Long? = lastEndedThreadId

    fun resetChatSession(endedThreadId: Long? = null) {
        if (endedThreadId != null) {
            lastEndedThreadId = endedThreadId
        }
        threadId = null
        _messages.value = emptyList()
        _summaryState.value = SummaryUiState.Idle
    }



    //    오늘의 추천 질문
    fun startChat(content: String, userQuestionId: Long? = null) {
        if (threadId != null) {
            Log.d("ChatViewModel", "startChat skip: already threadId=$threadId")
            return
        }

        viewModelScope.launch {
            val ok = startChatInternal(content, userQuestionId)
            if (!ok) {
                Log.e("ChatViewModel", "startChat() failed")
            }
        }
    }

    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            if (threadId == null) {
                val ok = startChatInternal(content = trimmed, userQuestionId = null)
                if (!ok) {
                    addFailMessage("대화를 시작하지 못했어. 잠시 후 다시 시도해줘.")
                    return@launch
                }
            }

            // UI에 내 메시지 + typing 표시
            val now = System.currentTimeMillis()
            val myId = "me_$now"

            _messages.value = _messages.value + ChatMessage(
                id = myId,
                sender = Sender.ME,
                text = trimmed,
                time = now,
                status = Status.SENT
            )

            val typingId = "typing_$now"
            _messages.value = _messages.value + ChatMessage(
                id = typingId,
                sender = Sender.AI,
                text = "…",
                time = System.currentTimeMillis(),
                status = Status.TYPING
            )

            // 다음 단계에서 /api/chatbot으로 교체
            try {
                val currentThreadId = threadId
                if (currentThreadId == null) {
                    addFailMessage("세션 정보를 확인할 수 없어요. 잠시 후 다시 시도해줘.")
                    return@launch
                }

                val reply = repo.ask(trimmed, currentThreadId)

                _messages.value = _messages.value
                    .filterNot { it.id == typingId } +
                        ChatMessage(
                            id = "ai_${System.currentTimeMillis()}",
                            sender = Sender.AI,
                            text = reply.content,
                            time = System.currentTimeMillis(),
                            status = Status.SENT
                        )

                if (reply.isSessionEnd) {
                    resetChatSession(currentThreadId)
                    _endEvent.tryEmit(Unit)
                }
            } catch (e: Exception) {
                _messages.value = _messages.value
                    .filterNot { it.id == typingId } +
                        ChatMessage(
                            id = "ai_fail_${System.currentTimeMillis()}",
                            sender = Sender.AI,
                            text = "응답 실패… 다시 보내볼래?",
                            time = System.currentTimeMillis(),
                            status = Status.FAILED
                        )
            }
        }
    }
    private suspend fun startChatInternal(content: String, userQuestionId: Long?): Boolean {
        return try {
            Log.d("ChatViewModel", "startChatInternal request: content='$content', userQuestionId=$userQuestionId")

            val res = RetrofitClient.chatbotApi.startChat(
                StartChatRequest(content = content, userQuestionId = userQuestionId)
            )

            if (res.isSuccessful) {
                val wrapper = res.body()
                val data = wrapper?.data

                if (data == null) {
                    Log.e("ChatViewModel", "startChatInternal success but data=null")
                    false
                } else {
                    threadId = data.userChatThreadId
                    Log.d("ChatViewModel", "✅ startChatInternal success threadId=$threadId")
                    if (userQuestionId == null && data.content.isNotBlank()) {
                        seedQuestionOnly(data.content)
                    }
                    true
                }
            } else {
                val err = runCatching { res.errorBody()?.string() }.getOrNull()
                Log.e("ChatViewModel", "startChatInternal failed code=${res.code()} err=$err")
                false
            }
        } catch (e: Exception) {
            Log.e("ChatViewModel", "startChatInternal exception", e)
            false
        }
    }

    fun seedPrefillQuestion(question: String) {
        if (_messages.value.isNotEmpty()) return

        val now = System.currentTimeMillis()

        val base = ChatMessage(
            id = "ai_base_$now",
            sender = Sender.AI,
            text = "안녕하세요 지은님!\n오늘은 이런 고민이 있으시군요.\n그럼 간단한 질문 먼저 드리도록 할게요.",
            time = now,
            status = Status.SENT
        )

        val q = ChatMessage(
            id = "ai_q_${now + 1}",
            sender = Sender.AI,
            text = question,
            time = now + 1,
            status = Status.SENT
        )

        _messages.value = listOf(base, q)
    }
fun seedQuestionOnly(question: String) {
        if (_messages.value.isNotEmpty()) return

        val now = System.currentTimeMillis()

        _messages.value = listOf(
            ChatMessage(
                id = "ai_q_$now",
                sender = Sender.AI,
                text = question,
                time = now,
                status = Status.SENT
            )
        )
    }

    // 대화 중 나가기 버튼 클릭 시
    fun stopChatOnExit(){
        val id = threadId?: return
        viewModelScope.launch {
            runCatching { repo.stopChat(id) }
                .onFailure { Log.e("ChatViewModel", "stopChat failed", it) }
        }
    }

    private fun addFailMessage(msg: String) {
        _messages.value = _messages.value + ChatMessage(
            id = "sys_${System.currentTimeMillis()}",
            sender = Sender.AI,
            text = msg,
            time = System.currentTimeMillis(),
            status = Status.FAILED
        )
    }

    fun fetchSummary(accessToken : String, threadId:Long){
        viewModelScope.launch {
            _summaryState.value = SummaryUiState.Loading

            repo.fetchSummary(accessToken, threadId)
                .onSuccess { data ->
                    _summaryState.value = SummaryUiState.Success(data)
                }
                .onFailure { e ->
                    _summaryState.value = SummaryUiState.Error(e.message ?: "요약 불러오기 실패")
                }
        }
    }
}



