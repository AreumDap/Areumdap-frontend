package com.example.areumdap.UI.auth

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.areumdap.Network.AuthRepository
import com.example.areumdap.Network.SocialAuthRepository
import com.example.areumdap.UI.MainActivity
import com.example.areumdap.UI.Onboarding.OnboardingActivity
import com.example.areumdap.databinding.ActivitySocialLoginWebviewBinding
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * 소셜 로그인 웹뷰 화면
 */
class SocialLoginWebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySocialLoginWebviewBinding
    private val tag = "SocialLoginWebView"

    private var loginType: String = ""

    companion object {
        const val EXTRA_LOGIN_TYPE = "login_type"
        const val EXTRA_LOGIN_URL = "login_url"
        const val TYPE_KAKAO = "kakao"
        const val TYPE_NAVER = "naver"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySocialLoginWebviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loginType = intent.getStringExtra(EXTRA_LOGIN_TYPE) ?: ""
        val loginUrl = intent.getStringExtra(EXTRA_LOGIN_URL) ?: ""

        if (loginUrl.isEmpty()) {
            Toast.makeText(this, "로그인 URL을 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupWebView(loginUrl)
    }

    private fun setupWebView(loginUrl: String) {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.settings.setSupportMultipleWindows(true)

        binding.webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (url != null) {
                    Log.d(tag, "페이지 시작: $url")
                    if (checkAndHandleCallback(url)) {
                        view?.stopLoading()
                    }
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (url != null) {
                    Log.d(tag, "페이지 완료: $url")
                }
            }
        }

        binding.webView.loadUrl(loginUrl)
    }

    private fun checkAndHandleCallback(url: String): Boolean {
        when (loginType) {
            TYPE_KAKAO -> {
                if (url.contains("code=")) {
                    val code = extractQueryParam(url, "code")
                    if (code != null) {
                        Log.d(tag, "카카오 인가 코드 획득: $code")
                        processKakaoLogin(code)
                        return true
                    }
                }
                if (url.contains("error=")) {
                    handleLoginError(url)
                    return true
                }
            }
            TYPE_NAVER -> {
                if (url.contains("code=") && url.contains("state=")) {
                    val code = extractQueryParam(url, "code")
                    val state = extractQueryParam(url, "state")
                    if (code != null && state != null) {
                        Log.d(tag, "네이버 인가 코드 획득: $code, state: $state")
                        processNaverLogin(code, state)
                        return true
                    }
                }
                if (url.contains("error=")) {
                    handleLoginError(url)
                    return true
                }
            }
        }
        return false
    }

    private fun extractQueryParam(url: String, param: String): String? {
        return try {
            val uri = Uri.parse(url)
            uri.getQueryParameter(param)
        } catch (e: Exception) {
            Log.e(tag, "$param 추출 실패: ${e.message}")
            null
        }
    }

    private fun handleLoginError(url: String) {
        val uri = Uri.parse(url)
        val errorDescription = uri.getQueryParameter("error_description") ?: "로그인이 취소되었습니다."
        Log.e(tag, "로그인 에러: $errorDescription")
        Toast.makeText(this, errorDescription, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun processKakaoLogin(code: String) {
        lifecycleScope.launch {
            val result = SocialAuthRepository.loginWithKakaoCode(code)

            result.onSuccess { response ->
                Log.d(tag, "카카오 로그인 성공: ${response.name}")
                Toast.makeText(
                    this@SocialLoginWebViewActivity,
                    "${response.name ?: "회원"}님 환영합니다!",
                    Toast.LENGTH_SHORT
                ).show()

                saveLoginState()
                checkCharacterAndNavigate()

            }.onFailure { error ->
                Log.e(tag, "카카오 로그인 실패: ${error.message}")
                Toast.makeText(
                    this@SocialLoginWebViewActivity,
                    error.message ?: "카카오 로그인에 실패했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun processNaverLogin(code: String, state: String) {
        lifecycleScope.launch {
            val result = SocialAuthRepository.loginWithNaverCode(code, state)

            result.onSuccess { response ->
                Log.d(tag, "네이버 로그인 성공: ${response.name}")
                Toast.makeText(
                    this@SocialLoginWebViewActivity,
                    "${response.name ?: "회원"}님 환영합니다!",
                    Toast.LENGTH_SHORT
                ).show()

                saveLoginState()
                checkCharacterAndNavigate()

            }.onFailure { error ->
                Log.e(tag, "네이버 로그인 실패: ${error.message}")
                Toast.makeText(
                    this@SocialLoginWebViewActivity,
                    error.message ?: "네이버 로그인에 실패했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun checkCharacterAndNavigate() {
        lifecycleScope.launch {
            try {
                val result = AuthRepository.getMyCharacter()

                result.onSuccess {
                    Log.d(tag, "캐릭터 확인됨: 메인으로 이동")
                    navigateToMain(forceMain = true)
                }.onFailure { e ->
                    if (e is HttpException && e.code() == 404) {
                        Log.d(tag, "캐릭터 없음(404): 온보딩으로 이동")
                        navigateToOnboarding()
                    } else {
                        Log.e(tag, "캐릭터 조회 실패: ${e.message}")
                        navigateToMain(forceMain = true)
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "알 수 없는 오류: ${e.message}")
                navigateToMain(forceMain = true)
            }
        }
    }

    private fun saveLoginState() {
        getSharedPreferences("auth", MODE_PRIVATE)
            .edit()
            .putBoolean("keep_login", true)
            .apply()
    }

    private fun navigateToOnboarding() {
        getSharedPreferences("auth", MODE_PRIVATE)
            .edit()
            .putBoolean("onboarding_done", false)
            .apply()

        val intent = Intent(this, OnboardingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToMain(forceMain: Boolean = false) {
        val pref = getSharedPreferences("auth", MODE_PRIVATE)
        val isOnboardingDone = if (forceMain) true else pref.getBoolean("onboarding_done", false)

        val intent = if (isOnboardingDone) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, OnboardingActivity::class.java)
        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}