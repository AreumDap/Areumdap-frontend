package com.example.areumdap.Task

import com.google.gson.annotations.SerializedName

data class MissionItem(
    @SerializedName("missionId")
    val missionId: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("tag")
    val tag: String,

    @SerializedName("reward")
    val reward: Int,

    @SerializedName("dDay")
    val dDay: Int?,

    @SerializedName("isCompleted")
    val isCompleted: Boolean? = false,

    @SerializedName("completedAt")
    val completedAt: String?
)

// 완료된 과제 조회 api 응답
data class CompletedMissionsResponse(
    @SerializedName("missions")
    val missions: List<MissionItem>,

    @SerializedName("totalCount")
    val totalCount: Int,

    @SerializedName("nextCursorTime")
    val nextCursorTime: String?,

    @SerializedName("nextCursorId")
    val nextCursorId: Int?,

    @SerializedName("hasNext")
    val hasNext: Boolean
)

// 완료된 과제 조회 요청
data class CompletedMissionsRequest(
    @SerializedName("tag")
    val tag: String,

    @SerializedName("cursorTime")
    val cursorTime: String,

    @SerializedName("cursorId")
    val cursorId: Int,

    @SerializedName("size")
    val size: Int = 10
)

// 미션 상세 조회 응답
data class MissionDetailResponse(
    @SerializedName("missionId")
    val missionId: Int,

    @SerializedName("tag")
    val tag: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("rewardXp")
    val rewardXp: Int,

    @SerializedName("dDay")
    val dDay: Int?,

    @SerializedName("status")
    val status: String?,

    @SerializedName("guide")
    val guide: String?
)

// 개별 질문 아이템
data class QuestionItem(
    @SerializedName("userQuestionId")
    val userQuestionId: Int,

    @SerializedName("questionId")
    val questionId: Int,

    @SerializedName("content")
    val content: String,

    @SerializedName("tag")
    val tag: String,

    @SerializedName("createdAt")
    val createdAt: String
)
// 저장한 질문 목록 조회 api응답 (BaseResponse의 data 필드로 매핑됨)
data class SavedQuestionsResponse(
    @SerializedName("totalCount")
    val totalCount: Int,

    @SerializedName("questions")
    val questions: List<QuestionItem>,

    @SerializedName("nextCursorTime")
    val nextCursorTime: String?,

    @SerializedName("nextCursorId")
    val nextCursorId: Int?,

    @SerializedName("hasNext")
    val hasNext: Boolean
)
// 저장한 질문 조회 요청
data class SavedQuestionRequest(
    @SerializedName("tag")
    val tag: String?,

    @SerializedName("cursorTime")
    val cursorTime: String,

    @SerializedName("cursorId")
    val cursorId: Int,

    @SerializedName("size")
    val size: Int = 10
)

// 캐릭터 조회 응답
data class CharacterResponse(
    @SerializedName("characterId")
    val characterId: Int,

    @SerializedName("nickname")
    val nickname: String,

    @SerializedName("level")
    val level: Int,

    @SerializedName("currentXp")
    val currentXp: Int,

    @SerializedName("goalXp")
    val goalXp: Int,

    @SerializedName("hasLevelUpParams")
    val hasLevelUpParams: Boolean,

    @SerializedName("missions")
    val missions: List<MissionItem>,

    @SerializedName("imageUrl")
    val imageUrl: String
)
