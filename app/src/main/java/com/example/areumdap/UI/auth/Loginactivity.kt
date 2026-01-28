package com.example.areumdap.UI.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.areumdap.UI.MainActivity
import com.example.areumdap.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initClickListeners()
    }

    private fun initClickListeners() {
        // 카카오 로그인 버튼
        binding.btnKakaoLogin.setOnClickListener {
            // 임시로 바로 메인화면으로 이동
            navigateToMain()
        }

        // 네이버 로그인 버튼
        binding.btnNaverLogin.setOnClickListener {
            // 임시로 바로 메인화면으로 이동
            navigateToMain()
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

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}