package com.example.areumdap

import android.app.Application
import com.example.areumdap.Network.TokenManager
import com.kakao.sdk.common.KakaoSdk
import com.navercorp.nid.NaverIdLoginSDK

/**
 * 앱 전역 설정 클래스
 * 카카오/네이버 SDK 초기화
 */
class GlobalApplication : Application() {

    companion object {
        // ★★★ 여기에 발급받은 키 입력 ★★★

        // 카카오 네이티브 앱 키 (Kakao Developers에서 발급)
        const val KAKAO_NATIVE_APP_KEY = "YOUR_KAKAO_NATIVE_APP_KEY"

        // 네이버 클라이언트 정보 (Naver Developers에서 발급)
        const val NAVER_CLIENT_ID = "YOUR_NAVER_CLIENT_ID"
        const val NAVER_CLIENT_SECRET = "YOUR_NAVER_CLIENT_SECRET"
        const val NAVER_CLIENT_NAME = "아름답"  // 앱 이름
    }

    override fun onCreate() {
        super.onCreate()

        // TokenManager 초기화
        TokenManager.init(this)

        // 카카오 SDK 초기화
        KakaoSdk.init(this, KAKAO_NATIVE_APP_KEY)

        // 네이버 SDK 초기화
        NaverIdLoginSDK.initialize(
            this,
            NAVER_CLIENT_ID,
            NAVER_CLIENT_SECRET,
            NAVER_CLIENT_NAME
        )
    }
}