package com.example.areumdap.data.model

object DummyChatData {
    val session:List<RecordItem> = listOf(
        // 무언가 있어야함
    )

    private val messageMap:Map<Int, List<ChatMessage>> = mapOf(
        1 to listOf(
            ChatMessage("m1", Sender.ME, "오늘 너무 지쳤어", System.currentTimeMillis()),
            ChatMessage("m2", Sender.AI, "어떤 일이 제일 컸어?", System.currentTimeMillis()),

        ),
        2 to listOf(
            ChatMessage("m3", Sender.ME, "친구가 선 넘는 느낌이야", System.currentTimeMillis()),
            ChatMessage("m4", Sender.AI, "구체적으로 어떤 상황이었어?", System.currentTimeMillis()),
        )
    )

    fun getMessages(seesionId:Int) = messageMap[seesionId].orEmpty()
}