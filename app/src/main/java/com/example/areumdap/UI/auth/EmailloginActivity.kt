package com.example.areumdap.UI.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.areumdap.Network.AuthRepository
import com.example.areumdap.Network.TokenManager
import com.example.areumdap.UI.MainActivity
import com.example.areumdap.UI.Onboarding.OnboardingActivity
import com.example.areumdap.databinding.ActivityEmailLoginBinding
import kotlinx.coroutines.launch

class EmailLoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmailLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TokenManager 초기화
        TokenManager.init(this)

        initClickListeners()
    }

    private fun initClickListeners() {
        // 뒤로가기 버튼
        binding.btnBack.setOnClickListener {
            finish()
        }

        // 로그인 버튼
        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        // 비밀번호 찾기
        binding.tvForgotPw.setOnClickListener {
            Toast.makeText(this, "비밀번호 찾기 기능은 추후 구현 예정입니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performLogin() {
        val email = binding.etId.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // 입력값 검증
        if (email.isEmpty()) {
            Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
            binding.etId.requestFocus()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            binding.etPassword.requestFocus()
            return
        }

        // 테스트용 로그인 (1234 / 1234)
        if (email == "1234" && password == "1234") {
            Toast.makeText(this, "테스트 로그인 성공!", Toast.LENGTH_SHORT).show()
            saveLoginState()
            navigateToMain()
            return
        }

        // 이메일 형식 검증
        if (!isValidEmail(email)) {
            Toast.makeText(this, "올바른 이메일 형식을 입력해주세요.", Toast.LENGTH_SHORT).show()
            binding.etId.requestFocus()
            return
        }

        // 버튼 비활성화 (중복 클릭 방지)
        binding.btnLogin.isEnabled = false

        // API 호출
        lifecycleScope.launch {
            val result = AuthRepository.login(email, password)

            result.onSuccess { loginResponse ->
                Toast.makeText(
                    this@EmailLoginActivity,
                    "${loginResponse.name}님 환영합니다!",
                    Toast.LENGTH_SHORT
                ).show()

                // 로그인 상태 저장
                val keepLogin = binding.cbKeepLogin.isChecked
                if (keepLogin) {
                    saveLoginState()
                }

                navigateToMain()
            }.onFailure { error ->
                Toast.makeText(
                    this@EmailLoginActivity,
                    error.message ?: "로그인에 실패했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                binding.btnLogin.isEnabled = true
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun saveLoginState() {
        getSharedPreferences("auth", MODE_PRIVATE)
            .edit()
            .putBoolean("keep_login", true)
            .apply()
    }

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