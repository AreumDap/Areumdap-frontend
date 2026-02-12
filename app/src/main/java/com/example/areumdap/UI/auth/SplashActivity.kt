package com.example.areumdap.UI.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.areumdap.data.repository.AuthRepository
import com.example.areumdap.data.api.ChatbotApiService
import com.example.areumdap.data.repository.ChatbotRepository
import com.example.areumdap.data.source.RetrofitClient
import com.example.areumdap.data.source.TokenManager
import com.example.areumdap.R
import com.example.areumdap.UI.Onboarding.OnboardingActivity
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 스플래시 화면 레이아웃이 있다면 설정 (없다면 생략 가능하거나 로고만 있는 xml 생성)
        setContentView(R.layout.activity_splash)

        TokenManager.init(this)

        // 약간의 딜레이 후 상태 체크 (로고 보여줄 시간 1~2초)
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginState()
        }, 1500)
    }

    private fun checkLoginState() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val isKeepLogin = prefs.getBoolean("keep_login", false)

        // 1. 로그인 유지 체크가 안 되어 있다면 -> 로그인 화면으로
        if (!isKeepLogin) {
            navigateToLogin()
            return
        }

        // 2. 로그인 유지가 되어 있다면 -> 서버에 캐릭터 존재 여부 확인
        // (토큰이 만료되었을 수도 있고, 캐릭터를 아직 안 만들었을 수도 있으므로 API 확인 필수)
        lifecycleScope.launch {
            try {
                val result = AuthRepository.getMyCharacter()

                result.onSuccess {
                    // 200 OK: 캐릭터 있음 -> 온보딩 완료 처리 -> 메인으로
                    assignTodayRecommendIfNeeded()
                    saveOnboardingDone()
                    navigateToMain()
                }.onFailure { e ->
                    if (e is HttpException && e.code() == 404) {
                        // 404: 캐릭터 없음 -> 온보딩 미완료 -> 온보딩으로
                        navigateToOnboarding()
                    } else {
                        // 그 외 에러(토큰 만료, 서버 오류 등) -> 로그인 화면으로 다시
                        navigateToLogin()
                    }
                }
            } catch (e: Exception) {
                // 네트워크 오류 등 -> 안전하게 로그인 화면으로
                navigateToLogin()
            }
        }
    }

    private suspend fun assignTodayRecommendIfNeeded() {
        val api = RetrofitClient.create(ChatbotApiService::class.java)
        val repo = ChatbotRepository(api)
        repo.assignTodayRecommendOnLogin()
            .onFailure { e ->
            }
    }

    private fun saveOnboardingDone() {
        getSharedPreferences("auth", MODE_PRIVATE)
            .edit()
            .putBoolean("onboarding_done", true)
            .apply()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToOnboarding() {
        val intent = Intent(this, OnboardingActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
