package com.example.areumdap.UI.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.areumdap.Network.SocialAuthRepository
import com.example.areumdap.Network.TokenManager
import com.example.areumdap.UI.MainActivity
import com.example.areumdap.UI.Onboarding.OnboardingActivity
import com.example.areumdap.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val tag = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TokenManager 초기화 (GlobalApplication에서 이미 했지만 안전하게)
        TokenManager.init(this)

        initClickListeners()
    }

    private fun initClickListeners() {
        // ★★★ 카카오 로그인 버튼 ★★★
        binding.btnKakaoLogin.setOnClickListener {
            performKakaoLogin()
        }

        // ★★★ 네이버 로그인 버튼 ★★★
        binding.btnNaverLogin.setOnClickListener {
            performNaverLogin()
        }

        // 회원가입 텍스트 클릭
        binding.tvSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        // 직접 로그인 텍스트 클릭
        binding.tvLoginDirect.setOnClickListener {
            val intent = Intent(this, EmailLoginActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * 카카오 로그인 실행
     */
    private fun performKakaoLogin() {
        // 버튼 비활성화 (중복 클릭 방지)
        binding.btnKakaoLogin.isEnabled = false

        lifecycleScope.launch {
            val result = SocialAuthRepository.loginWithKakao(this@LoginActivity)

            result.onSuccess { response ->
                Log.d(tag, "카카오 로그인 성공: ${response.name}")
                Toast.makeText(
                    this@LoginActivity,
                    "${response.name}님 환영합니다!",
                    Toast.LENGTH_SHORT
                ).show()

                // 로그인 상태 저장
                saveLoginState()

                // 신규 회원이면 온보딩, 기존 회원이면 메인
                if (response.isNewUser) {
                    navigateToOnboarding()
                } else {
                    navigateToMain()
                }
            }.onFailure { error ->
                Log.e(tag, "카카오 로그인 실패: ${error.message}")
                Toast.makeText(
                    this@LoginActivity,
                    error.message ?: "카카오 로그인에 실패했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                binding.btnKakaoLogin.isEnabled = true
            }
        }
    }

    /**
     * 네이버 로그인 실행
     */
    private fun performNaverLogin() {
        // 버튼 비활성화 (중복 클릭 방지)
        binding.btnNaverLogin.isEnabled = false

        lifecycleScope.launch {
            val result = SocialAuthRepository.loginWithNaver(this@LoginActivity)

            result.onSuccess { response ->
                Log.d(tag, "네이버 로그인 성공: ${response.name}")
                Toast.makeText(
                    this@LoginActivity,
                    "${response.name}님 환영합니다!",
                    Toast.LENGTH_SHORT
                ).show()

                // 로그인 상태 저장
                saveLoginState()

                // 신규 회원이면 온보딩, 기존 회원이면 메인
                if (response.isNewUser) {
                    navigateToOnboarding()
                } else {
                    navigateToMain()
                }
            }.onFailure { error ->
                Log.e(tag, "네이버 로그인 실패: ${error.message}")
                Toast.makeText(
                    this@LoginActivity,
                    error.message ?: "네이버 로그인에 실패했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                binding.btnNaverLogin.isEnabled = true
            }
        }
    }

    /**
     * 로그인 상태 저장
     */
    private fun saveLoginState() {
        getSharedPreferences("auth", MODE_PRIVATE)
            .edit()
            .putBoolean("keep_login", true)
            .apply()
    }

    /**
     * 온보딩 화면으로 이동 (신규 회원)
     */
    private fun navigateToOnboarding() {
        val intent = Intent(this, OnboardingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * 메인 화면으로 이동
     */
    private fun navigateToMain() {
        val pref = getSharedPreferences("auth", MODE_PRIVATE)
        val isOnboardingDone = pref.getBoolean("onboarding_done", false)

        val intent = if (isOnboardingDone) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, OnboardingActivity::class.java)
        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}