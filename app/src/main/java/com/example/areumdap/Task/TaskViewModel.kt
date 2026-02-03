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
    fun fetchCompletedMissions(tag: String? = null, size: Int = 100) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getCompletedMissions(
                    tag = tag,
                    cursorTime = getCurrentTimeISO(), // 현재 시간
                    cursorId = 0,
                    size = size
                )

                if (response.isSuccessful) {
                    val missions = response.body()?.missions
                    Log.d("API_SUCCESS_DATA", "받아온 데이터 개수: ${missions?.size ?: 0}")
                    //_completedMissions.value = missions
                    // 성공 로직
                    _completedMissions.value = response.body()?.missions

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

                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        val data = responseBody.data
                        val questions = data.questions

                        Log.d("QUESTION_API_SUCCESS", "받아온 질문 개수: ${questions.size}")

                        _savedQuestions.value = questions
                        questionsNextCursorTime = data.nextCursorTime
                        questionsNextCursorId = data.nextCursorId
                        _questionsHasNext.value = data.hasNext
                    }
                } else {
                    val errorDetail = response.errorBody()?.string()
                    Log.e("QUESTION_API_ERROR", "서버 에러: $errorDetail")
                    _errorMessage.value = "질문을 가져오는데 실패했습니다: ${response.code()}"
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
                    response.body()?.let { responseBody ->  // ✅ 첫 번째 let에서 responseBody 가져오기
                        val data = responseBody.data        // ✅ data 객체 가져오기

                        val currentList = _savedQuestions.value?.toMutableList() ?: mutableListOf()
                        currentList.addAll(data.questions)  // ✅ data.questions 사용
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
}