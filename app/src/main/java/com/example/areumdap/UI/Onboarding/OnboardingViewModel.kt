package com.example.areumdap.UI.Onboarding

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OnboardingViewModel : ViewModel() {
    // 1. 현재 진행 단계를 관리하는 변수
    val currentStep = MutableLiveData<Int>(0)

    // 2. 시작 화면에서 텍스트가 한 번 바뀌었는지 체크하는 변수
    var isTextUpdated: Boolean = false

    // 1단계: 계절
    val selectedSeason = MutableLiveData<String>()

    // 2단계: 키워드
    val selectedKeywords = MutableLiveData<MutableList<String>>(mutableListOf())
    val isKeywordSelected = MutableLiveData<Boolean>(false)
    val isDirectInput = MutableLiveData<Boolean>(false)
    val directKeyword = MutableLiveData<String>()

    // 텍스트 변경 단계 추적 (0: 아름이가 태어났어요, 1: 닉네임 인트로, 2: 결과1, 3: 결과2)
    var infoTextStep = MutableLiveData<Int>(0)

    // 캐릭터 생성 결과 (Fragment 재생성 시 이미지 복원용)
    var createdCharacterId: Int? = null
    var createdImageUrl: String? = null

    // 4단계: 닉네임
    val nickname = MutableLiveData<String>("")

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