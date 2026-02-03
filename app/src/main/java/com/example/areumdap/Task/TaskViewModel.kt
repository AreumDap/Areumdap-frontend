package com.example.areumdap.Task

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.launch

class TaskViewModel(private val apiService: TaskApiService) : ViewModel() {
    private val _completedMissions = MutableLiveData<List<MissionItem>>()
    val completedMissions: LiveData<List<MissionItem>> = _completedMissions

    //다음 페이지 존재 여부
    private val _hasNext = MutableLiveData<Boolean>()
    val hasNext: LiveData<Boolean> = _hasNext
    //로딩 상태
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    //에러 메시지
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    // 다음 커서 정보 (페이징용)
    private var nextCursorTime: String? = null
    private var nextCursorId: Int? = null

    // 완료된 과제 조회 (초기 로딩)
    fun fetchCompletedMissions(tag: String? = null, size: Int = 100) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // request 객체를 만들지 않고, 값을 직접 순서대로 넣습니다.
                val response = apiService.getCompletedMissions(
                    tag = tag,
                    cursorTime = getCurrentTimeISO(), // 현재 시간
                    cursorId = 0,
                    size = size
                )

                if (response.isSuccessful) {
                    val missions = response.body()?.missions
                    // 데이터가 몇 개나 들어왔는지 로그캣에 찍어봅니다.
                    Log.d("API_SUCCESS_DATA", "받아온 데이터 개수: ${missions?.size ?: 0}")
                    //_completedMissions.value = missions
                    // 성공 로직 (기존과 동일)
                    _completedMissions.value = response.body()?.missions
                    // 다음 페이지를 위한 커서 저장
                    nextCursorTime = response.body()?.nextCursorTime
                    nextCursorId = response.body()?.nextCursorId
                    _hasNext.value = response.body()?.hasNext ?: false
                } else {
                    val errorDetail = response.errorBody()?.string()
                    Log.e("API_ERROR_DETAIL", "서버가 보낸 에러 내용: $errorDetail")
                    _errorMessage.value = "서버 에러: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Exception: ${e.message}")
                _errorMessage.value = "네트워크 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 다음 페이지 로딩 (스크롤 시)
    fun fetchMoreMissions(tag: String = "CAREER", size: Int = 10) {
        if (_isLoading.value == true || _hasNext.value == false) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 여기도 객체 대신 저장해둔 커서 값들을 직접 넣습니다.
                val response = apiService.getCompletedMissions(
                    tag = tag,
                    cursorTime = nextCursorTime!!,
                    cursorId = nextCursorId!!,
                    size = size
                )

                if (response.isSuccessful) {
                    response.body()?.let { data ->
                        val currentList = _completedMissions.value?.toMutableList() ?: mutableListOf()
                        currentList.addAll(data.missions)
                        _completedMissions.value = currentList

                        _hasNext.value = data.hasNext
                        nextCursorTime = data.nextCursorTime
                        nextCursorId = data.nextCursorId
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "네트워크 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 현재 시간을 ISO 8601 형식으로 반환
    private fun getCurrentTimeISO(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}