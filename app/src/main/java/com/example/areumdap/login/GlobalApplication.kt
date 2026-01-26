package com.example.areumdap.login

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Kakao SDK 초기화
        // TODO: "네이티브 앱 키" 부분에 카카오 개발자 사이트에서 받은 키를 넣어야 합니다.
        KakaoSdk.init(this, "a21690c0113fea3822765e107e26bc1e")
    }
}