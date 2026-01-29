package com.example.areumdap.domain.model

data class RecordItem(
    val id: Long,
    val category: Category,   // 자기성찰/성장/관계
    val title: String,
    val summary: String,
    val dateText: String,           // "2025. 10. 29"
    val isStarred: Boolean
)
