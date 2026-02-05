package com.example.areumdap.Task

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.launch

class TaskViewModel(private val apiService: TaskApiService) : ViewModel() {
    // 저장한 질문
    private val _savedQuestions = MutableLiveData<List<QuestionItem>>()
    val savedQuestions: LiveData<List<QuestionItem>> = _savedQuestions

    private val _questionsHasNext = MutableLiveData<Boolean>()
    val questionsHasNext: LiveData<Boolean> = _questionsHasNext
    // 완료 과제
    private val _completedMissions = MutableLiveData<List<MissionItem>>()
    val completedMissions: LiveData<List<MissionItem>> = _completedMissions
    // 전체 과제 개수
    private val _totalMissionCount = MutableLiveData<Int>(0)
    val totalMissionCount: LiveData<Int> = _totalMissionCount
    // 저장한 질문
    private val _questionTotalCount = MutableLiveData<Int>(0)
    val questionTotalCount: LiveData<Int> = _questionTotalCount
    //다음 페이지 존재 여부
    private val _hasNext = MutableLiveData<Boolean>()
    val hasNext: LiveData<Boolean> = _hasNext
    //로딩 상태
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    //에러 메시지
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    // 다음 커서 정보 과제 (페이징용)
    private var nextCursorTime: String? = null
    private var nextCursorId: Int? = null
    // 다음 커서 정보 질문 (페이징용)
    private var questionsNextCursorTime: String? = null
    private var questionsNextCursorId: Int? = null

    // 완료된 과제 조회
    fun fetchCompletedMissions(tag: String? = null, size: Int = 20) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getCompletedMissions(
                    tag = tag,
                    cursorTime = getCurrentTimeISO(),
                    cursorId = 0,
                    size = size
                )

                Log.d("TaskViewModel", "fetchCompletedMissions Response Code: ${response.code()}")
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("TaskViewModel", "isSuccess: ${body?.isSuccess}")
                    body?.data?.let { data ->
                        Log.d("TaskViewModel", "Total: ${data.totalCount}, Missions: ${data.missions.size}")
                        _totalMissionCount.value = data.totalCount

                        _completedMissions.value = data.missions
                        nextCursorTime = data.nextCursorTime
                        nextCursorId = data.nextCursorId
                        _hasNext.value = data.hasNext
                    } ?: run {
                        Log.e("TaskViewModel", "Data is null!")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    Log.e("TaskViewModel", "Error Body: $errorBody")
                    _errorMessage.value = "서버 에러(${response.code()}): $errorBody"
                }
            } catch (e: Exception) {
                _errorMessage.value = "네트워크 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 다음 페이지 로딩
    fun fetchMoreMissions(tag: String = "CAREER", size: Int = 10) {
        if (_isLoading.value == true || _hasNext.value == false) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getCompletedMissions(
                    tag = tag,
                    cursorTime = nextCursorTime!!,
                    cursorId = nextCursorId!!,
                    size = size
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    body?.data?.let { data ->
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

    // 저장한 질문 조회
    fun fetchSavedQuestions(tag: String? = null, size: Int = 100){
        viewModelScope.launch {
            _isLoading.value = true
            try{
                val response = apiService.getSavedQuestions(
                    tag = tag,
                    cursorTime = getCurrentTimeISO(),
                    cursorId = 0,
                    size = size
                )

                Log.d("API_CHECK", "Status Code: ${response.code()}")

                Log.d("TaskViewModel", "fetchSavedQuestions Response Code: ${response.code()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    responseBody?.data?.let { data ->
                        _questionTotalCount.value = data.totalCount

                        Log.d("TaskViewModel", "Total Questions: ${data.totalCount}, Loaded: ${data.questions.size}")

                        _savedQuestions.value = data.questions
                        questionsNextCursorTime = data.nextCursorTime
                        questionsNextCursorId = data.nextCursorId
                        _questionsHasNext.value = data.hasNext
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    Log.e("TaskViewModel", "Question Server Error: ${response.code()}, Body: $errorBody")
                    _errorMessage.value = "서버 에러(${response.code()}): $errorBody"
                }
            } catch (e: Exception) {
                Log.e("QUESTION_API_ERROR", "Exception: ${e.message}")
                _errorMessage.value = "네트워크 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 다음 페이지 질문 로딩
    fun fetchMoreQuestions(tag: String? = null, size: Int = 10) {
        if (_isLoading.value == true || _questionsHasNext.value == false) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getSavedQuestions(
                    tag = tag,
                    cursorTime = questionsNextCursorTime!!,
                    cursorId = questionsNextCursorId!!,
                    size = size
                )

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    responseBody?.data?.let { data ->
                        val currentList = _savedQuestions.value?.toMutableList() ?: mutableListOf()
                        currentList.addAll(data.questions)
                        _savedQuestions.value = currentList

                        _questionsHasNext.value = data.hasNext
                        questionsNextCursorTime = data.nextCursorTime
                        questionsNextCursorId = data.nextCursorId
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

    // 과제 삭제 (로컬)
    fun deleteCompletedMission(missionId: Int) {
        val currentList = _completedMissions.value?.toMutableList() ?: return
        val itemToRemove = currentList.find { it.missionId == missionId }

        if (itemToRemove != null) {
            currentList.remove(itemToRemove)
            _completedMissions.value = currentList
            
            // 토탈 개수 감소
            val currentCount = _totalMissionCount.value ?: 0
            if (currentCount > 0) {
                _totalMissionCount.value = currentCount - 1
            }
        }
    }

    // 질문 삭제 (로컬)
    fun deleteSavedQuestion(userQuestionId: Int) {
        val currentList = _savedQuestions.value?.toMutableList() ?: return
        val itemToRemove = currentList.find { it.userQuestionId == userQuestionId }

        if (itemToRemove != null) {
            currentList.remove(itemToRemove)
            _savedQuestions.value = currentList

            // 토탈 개수 감소
            val currentCount = _questionTotalCount.value ?: 0
            if (currentCount > 0) {
                _questionTotalCount.value = currentCount - 1
            }
        }
    }
}