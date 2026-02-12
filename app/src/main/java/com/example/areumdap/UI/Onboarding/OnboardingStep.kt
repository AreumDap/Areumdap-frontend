package com.example.areumdap.UI.Onboarding

/**
 * 온보딩 진행 단계를 정의하는 Enum
 */
enum class OnboardingStep(val value: Int, val displayProgress: Int) {
    START(0, 0),
    SEASON(1, 1),
    KEYWORD(2, 2),
    CUSTOM_KEYWORD(3, 2),
    INFO(4, 3),
    NICKNAME(5, 4),
    FINAL(6, 5);

    companion object {
        fun fromValue(value: Int): OnboardingStep? {
            return values().find { it.value == value }
        }
    }
}

/**
 * Info 화면 내부의 텍스트 변경 단계
 */
enum class InfoTextStep(val value: Int, val displayProgress: Int) {
    AREUM_BORN(0, 3),           // "아름이가 태어났어요"
    NICKNAME_INTRO(1, 4),        // "서로를 알아가기 위해..."
    NICKNAME_INPUT(2, 4),        // "좋아요! 앞으로..." (닉네임 입력)
    FINAL_MESSAGE(3, 5);         // "아름이는 oo님이..." (여정 시작하기)

    companion object {
        fun fromValue(value: Int): InfoTextStep {
            return values().find { it.value == value } ?: AREUM_BORN
        }
    }
}

/**
 * 온보딩 버튼 텍스트
 */
enum class ButtonText(val text: String) {
    START("시작할게요"),
    I_AM("나는..."),
    NEXT("다음으로"),
    START_JOURNEY("여정 시작하기");
}