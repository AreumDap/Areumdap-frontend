package com.example.areumdap.UI.Chat.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.areumdap.Data.repository.MissionRepository
import kotlinx.coroutines.launch

class MissionViewModel(
    private val repo : MissionRepository
) : ViewModel() {

    private val _missions = MutableLiveData<List<Mission>>()
    val missions: LiveData<List<Mission>> = _missions

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error : LiveData<String?> = _error

    fun createMissions(threadId: Long, accessToken:String){
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = repo.createMission(threadId, accessToken)

            result
                .onSuccess { _missions.value = it }
                .onFailure { _error.value = it.message ?: "알 수 없는 오류" }

            _loading.value = false
        }
    }
}