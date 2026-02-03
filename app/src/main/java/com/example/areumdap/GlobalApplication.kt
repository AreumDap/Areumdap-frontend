package com.example.areumdap

import android.app.Application
import com.example.areumdap.Network.TokenManager

/**
 * 앱 전역 설정 클래스
 * 웹뷰 방식 소셜 로그인이므로 SDK 초기화 불필요
 */
class GlobalApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // TokenManager 초기화
        TokenManager.init(this)
    }
}