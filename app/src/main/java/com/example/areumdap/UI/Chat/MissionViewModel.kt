package com.example.areumdap.UI.Chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.areumdap.data.model.Mission
import com.example.areumdap.data.repository.MissionRepository
import kotlinx.coroutines.launch

class MissionViewModel(
    private val repo : MissionRepository
) : ViewModel() {

    private val _missions = MutableLiveData<List<Mission>>(emptyList())
    val missions: LiveData<List<Mission>> = _missions

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error : LiveData<String?> = _error

    fun createMissions(threadId: Long){
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = repo.createMissions(threadId)

            result
                .onSuccess { response ->
                    _missions.value = response.missions
                }
                .onFailure { e ->
                    _error.value = e.message ?: "알 수 없는 오류"
                }

            _loading.value = false
        }
    }
}
