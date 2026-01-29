package com.example.areumdap.Data

interface ChatRepository {
    suspend fun ask(text:String) : String
}

class FakeChatRepository: ChatRepository{
    override suspend fun ask(text:String): String{
        kotlinx.coroutines.delay(900)
        return when{
            text.contains("안녕") -> "반가워요! 대화를 시작해보아요"
            text.contains("질문") -> "질문질문질문"
            text.length < 6 -> "조금만 더 자세히 말해줘!"
            else -> "좋아. 네 말 요약하면: \"$text\" 맞지?"

        }
    }
}