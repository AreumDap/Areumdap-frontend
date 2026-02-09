package com.example.areumdap.UI.Chat.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.areumdap.Data.repository.ChatRepository
import com.example.areumdap.Data.api.ChatSummaryData
import com.example.areumdap.Data.api.StartChatRequest
import com.example.areumdap.Data.repository.ChatRepositoryImpl
import com.example.areumdap.Data.api.ChatReportApiService
import com.example.areumdap.Data.api.ReportResponse
import com.example.areumdap.Network.RetrofitClient
import com.example.areumdap.Network.TokenManager
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

sealed interface ReportUiState {
    data object Idle : ReportUiState
    data object Loading : ReportUiState
    data class Success(val data: ReportResponse) : ReportUiState
    data class Error(val message: String) : ReportUiState
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
    private var lastRecommendTag: String? = null

    private val _summaryState = MutableStateFlow<SummaryUiState>(SummaryUiState.Idle)
    val summaryState: StateFlow<SummaryUiState> = _summaryState

    private val _reportState = MutableStateFlow<ReportUiState>(ReportUiState.Idle)
    val reportState : StateFlow<ReportUiState> = _reportState

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
        _reportState.value = ReportUiState.Idle
        lastRecommendTag = null

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

                val aiMessageId = "ai_${System.currentTimeMillis()}"
                _messages.value = _messages.value
                    .filterNot { it.id == typingId } +
                        ChatMessage(
                            id = aiMessageId,
                            sender = Sender.AI,
                            text = reply.content,
                            time = System.currentTimeMillis(),
                            status = Status.SENT,
                            chatHistoryId = reply.chatHistoryId
                        )

                if (reply.chatHistoryId == null) {
                    attachChatHistoryId(currentThreadId, aiMessageId, reply.content)
                }

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

            val res = RetrofitClient.chatbotApiService.startChat(
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
        "안녕하세요 {name}님!\n오늘 나누고 싶은 이야기가 있으시군요.\n몇 가지 질문을 통해 함께 생각해볼게요.",
        "반가워요 {name}님!\n오늘의 이야기를 시작해볼까요?",
        "다시 만나서 반가워요, {name}님.\n선택하신 질문으로 대화를 시작해볼게요.",
        "안녕하세요, {name}님.\n오늘은 이 질문을 중심으로 이야기를 시작해볼게요.",
        "안녕하세요, {name}님.\n이 질문이 눈에 들어온 데에는 이유가 있을지도 모르겠어요.\n함께 살펴볼게요."
    )

    fun seedPrefillQuestion(question: String) {
        if (_messages.value.isNotEmpty()) return

        val now = System.currentTimeMillis()
        val name = TokenManager.getUserName().orEmpty().ifBlank { "사용자" }

        val baseText = prefillBaseTemplates.random()
            .replace("{name}", name)

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

    fun createReportForSummaryScreen(){
        val id = lastEndedThreadId ?: threadId ?: run{
            Log.w("ChatViewModel", "createReportForSummaryScreen: no threadId (lastEndedThreadId=null, threadId=null)")
            return
        }

        viewModelScope.launch {
            _reportState.value = ReportUiState.Loading

            repo.createReport(id)
                .onSuccess { data ->
                    Log.d("ChatViewModel", "createReport success reportId=${data.reportId}")
                    _reportState.value = ReportUiState.Success(data)
                }
                .onFailure { e ->
                    Log.e("ChatViewModel", "createReport failed", e)
                    _reportState.value = ReportUiState.Error(e.message ?: "레포트 생성 실패")
                }
        }
    }
}




