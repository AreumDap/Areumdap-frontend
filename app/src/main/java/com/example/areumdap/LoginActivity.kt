package com.example.areumdap.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.areumdap.EmailLoginActivity
import com.example.areumdap.MainActivity
import com.example.areumdap.R
import com.example.areumdap.signup.SignupActivity
import com.kakao.sdk.user.UserApiClient // [중요] 카카오 로그인 기능

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnEmail = findViewById<Button>(R.id.btnEmail)
        val btnKakao = findViewById<Button>(R.id.btnKakao) // 카카오 버튼 찾기
        val btnNaver = findViewById<Button>(R.id.btnNaver)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)

        // 1. 회원가입 이동
        tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // 2. 이메일 로그인 이동
        btnEmail.setOnClickListener {
            startActivity(Intent(this, EmailLoginActivity::class.java))
        }

        // 3. 카카오 로그인 버튼 클릭
        btnKakao.setOnClickListener {
            // 카카오톡 앱이 있으면 앱으로, 없으면 웹으로 로그인
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                    if (error != null) {
                        Log.e("KakaoLogin", "카카오톡 로그인 실패", error)
                        // 사용자가 취소한 게 아니면 웹으로 다시 시도
                        if (error.toString().contains("statusCode=302")) {
                            loginWithKakaoAccount()
                        }
                    } else if (token != null) {
                        Log.i("KakaoLogin", "카카오톡 로그인 성공: ${token.accessToken}")
                        navigateToMain()
                    }
                }
            } else {
                loginWithKakaoAccount()
            }
        }

        // 4. 네이버 (아직 준비중)
        btnNaver.setOnClickListener {
            Toast.makeText(this, "네이버 로그인은 준비 중입니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 웹(계정)으로 로그인하는 함수
    private fun loginWithKakaoAccount() {
        UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
            if (error != null) {
                Log.e("KakaoLogin", "웹 로그인 실패", error)
            } else if (token != null) {
                Log.i("KakaoLogin", "웹 로그인 성공: ${token.accessToken}")
                navigateToMain()
            }
        }
    }

    // 로그인 성공 시 메인 화면으로 이동
    private fun navigateToMain() {
        Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}