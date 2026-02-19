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
        setContentView(R.layout.activity_splash)

        TokenManager.init(this)

        // 1.5초 딜레이 후 데모 자동 로그인 시작
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginState()
        }, 1500)
    }

    private fun checkLoginState() {
        // 데모 모드: 항상 테스트 계정으로 자동 로그인
        lifecycleScope.launch {
            try {
                val loginResult = AuthRepository.testLogin()

                loginResult.onSuccess {
                    // 로그인 성공 → 캐릭터 존재 여부 확인
                    val characterResult = AuthRepository.getMyCharacter()
                    characterResult.onSuccess {
                        assignTodayRecommendIfNeeded()
                        saveOnboardingDone()
                        navigateToMain()
                    }.onFailure { e ->
                        if (e is HttpException && e.code() == 404) {
                            // 캐릭터 없음 → 온보딩으로
                            navigateToOnboarding()
                        } else {
                            navigateToOnboarding()
                        }
                    }
                }.onFailure {
                    // 데모 로그인 실패 시 → 기존 토큰으로 fallback
                    val characterResult = runCatching { AuthRepository.getMyCharacter() }
                    if (characterResult.isSuccess) {
                        assignTodayRecommendIfNeeded()
                        saveOnboardingDone()
                        navigateToMain()
                    } else {
                        navigateToLogin()
                    }
                }
            } catch (e: Exception) {
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
