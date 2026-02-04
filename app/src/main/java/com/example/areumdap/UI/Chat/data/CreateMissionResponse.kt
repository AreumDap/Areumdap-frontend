package com.example.areumdap.UI.Chat.data

import com.example.areumdap.UI.Chat.data.Mission

data class CreateMissionResponse(
    val missionIds:List<Long>,
    val missions:List<Mission>
)