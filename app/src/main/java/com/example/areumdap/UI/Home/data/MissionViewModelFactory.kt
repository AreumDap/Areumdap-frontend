package com.example.areumdap.UI.Home.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.areumdap.Data.repository.MissionRepository
import com.example.areumdap.UI.Chat.data.MissionViewModel

class MissionViewModelFactory(
    private val repo: MissionRepository
) : ViewModelProvider.Factory {

    override fun<T: ViewModel> create(modelClass:Class<T>) : T{
        return MissionViewModel(repo) as T
    }
}