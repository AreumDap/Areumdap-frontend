package com.example.areumdap.UI.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.areumdap.UI.MainActivity
import com.example.areumdap.UI.Onboarding.OnboardingActivity
import com.example.areumdap.databinding.ActivityEmailLoginBinding

class EmailLoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmailLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        val id = binding.etId.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (id.isEmpty()) {
            Toast.makeText(this, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show()
            binding.etId.requestFocus()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            binding.etPassword.requestFocus()
            return
        }

        Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()

        val keepLogin = binding.cbKeepLogin.isChecked
        if (keepLogin) {
            getSharedPreferences("auth", MODE_PRIVATE)
                .edit()
                .putBoolean("keep_login", true)
                .putString("user_id", id)
                .apply()
        }

        navigateToMain()
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