package com.example.areumdap.UI.onboarding

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OnboardingViewModel : ViewModel() {
    // 1단계: 선택한 계절
    val selectedSeason = MutableLiveData<String>()

    // 2단계: 선택한 키워드
    val selectedKeywords = MutableLiveData<MutableList<String>>(mutableListOf())
    val isKeywordSelected = MutableLiveData<Boolean>(false)

    // 4단계: 입력한 닉네임
    val nickname = MutableLiveData<String>()

    // 데이터 저장 함수들
    fun setSeason(season: String) {
        selectedSeason.value = season
    }

    fun toggleKeyword(keyword: String) {
        val current = selectedKeywords.value ?: mutableListOf()
        if (current.contains(keyword)) {
            current.remove(keyword)
        } else {
            current.add(keyword)
        }
        selectedKeywords.value = current

        // 키워드가 하나라도 있으면 버튼 활성화
        isKeywordSelected.value = current.isNotEmpty()
    }
}