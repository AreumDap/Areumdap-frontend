package com.example.areumdap.data.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.areumdap.UI.Character.CharacterViewModel
import com.example.areumdap.data.api.CharacterApiService

class CharacterViewModelFactory(private val apiService: CharacterApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CharacterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CharacterViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}