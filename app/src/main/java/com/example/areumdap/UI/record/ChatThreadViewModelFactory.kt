package com.example.areumdap.UI.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.areumdap.Data.repository.ChatReportRepository

class ChatThreadViewModelFactory(
    private val repo : ChatReportRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatThreadViewModel::class.java)){
            @Suppress("UNCECKED_CATS")
            return ChatThreadViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}