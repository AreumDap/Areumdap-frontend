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

data class CharacterLevelUpResponse(
    @SerializedName("characterId")
    val characterId: Int,
    @SerializedName("characterName")
    val characterName: String,
    @SerializedName("previousLevel") // 이전 레벨
    val previousLevel: Int,
    @SerializedName("currentLevel") //현재 레벨
    val currentLevel: Int,
    @SerializedName("currentXp") // 현재 경험치
    val currentXp: Int,
    @SerializedName("requiredXpForNextLevel") // 다음 레벨까지 필요한 경험치
    val requiredXpForNextLevel: Int
)