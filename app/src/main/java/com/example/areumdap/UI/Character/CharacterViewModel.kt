package com.example.areumdap.UI.Character

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.areumdap.data.api.CharacterApiService
import com.example.areumdap.data.model.CharacterCreateRequest
import com.example.areumdap.data.model.CharacterHistoryResponse
import com.example.areumdap.data.model.CharacterLevelUpResponse
import com.example.areumdap.data.model.ErrorResponse
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CharacterViewModel(private val apiService: CharacterApiService) : ViewModel() {
    // 캐릭터 레벨 데이터
    private val _characterLevel = MutableLiveData<CharacterLevelUpResponse?>()
    val characterLevel: LiveData<CharacterLevelUpResponse?> = _characterLevel

    // 캐릭터 history 데이터
    private val _historyData = MutableLiveData<CharacterHistoryResponse?>()
    val historyData: LiveData<CharacterHistoryResponse?> = _historyData

    // 로딩 상태 처리
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // 에러 메시지 처리
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // 선택된 태그 (필터링용)
    private val _selectedTag = MutableLiveData<String?>("전체")
    val selectedTag: LiveData<String?> = _selectedTag

    private val _uiState = MutableStateFlow<CharacterUiState>(CharacterUiState.Idle)
    val uiState: StateFlow<CharacterUiState> = _uiState.asStateFlow()

    fun setSelectedTag(tag: String?) {
        _selectedTag.value = tag
    }

    // 완료된 미션 ID 관리 (로컬 블랙리스트)
    private val _completedMissionIds = mutableSetOf<Int>()

    fun addCompletedMission(missionId: Int) {
        _completedMissionIds.add(missionId)
    }

    fun isMissionCompleted(missionId: Int): Boolean {
        return _completedMissionIds.contains(missionId)
    }

    //캐릭터 성장 히스토리 데이터를 가져오는 함수
    fun fetchCharacterHistory() {
        viewModelScope.launch {
            try {
                val response = apiService.getCharacterHistory()
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody?.data != null) {
                        _historyData.value = responseBody.data
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "네트워크 오류가 발생했습니다"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchCharacterLevel() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.postCharacterLevel()

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("DEBUG_API", "fetchCharacterLevel Body: $body")
                    if (body?.data != null) {
                        var newData = body.data

                        // 이미지 URL이 null이면 이전 레벨 정보를 바탕으로 유추
                        if (newData.imageUrl == null) {
                            val oldUrl = _characterLevel.value?.imageUrl
                            if (oldUrl != null) {
                                try {
                                    val regex = "(.*stage)(\\d+)(\\.png)".toRegex()
                                    val match = regex.find(oldUrl)
                                    if (match != null) {
                                        val (prefix, numStr, suffix) = match.destructured
                                        val nextNum = numStr.toInt() + 1
                                        val newUrl = "$prefix$nextNum$suffix"
                                        newData = newData.copy(imageUrl = newUrl)
                                    }
                                } catch (e: Exception) {
                                    // Ignore inference errors
                                }
                            }
                        }

                        _characterLevel.value = newData

                        // 레벨업/정보 갱신 성공 시 히스토리 요약 업데이트 요청
                        try {
                            val summaryResponse = apiService.postCharacterHistorySummary()
                            if (summaryResponse.isSuccessful) {
                                Log.d("DEBUG_API", "History Summary Updated")
                            } else {
                                Log.e(
                                    "DEBUG_API",
                                    "History Summary Update Failed: ${summaryResponse.code()}"
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("DEBUG_API", "History Summary Update Exception", e)
                        }

                    } else {
                        Log.e("DEBUG_API", "fetchCharacterLevel Body Data is NULL")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("DEBUG_API", "fetchCharacterLevel Error: $errorBody")
                    _errorMessage.value = "레벨 정보를 가져오는데 실패했습니다 (${response.code()})"
                }
            } catch (e: Exception) {
                Log.e("DEBUG_API", "fetchCharacterLevel Exception", e)
                _errorMessage.value = "네트워크 오류가 발생했습니다"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 히스토리 요약 생성 요청 (명시적 호출용)
    fun requestHistorySummary() {
        viewModelScope.launch {
            try {
                val response = apiService.postCharacterHistorySummary()
                if (response.isSuccessful) {
                    Log.d("DEBUG_API", "History Summary Generated Successfully")
                    // 생성 후 다시 조회
                    fetchCharacterHistory()
                } else {
                    Log.e("DEBUG_API", "History Summary Generation Failed: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("DEBUG_API", "History Summary Generation Exception", e)
            }
        }
    }

    fun fetchMyCharacter() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getMycharacter()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.data != null) {
                        _characterLevel.value = body.data
                    }
                } else {
                    _errorMessage.value = "캐릭터 정보를 가져오는데 실패"
                }
            } catch (e: Exception) {
                _errorMessage.value = "네트워크 오류가 발생"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 캐릭터 경험치 로컬 업데이트
    fun addXp(amount: Int) {
        val currentData = _characterLevel.value ?: return
        val currentXp = currentData.currentXp
        val maxXp = currentData.maxXp

        // 경험치 증가
        var newXp = currentXp + amount
        // if (maxXp > 0 && newXp > maxXp) {     <-- 이 부분 삭제
        //    newXp = maxXp
        // }

        val newData = currentData.copy(currentXp = newXp)
        _characterLevel.value = newData
    }


    // 캐릭터 생성
    fun createCharacter(season: String, keywords: List<String>) {
        viewModelScope.launch {
            _uiState.value = CharacterUiState.Loading

            try {
                val request = CharacterCreateRequest(
                    characterSeason = season,
                    keywords = keywords,
                    keywordType = "PRESET"
                )

                val response = apiService.createCharacter(request)

                val body = response.body()

                if (response.isSuccessful && body?.data != null) {
                    val data = body.data!!
                    _uiState.value = CharacterUiState.Success(
                        characterId = data.characterId,
                        imageUrl = data.imageUrl
                    )
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = try {
                        Gson().fromJson(errorBody, ErrorResponse::class.java).message
                    } catch (e: Exception) {
                        response.body()?.message ?: "캐릭터 생성에 실패했습니다."
                    }
                    _uiState.value = CharacterUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = CharacterUiState.Error(
                    e.message ?: "네트워크 오류가 발생했습니다."
                )
            }
        }
    }

    fun resetUiState() {
        _uiState.value = CharacterUiState.Idle
    }
}

sealed class CharacterUiState {
    object Idle : CharacterUiState()
    object Loading : CharacterUiState()
    data class Success(val characterId: Int, val imageUrl: String?) : CharacterUiState()
    data class Error(val message: String) : CharacterUiState()
}