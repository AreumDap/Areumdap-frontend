package com.example.areumdap.data.model

import com.example.areumdap.UI.auth.Category

data class RecommendQuestion(
    val id:Long,
    val text:String,
    val tag: Category
)