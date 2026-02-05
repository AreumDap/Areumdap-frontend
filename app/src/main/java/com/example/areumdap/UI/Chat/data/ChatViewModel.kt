package com.example.areumdap.UI.Chat.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.areumdap.Data.ChatRepository
import com.example.areumdap.Data.FakeChatRepository
import com.example.areumdap.Network.RetrofitClient
import com.example.areumdap.domain.model.ChatMessage
import com.example.areumdap.domain.model.Sender
import com.example.areumdap.domain.model.Status
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.collections.plus
import com.example.areumdap.UI.Chat.data.StartChatRequest

class ChatViewModel(
    private val repo: ChatRepository = FakeChatRepository()
) : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val message: StateFlow<List<ChatMessage>> = _messages

    private  val _endEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val endEvent : SharedFlow<Unit> = _endEvent

    private var threadId: Long? = null

    fun startChat(prefill : String, userQuestionId : Long){
        if(threadId != null) return

        viewModelScope.launch {
            try{
                val res = RetrofitClient.chatbotApi.startChat(
                    StartChatRequest(
                        content = prefill,
                        userQuestionId = userQuestionId
                    )
                )

                if (res.isSuccessful) {
                    val wrapper = res.body()
                    val data = wrapper?.data

                    Log.d("ChatViewModel", "startChat wrapper=$wrapper")

                    if (data == null) {
                        Log.e("ChatViewModel", "startChat 성공인데 data=null")
                        return@launch
                    }

                    threadId = data.userChatThreadId
                    Log.d("ChatViewModel", "startChat success threadId=$threadId")
                } else {
                    val errorText = runCatching { res.errorBody()?.string() }.getOrNull()
                    Log.e("ChatViewModel", "startChat failed code=${res.code()} msg=${res.message()} errorBody=$errorText")
                }
            } catch(e: Exception){
                Log.e("ChatViewModel", "startChat error", e)

            }
        }
    }

    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return

        if(trimmed =="대화끝" || trimmed =="대화 끝"){
            _messages.value = _messages.value + ChatMessage(
                id = "me_${System.currentTimeMillis()}",
                sender = Sender.ME,
                text = trimmed,
                time = System.currentTimeMillis(),
                status = Status.SENT
            )

            _endEvent.tryEmit(Unit)
            return
        }

        val now = System.currentTimeMillis()
        val myId = "me_$now"

//       내 메시지 추가
        _messages.value = _messages.value + ChatMessage(
            id = myId,
            sender = Sender.ME,
            text = trimmed,
            time = now,
            status = Status.SENT
        )
//    AI 타이핑 메시지 추가
        val typingId = "typing_${now}"
        _messages.value = _messages.value + ChatMessage(
            id = typingId,
            sender = Sender.AI,
            text = "…",
            time = System.currentTimeMillis(),
            status = Status.TYPING
        )

        viewModelScope.launch {
            try {
                val reply = repo.ask(trimmed)

                _messages.value = _messages.value
                    .filterNot { it.id == typingId } +
                        ChatMessage(
                            id = "ai_${System.currentTimeMillis()}",
                            sender = Sender.AI,
                            text = reply,
                            time = System.currentTimeMillis(),
                            status = Status.SENT
                        )
            } catch (e: Exception){
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

    fun seedPrefillQuestion(question: String) {
        if(_messages.value.isNotEmpty()) return

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

}
