package com.example.areumdap.UI.Chat.data

import com.google.gson.annotations.SerializedName

data class CreateMissionRequest(
    val userChatThreadId: Long
)

data class CreateMissionResponse(
    @SerializedName("userChatThreadId")
    val userChatThreadId: Long,
    @SerializedName(value = "missions", alternate = ["mission"])
    val missions: List<Mission> = emptyList()
)
