package com.example.areumdap.data.model

import com.google.gson.annotations.SerializedName

data class Mission (
    @SerializedName(value = "missionId", alternate = ["id"])
    val missionId: Long,
    @SerializedName(value = "tag", alternate = ["category"])
    val tag: String,
    @SerializedName(value = "title", alternate = ["name"])
    val title: String,
    @SerializedName(value = "content", alternate = ["description", "desc"])
    val content: String,
    @SerializedName(value = "tip", alternate = ["guide"])
    val tip: String,
    @SerializedName(value = "dueDate", alternate = ["due_date"])
    val dueDate: String?,
    @SerializedName(value = "dDay", alternate = ["dday", "due_day"])
    val dDay: Int?,
    @SerializedName(value = "reward", alternate = ["rewardXp", "xp"])
    val reward: Long,
    @SerializedName(value = "status", alternate = ["missionStatus"])
    val status: String
)