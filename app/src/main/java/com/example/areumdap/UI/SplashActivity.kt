package com.example.areumdap.UI

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.areumdap.R
import com.example.areumdap.Network.AuthRepository
import com.example.areumdap.Network.TokenManager
import com.example.areumdap.UI.Onboarding.OnboardingActivity
import com.example.areumdap.UI.auth.LoginActivity
import com.example.areumdap.UI.auth.EmailLoginActivity
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 토큰 매니저 초기화 (자동 로그인을 위해 필수)
        TokenManager.init(this)

        // 2초 뒤에 로그인 상태 및 캐릭터 존재 여부 체크
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginAndCharacter()
        }, 2000)
    }

    private fun checkLoginAndCharacter() {
        val pref = getSharedPreferences("auth", MODE_PRIVATE)
        val keepLogin = pref.getBoolean("keep_login", false)

        // 1. 자동 로그인 설정이 되어 있고, 토큰이 있는 경우
        if (keepLogin && !TokenManager.getAccessToken().isNullOrEmpty()) {

            // 서버에 캐릭터가 진짜 있는지 확인 (API 호출)
            lifecycleScope.launch {
                try {
                    val result = AuthRepository.getMyCharacter()

                    result.onSuccess {
                        // [성공 200] 캐릭터 있음 -> 메인 화면
                        Log.d("SplashActivity", "자동 로그인 & 캐릭터 확인 완료")
                        moveToMain()
                    }.onFailure { e ->
                        // [실패] 404면 온보딩, 그 외엔 다시 로그인 유도
                        if (e is HttpException && e.code() == 404) {
                            Log.d("SplashActivity", "캐릭터 없음(404) -> 온보딩 이동")
                            moveToOnboarding()
                        } else {
                            Log.e("SplashActivity", "토큰 만료 또는 서버 오류 -> 로그인 화면으로")
                            moveToLogin()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SplashActivity", "오류 발생: ${e.message}")
                    moveToLogin()
                }
            }
        } else {
            // 2. 로그인 기록 없음 -> 로그인 화면으로
            moveToLogin()
        }
    }

    private fun moveToMain() {
        val intent = Intent(this, MainActivity::class.java)
        // 뒤로가기 방지
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun moveToOnboarding() {
        val intent = Intent(this, OnboardingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun moveToLogin() {
        // LoginActivity 혹은 EmailLoginActivity 중 사용하시는 것으로 연결
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}