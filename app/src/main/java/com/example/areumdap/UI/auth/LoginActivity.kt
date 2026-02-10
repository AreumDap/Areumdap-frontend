package com.example.areumdap.UI.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.areumdap.R
import com.example.areumdap.data.repository.SocialAuthRepository
import com.example.areumdap.data.source.TokenManager
import com.example.areumdap.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val tag = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        TokenManager.init(this)
        initClickListeners()
    }

    private fun initClickListeners() {
        // 카카오 로그인 버튼
        binding.btnKakaoLogin.setOnClickListener {
            performKakaoLogin()
        }

        // 네이버 로그인 버튼
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

    // [수정됨] 커스텀 토스트 표시 함수 (ToastDialogFragment 사용)
    private fun showCustomToast(message: String, isSuccess: Boolean = true) {
        // 액티비티가 종료된 상태라면 실행하지 않음
        if (isFinishing || isDestroyed) return

        // 성공/실패에 따라 아이콘 변경
        // (기존 코드에 있던 ic_failure 리소스를 그대로 사용했습니다)
        val iconRes = if (isSuccess) {
            R.drawable.ic_success
        } else {
            R.drawable.ic_failure // 혹은 ic_error
        }

        val toast = ToastDialogFragment(message, iconRes)
        // Activity에서는 supportFragmentManager를 사용
        toast.show(supportFragmentManager, "CustomToast")
    }

    private fun performKakaoLogin() {
        binding.btnKakaoLogin.isEnabled = false

        lifecycleScope.launch {
            val result = SocialAuthRepository.getKakaoLoginUrl()

            result.onSuccess { loginUrl ->
                Log.d(tag, "카카오 로그인 URL: $loginUrl")

                // 웹뷰 화면으로 이동
                val intent = Intent(this@LoginActivity, SocialLoginWebViewActivity::class.java)
                intent.putExtra(SocialLoginWebViewActivity.EXTRA_LOGIN_TYPE, SocialLoginWebViewActivity.TYPE_KAKAO)
                intent.putExtra(SocialLoginWebViewActivity.EXTRA_LOGIN_URL, loginUrl)
                startActivity(intent)

                binding.btnKakaoLogin.isEnabled = true

            }.onFailure { error ->
                Log.e(tag, "카카오 로그인 URL 조회 실패: ${error.message}")
                // 실패 시 커스텀 토스트 호출
                showCustomToast("로그인에 실패했어요. 잠시 후 다시 시도해 주세요", isSuccess = false)
                binding.btnKakaoLogin.isEnabled = true
            }
        }
    }


    private fun performNaverLogin() {
        binding.btnNaverLogin.isEnabled = false

        lifecycleScope.launch {
            val result = SocialAuthRepository.getNaverLoginUrl()

            result.onSuccess { loginUrl ->
                Log.d(tag, "네이버 로그인 URL: $loginUrl")

                // 웹뷰 화면으로 이동
                val intent = Intent(this@LoginActivity, SocialLoginWebViewActivity::class.java)
                intent.putExtra(SocialLoginWebViewActivity.EXTRA_LOGIN_TYPE, SocialLoginWebViewActivity.TYPE_NAVER)
                intent.putExtra(SocialLoginWebViewActivity.EXTRA_LOGIN_URL, loginUrl)
                startActivity(intent)

                binding.btnNaverLogin.isEnabled = true

            }.onFailure { error ->
                Log.e(tag, "네이버 로그인 URL 조회 실패: ${error.message}")
                // 실패 시 커스텀 토스트 호출
                showCustomToast("로그인에 실패했어요. 잠시 후 다시 시도해 주세요", isSuccess = false)
                binding.btnNaverLogin.isEnabled = true
            }
        }
    }
}