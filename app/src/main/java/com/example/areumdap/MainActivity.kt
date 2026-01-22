package com.example.areumdap

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.areumdap.login.LoginActivity
import com.kakao.sdk.user.UserApiClient

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tvNickname = findViewById<TextView>(R.id.tvNickname)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // 1. 사용자 정보 요청 (닉네임 가져오기)
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e("KakaoMain", "사용자 정보 요청 실패", error)
                tvNickname.text = "로그인 정보가 없습니다."
            } else if (user != null) {
                val nickname = user.kakaoAccount?.profile?.nickname
                Log.i("KakaoMain", "사용자 정보 요청 성공: $nickname")
                tvNickname.text = "${nickname}님, 환영합니다!"
            }
        }

        // 2. 로그아웃 버튼
        btnLogout.setOnClickListener {
            UserApiClient.instance.logout { error ->
                if (error != null) {
                    Log.e("KakaoMain", "로그아웃 실패", error)
                } else {
                    Log.i("KakaoMain", "로그아웃 성공")
                    // 로그아웃 되면 다시 로그인 화면으로 이동
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
        }
    }
}