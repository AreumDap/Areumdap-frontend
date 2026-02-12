package com.example.areumdap.data.model

import com.example.areumdap.data.model.Mission
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
