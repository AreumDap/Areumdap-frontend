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
    val pastDescription: String?,    // 과거의 고민 설명
    @SerializedName("presentDescription")
    val presentDescription: String?, // 현재의 성장 설명
    @SerializedName("historyList")
    val historyList: List<HistoryItem>? // 성장 기록 리스트
)

data class CharacterLevelUpResponse(
    @SerializedName("characterId")
    val characterId: Int,
    @SerializedName("characterName") // POST /level 에서는 characterName
    val characterName: String?,
    @SerializedName("nickname") // GET /me 에서는 nickname
    val nickname: String?,
    @SerializedName("previousLevel")
    val previousLevel: Int?,
    @SerializedName("currentLevel")
    val currentLevel: Int?,
    @SerializedName("level") // GET /me 에서는 level
    val level: Int?,
    @SerializedName("currentXp")
    val currentXp: Int,
    @SerializedName("requiredXpForNextLevel") // POST /level
    val requiredXpForNextLevel: Int?,
    @SerializedName("goalXp") // GET /me
    val goalXp: Int?,
    @SerializedName("imageUrl")
    val imageUrl: String?
)