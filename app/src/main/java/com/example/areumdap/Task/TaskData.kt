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

    @SerializedName("completedAt")
    val completedAt: String
)

// 완료된 과제 조회 api 응답
data class CompletedMissionsResponse(
    @SerializedName("missions")
    val missions: List<MissionItem>,

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
// 저장한 질문 목록 조회 api응답
data class SavedQuestionsResponse(
    @SerializedName("isSuccess")
    val isSuccess: Boolean,

    @SerializedName("code")
    val code: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: QuestionData
)

data class QuestionData(
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
