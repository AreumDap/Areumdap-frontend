package com.example.areumdap.Util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.areumdap.R
import com.example.areumdap.UI.MainActivity
import com.example.areumdap.Network.UserRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "areumdap_notification"
        private const val CHANNEL_NAME = "철학적 질문 알림"
    }

    /**
     * FCM 토큰이 생성/갱신될 때 호출
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "새 FCM 토큰: $token")

        // 토큰을 서버에 등록
        CoroutineScope(Dispatchers.IO).launch {
            try {
                UserRepository.updateFcmToken(token)
                Log.d(TAG, "FCM 토큰 서버 등록 성공")
            } catch (e: Exception) {
                Log.e(TAG, "FCM 토큰 서버 등록 실패: ${e.message}")
            }
        }
    }

    /**
     * 푸시 알림 수신 시 호출
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "푸시 수신 from: ${remoteMessage.from}")

        // 데이터 메시지 처리
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "데이터: ${remoteMessage.data}")
        }

        // 알림 데이터 추출
        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "아름답"
        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "오늘의 철학적 질문이 도착했어요!"

        // 알림 표시
        showNotification(title, body)
    }

    /**
     * 알림 표시
     */
    private fun showNotification(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0 이상은 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "매일 철학적 질문을 보내드려요"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 클릭 시 앱 열기
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )

        // 알림 소리
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // 알림 생성
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}