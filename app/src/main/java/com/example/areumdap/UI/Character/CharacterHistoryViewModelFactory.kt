package com.example.areumdap.UI.Character

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.areumdap.UI.Character.CharacterHistoryApiService

class CharacterHistoryViewModelFactory(private val apiService: CharacterHistoryApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // 생성하려는 클래스가 CharacterHistoryViewModel인지 확인
        if (modelClass.isAssignableFrom(CharacterHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CharacterHistoryViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}