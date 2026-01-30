package com.example.areumdap.UI.Character

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.areumdap.UI.Character.Data.CharacterHistoryResponse
import kotlinx.coroutines.launch

class CharacterHistoryViewModel(private val apiService: CharacterHistoryApiService) : ViewModel() {
    private val _historyData = MutableLiveData<CharacterHistoryResponse?>()
    val historyData: LiveData<CharacterHistoryResponse?> = _historyData

    // 에러 메시지 처리
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    //캐릭터 성장 히스토리 데이터를 가져오는 함수
    fun fetchCharacterHistory() {
        viewModelScope.launch {
            try {
                val response = apiService.getCharacterHistory()
                if (response.isSuccessful) {
                    // 성공 로그 출력! 여기서 데이터가 찍히면 연결 성공입니다.
                    Log.d("API_TEST", "성공 데이터: ${response.body()}")
                    _historyData.value = response.body()
                } else {
                    Log.e("API_TEST", "실패 코드: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API_TEST", "연결 에러: ${e.message}")
            }
        }
         /*iewModelScope.launch {
            try {
                val response = apiService.getCharacterHistory()
                if (response.isSuccessful) {
                    // 성공
                    _historyData.value = response.body()
                } else {
                    // 실패
                    _errorMessage.value = "데이터를 불러오지 못했습니다: ${response.code()}"
                }
            } catch (e: Exception) {
                // 네트워크 오류 등 예외 발생
                _errorMessage.value = "연결 오류: ${e.localizedMessage}"
            }
        }*/
    }
}