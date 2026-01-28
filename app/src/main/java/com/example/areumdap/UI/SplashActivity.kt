package com.example.areumdap.UI

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.areumdap.R
import com.example.areumdap.UI.auth.LoginActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 2초(2000ms) 뒤에 로그인 상태 체크 후 화면 이동
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginStatusAndNavigate()
        }, 2000)
    }

    private fun checkLoginStatusAndNavigate() {
        val pref = getSharedPreferences("auth", MODE_PRIVATE)
        val keepLogin = pref.getBoolean("keep_login", false)

        if (keepLogin) {
            // 이미 로그인 상태면 메인화면으로
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            // 로그인 안된 상태면 로그인화면으로
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        finish() // 뒤로가기 누르면 스플래시 다시 안 나오게 종료
    }
}