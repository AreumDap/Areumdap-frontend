package com.example.areumdap.UI.auth

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.areumdap.data.repository.AuthRepository
import com.example.areumdap.data.repository.SocialAuthRepository
import com.example.areumdap.data.repository.UserRepository
import com.example.areumdap.UI.Onboarding.OnboardingActivity
import com.example.areumdap.databinding.ActivitySocialLoginWebviewBinding
import com.google.firebase.messaging.FirebaseMessaging
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

        if (loginUrl.isEmpty() || (loginType != TYPE_KAKAO && loginType != TYPE_NAVER)) {
            Toast.makeText(this, "로그인 정보가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupWebView(loginUrl)
    }

    private fun setupWebView(loginUrl: String) {
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            javaScriptCanOpenWindowsAutomatically = true // 팝업 허용
            setSupportMultipleWindows(true) // 멀티 윈도우 허용
        }

        binding.webView.webViewClient = object : WebViewClient() {

            // ★★★ 핵심 수정: Intent 및 외부 앱 실행 처리 ★★★
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                Log.d(tag, "URL 로딩 감지: $url")

                // 1. 인가 코드/에러 감지 (로그인 성공/실패 처리)
                if (checkAndHandleCallback(url)) {
                    return true // 우리가 처리했으므로 WebView 로딩 중단
                }

                // 2. intent:// 스킴 처리 (네이버 앱 실행 등)
                if (url.startsWith("intent://")) {
                    try {
                        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        val existPackage = packageManager.getLaunchIntentForPackage(intent.`package` ?: "")

                        if (existPackage != null) {
                            // 앱이 설치되어 있으면 실행
                            startActivity(intent)
                        } else {
                            // 앱이 없으면 마켓으로 이동하거나 Fallback URL로 이동
                            val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                            if (fallbackUrl != null) {
                                view?.loadUrl(fallbackUrl)
                            } else {
                                // 마켓으로 이동
                                val marketIntent = Intent(Intent.ACTION_VIEW)
                                marketIntent.data = Uri.parse("market://details?id=" + intent.`package`)
                                startActivity(marketIntent)
                            }
                        }
                        return true
                    } catch (e: Exception) {
                        Log.e(tag, "Intent 처리 실패: ${e.message}")
                    }
                }
                // 3. 마켓 링크 처리
                else if (url.startsWith("market://")) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                        return true
                    } catch (e: Exception) {
                        Log.e(tag, "Market 처리 실패: ${e.message}")
                    }
                }

                // 그 외(http, https)는 WebView가 로드하도록 false 반환
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                // 페이지 시작 시점에도 콜백 URL 체크 (보조)
                if (url != null && checkAndHandleCallback(url)) {
                    view?.stopLoading()
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
                // 네이버는 state도 체크하는 것이 안전함
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

                // [수정] 토큰 등록 후 이동
                registerFcmTokenAndNavigate()

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

                // [수정] 토큰 등록 후 이동
                registerFcmTokenAndNavigate()

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

    /**
     * [추가] FCM 토큰 가져와서 서버에 등록 후 화면 이동
     */
    private fun registerFcmTokenAndNavigate() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e(tag, "FCM 토큰 가져오기 실패", task.exception)
                // 토큰 실패해도 앱 진입은 시켜야 함
                checkCharacterAndNavigate()
                return@addOnCompleteListener
            }

            // 토큰 가져오기 성공
            val token = task.result

            // 서버에 전송 (비동기)
            lifecycleScope.launch {
                try {
                    UserRepository.updateFcmToken(token)
                    Log.d(tag, "소셜 로그인 직후 FCM 토큰 서버 등록 완료")
                } catch (e: Exception) {
                    Log.e(tag, "FCM 토큰 서버 등록 실패: ${e.message}")
                } finally {
                    // 성공하든 실패하든 메인/온보딩으로 이동
                    checkCharacterAndNavigate()
                }
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