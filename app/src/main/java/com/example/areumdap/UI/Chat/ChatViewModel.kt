package com.example.areumdap.UI.Chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.areumdap.data.api.ChatReportApiService
import com.example.areumdap.data.model.ChatMessage
import com.example.areumdap.data.model.ChatSummaryData
import com.example.areumdap.data.model.Sender
import com.example.areumdap.data.model.StartChatRequest
import com.example.areumdap.data.model.Status
import com.example.areumdap.data.repository.ChatRepository
import com.example.areumdap.data.repository.ChatRepositoryImpl
import com.example.areumdap.data.source.RetrofitClient
import com.example.areumdap.data.source.RetrofitClient.chatbotApiService
import com.example.areumdap.data.source.TokenManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.internal.http.hasBody

sealed interface SummaryUiState{
    data object Idle: SummaryUiState
    data object Loading : SummaryUiState
    data class Success(val data : ChatSummaryData) : SummaryUiState
    data class Error(val message:String) : SummaryUiState
}

sealed interface ReportCreateUiState {
    data object Idle : ReportCreateUiState
    data object Loading : ReportCreateUiState
    data class Success(val reportId: Long) : ReportCreateUiState
    data class Error(val message: String) : ReportCreateUiState
}

class ChatViewModel(
    private val repo: ChatRepository = ChatRepositoryImpl(RetrofitClient.chatbotApiService)
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val message: StateFlow<List<ChatMessage>> = _messages

    private val _endEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val endEvent: SharedFlow<Unit> = _endEvent

    private var threadId: Long? = null
    private var lastEndedThreadId: Long? = null
    private var lastRecommendTag: String? = null
    private var sessionEnded: Boolean = false

    private val _summaryState = MutableStateFlow<SummaryUiState>(SummaryUiState.Idle)
    val summaryState: StateFlow<SummaryUiState> = _summaryState

    private val _reportCreateState = MutableStateFlow<ReportCreateUiState>(ReportCreateUiState.Idle)
    val reportCreateState: StateFlow<ReportCreateUiState> = _reportCreateState
    fun getThreadId(): Long? = threadId
    fun getLastEndedThreadId(): Long? = lastEndedThreadId
    fun getLastRecommendTag(): String? = lastRecommendTag
    fun setRecommendTag(tag: String?) {
        lastRecommendTag = tag
    }

    fun resetChatSession(endedThreadId: Long? = null) {
        if (endedThreadId != null) {
            lastEndedThreadId = endedThreadId
        }
        threadId = null
        _messages.value = emptyList()
        _summaryState.value = SummaryUiState.Idle
        lastRecommendTag = null
        sessionEnded = false
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

    // 메시지 전송
    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            if (sessionEnded) {
                addFailMessage("대화가 종료되었어요. 과제를 확인하거나 새 대화를 시작해줘.")
                return@launch
            }
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
            showTyping(typingId)

            // 다음 단계에서 /api/chatbot으로 교체
            try {
                val currentThreadId = threadId
                if (currentThreadId == null) {
                    addFailMessage("세션 정보를 확인할 수 없어요. 잠시 후 다시 시도해줘.")
                    return@launch
                }

                val reply = repo.ask(trimmed, currentThreadId)

                val parts = splitToBubbles(reply.content)

                val base = System.currentTimeMillis()
                for ((i, part) in parts.withIndex()) {
                    val isLast = i == parts.lastIndex

                    _messages.value = _messages.value.filterNot { it.id == typingId }

                    val aiMsgs = ChatMessage(
                            id = "ai_${base}_$i",
                            sender = Sender.AI,
                            text = part,
                            time = base + i,
                            status = Status.SENT,
                            chatHistoryId = if (i == parts.lastIndex) reply.chatHistoryId else null
                        )
                    _messages.value = _messages.value+aiMsgs
                    if (!isLast) {
                        showTyping(typingId)
                        delay(1000L)
                    }

                }

                _messages.value = _messages.value
                    .filterNot { it.id == typingId }

                val lastAiId = "ai_${base}_${parts.lastIndex}"
                if (reply.chatHistoryId == null && lastAiId != null) {
                    attachChatHistoryId(currentThreadId, lastAiId, reply.content)
                }

                if (reply.isSessionEnd) {
                    // runCatching { repo.stopChat(currentThreadId) }
                    //     .onFailure { Log.e("ChatViewModel", "stopChat failed", it) }
                    lastEndedThreadId = currentThreadId
                    sessionEnded = true
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

    // 구분자 기준으로 채팅 버블 나누기
    private fun splitToBubbles(text: String): List<String> {
        // . ? ! 뒤에서 끊고, 공백/줄바꿈은 무시
        val regex = Regex("(?<=[.!?])\\s+")
        return text.trim()
            .split(regex)
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }


    private suspend fun startChatInternal(content: String, userQuestionId: Long?): Boolean {
        return try {
            Log.d("ChatViewModel", "startChatInternal request: content='$content', userQuestionId=$userQuestionId")

            val res = chatbotApiService.startChat(
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

    private val prefillBaseTemplates = listOf(
        "안녕하세요 {nick}님!\n오늘 나누고 싶은 이야기가 있으시군요.\n몇 가지 질문을 통해 함께 생각해볼게요.",
        "반가워요 {nick}님!\n오늘의 이야기를 시작해볼까요?",
        "다시 만나서 반가워요, {nick}님.\n선택하신 질문으로 대화를 시작해볼게요.",
        "안녕하세요, {nick}님.\n오늘은 이 질문을 중심으로 이야기를 시작해볼게요.",
        "안녕하세요, {nick}님.\n이 질문이 눈에 들어온 데에는 이유가 있을지도 모르겠어요.\n함께 살펴볼게요."
    )

    // 대화 바로 시작할 때 자동으로 나오는 질문
    fun seedPrefillQuestion(question: String, nickname: String? = null) {
        if (_messages.value.isNotEmpty()) return

        val now = System.currentTimeMillis()
        val nick = nickname
            ?: TokenManager.getUserNickname()
            ?: TokenManager.getUserName()
            ?: "사용자"

        val baseText = prefillBaseTemplates.random()
            .replace("{nick}", nick)

        val base = ChatMessage(
            id = "ai_base_$now",
            sender = Sender.AI,
            text = baseText,
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
        Log.d("ChatExit", "stopChatOnExit threadId=$id")
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

    private fun showTyping(typingId: String) {
        val typing = ChatMessage(
            id = typingId,
            sender = Sender.AI,
            text = "…",
            time = System.currentTimeMillis(),
            status = Status.TYPING
        )
        _messages.value = _messages.value
            .filterNot { it.id == typingId }
            .plus(typing)
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

    fun saveQuestion(chatHistoryId : Long){
        viewModelScope.launch {
            val token = TokenManager.getAccessToken()?.toString().orEmpty() // 너희 방식
            if (token.isNullOrBlank()) return@launch

            repo.saveQuestion(chatHistoryId)
                .onSuccess { res ->
                    Log.d("ChatViewModel", "saveQuestion success: ${res.message}") }
                .onFailure {  e ->
                    Log.e("ChatViewModel", "saveQuestion failed", e) }
        }
    }

    private suspend fun attachChatHistoryId(threadId: Long, aiMessageId: String, content: String) {
        runCatching {
            val api = RetrofitClient.create(ChatReportApiService::class.java)
            val res = api.getThreadHistories(threadId)
            if (!res.isSuccessful) return@runCatching
            val body = res.body() ?: return@runCatching
            val data = body.data ?: return@runCatching

            val match = data.histories
                .asReversed()
                .firstOrNull { it.senderType.equals("BOT", ignoreCase = true) && it.content == content }
                ?: return@runCatching

            _messages.value = _messages.value.map { msg ->
                if (msg.id == aiMessageId) msg.copy(chatHistoryId = match.id) else msg
            }
        }.onFailure { e ->
            Log.e("ChatViewModel", "attachChatHistoryId failed", e)
        }
    }

    fun createReport(){
        val id = lastEndedThreadId ?: threadId ?: return

        viewModelScope.launch {
            _reportCreateState.value = ReportCreateUiState.Loading

            repo.createReport(id)
                .onSuccess { body ->
                    val reportId = body.reportId
                    _reportCreateState.value = ReportCreateUiState.Success(reportId)
                }
                .onFailure { e ->
                    _reportCreateState.value = ReportCreateUiState.Error(e.message ?: "과제 생성 실패")
                }
        }
    }
}
