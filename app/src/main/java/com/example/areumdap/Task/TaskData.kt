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

