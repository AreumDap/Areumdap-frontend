package com.example.areumdap.UI.Character

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.areumdap.UI.Character.Data.CharacterHistoryResponse
import com.example.areumdap.UI.Character.Data.CharacterLevelUpResponse
import kotlinx.coroutines.launch

class CharacterViewModel(private val apiService: CharacterApiService) : ViewModel() {
    // 캐릭터 레벨 데이터
    private val _characterLevel = MutableLiveData<CharacterLevelUpResponse?>()
    val characterLevel : LiveData<CharacterLevelUpResponse?> = _characterLevel
    // 캐릭터 history 데이터
    private val _historyData = MutableLiveData<CharacterHistoryResponse?>()
    val historyData: LiveData<CharacterHistoryResponse?> = _historyData

    // 로딩 상태 처리
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading : LiveData<Boolean> = _isLoading

    // 에러 메시지 처리
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    //캐릭터 성장 히스토리 데이터를 가져오는 함수
    fun fetchCharacterHistory() {
        viewModelScope.launch {
            try {
                val response = apiService.getCharacterHistory()
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        _historyData.value = responseBody
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "네트워크 오류가 발생했습니다"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchCharacterLevel(){
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.postCharacterLevel()
                if(response.isSuccessful){
                    _characterLevel.value = response.body()
                } else{
                    _errorMessage.value = "레벨 정보를 가죠오는데 실패했습니다"
                }
            }  catch (e: Exception) {
                _errorMessage.value = "네트워크 오류가 발생했습니다"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchMyCharacter(){
        viewModelScope.launch{
            _isLoading.value = true
            try{
                val response = apiService.getMycharacter()
                if(response.isSuccessful){
                    _characterLevel.value = response.body()
                } else{
                    _errorMessage.value = "캐릭터 정보를 가져오는데 실패"
                }
            } catch (e: Exception){
                Log.e("CharacterViewModel", "fetchMyCharacter 실패", e)
                _errorMessage.value = "네트워크 오류가 발생"
            } finally {
                _isLoading.value = false
            }
        }
    }
}