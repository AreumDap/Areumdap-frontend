package com.example.areumdap.UI.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.areumdap.UI.MainActivity
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

        // 임시 유효성 검사 (빈 값만 체크)
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

        // 임시로 아무 값이나 입력하면 로그인 성공
        Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()

        // 로그인 상태 유지 체크 여부 저장 (임시로 SharedPreferences 사용)
        val keepLogin = binding.cbKeepLogin.isChecked
        if (keepLogin) {
            // 실제로는 토큰을 저장해야 하지만 임시로 플래그만 저장
            getSharedPreferences("auth", MODE_PRIVATE)
                .edit()
                .putBoolean("keep_login", true)
                .putString("user_id", id)
                .apply()
        }

        navigateToMain()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}