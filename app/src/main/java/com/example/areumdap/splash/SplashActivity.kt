package com.example.areumdap.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.areumdap.R
import com.example.areumdap.login.LoginActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        val ivLogo = findViewById<ImageView>(R.id.splashLogo)
        val tvText = findViewById<TextView>(R.id.splashText)

        // 초기 위치 설정 (로고: 오른쪽, 텍스트: 투명)
        ivLogo.translationX = 200f
        tvText.alpha = 0f

        // 0.5초 대기 후 애니메이션 시작
        Handler(Looper.getMainLooper()).postDelayed({
            startSplashAnimation(ivLogo, tvText)
        }, 500)
    }

    // 애니메이션 로직 분리 (가독성 향상)
    private fun startSplashAnimation(logo: ImageView, text: TextView) {
        // 1. 로고 이동 (오른쪽 -> 중앙)
        logo.animate()
            .translationX(0f)
            .setDuration(1000)
            .start()

        // 2. 텍스트 페이드인 & 종료 후 화면 이동
        text.animate()
            .alpha(1f)
            .setDuration(1000)
            .withEndAction {
                // 애니메이션 끝난 후 1초 대기했다가 이동
                Handler(Looper.getMainLooper()).postDelayed({
                    navigateToNextScreen()
                }, 1000)
            }
            .start()
    }

    private fun navigateToNextScreen() {
        // TODO: 자동 로그인 여부(토큰) 확인 로직 추가 필요
        // 토큰이 없으면 LoginActivity, 있으면 MainActivity로 이동하도록 분기 처리 예정

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // 스플래시 화면은 뒤로가기로 돌아올 필요 없으므로 종료
    }
}