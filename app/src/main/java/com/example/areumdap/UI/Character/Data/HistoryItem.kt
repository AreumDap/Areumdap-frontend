package com.example.areumdap.UI.Character.Data

import com.google.gson.annotations.SerializedName

data class HistoryItem(
    @SerializedName("level")
    val level: Int,
    @SerializedName("achievedDate")
    val achievedDate: String,
    @SerializedName("imageUrl")
    val imageUrl: String
)

data class CharacterHistoryResponse(
    @SerializedName("pastDescription")
    val pastDescription: String,    // 과거의 고민 설명
    @SerializedName("presentDescription")
    val presentDescription: String, // 현재의 성장 설명
    @SerializedName("historyList")
    val historyList: List<HistoryItem> // 성장 기록 리스트
)