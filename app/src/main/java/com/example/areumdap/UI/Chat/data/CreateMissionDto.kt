package com.example.areumdap.UI.Chat.data

data class CreateMissionRequest(
    @com.google.gson.annotations.SerializedName("userChatThreadId")
    val threadId: Long
)

data class CreateMissionResponse(
    val missions: List<Mission>
)
