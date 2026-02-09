package com.example.areumdap.UI.auth

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.areumdap.R
import com.example.areumdap.data.source.TokenManager

/**
 * 앱 전역 설정 클래스
 * 웹뷰 방식 소셜 로그인이므로 SDK 초기화 불필요
 */
class GlobalApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // TokenManager 초기화
        TokenManager.init(this)

        // 알림 채널 생성
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = "기타 알림" // 사용자에게 보이는 이름
            val channelDescription = "일반적인 알림을 수신합니다."
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        TokenManager.init(this)
    }
}