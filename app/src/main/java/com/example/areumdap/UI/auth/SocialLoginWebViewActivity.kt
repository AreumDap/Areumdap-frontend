package com.example.areumdap.UI.auth

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.areumdap.R
import com.example.areumdap.data.repository.AuthRepository
import com.example.areumdap.data.api.ChatbotApiService
import com.example.areumdap.data.repository.ChatbotRepository
import com.example.areumdap.data.repository.SocialAuthRepository
import com.example.areumdap.data.repository.UserRepository
import com.example.areumdap.data.source.RetrofitClient
import com.example.areumdap.data.source.TokenManager
import com.example.areumdap.UI.Onboarding.OnboardingActivity
import com.example.areumdap.databinding.ActivitySocialLoginWebviewBinding
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.HttpException
import kotlin.coroutines.resume

/**
 * 소셜 로그인 웹뷰 화면
 */
class SocialLoginWebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySocialLoginWebviewBinding

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
            showCustomToast("로그인 정보가 올바르지 않습니다.", isSuccess = false)
            finish()
            return
        }

        setupWebView(loginUrl)
    }

    // [수정됨] 커스텀 토스트 표시 함수 (ToastDialogFragment 사용)
    private fun showCustomToast(message: String, isSuccess: Boolean = true) {
        // 액티비티가 종료된 상태라면 실행하지 않음
        if (isFinishing || isDestroyed) return

        // 성공/실패에 따라 아이콘 변경 (프로젝트 리소스 이름에 맞춰주세요)
        val iconRes = if (isSuccess) {
            R.drawable.ic_success
        } else {
            R.drawable.ic_failure // 또는 ic_error
        }

        val toast = ToastDialogFragment(message, iconRes)
        // Activity에서는 supportFragmentManager를 사용
        toast.show(supportFragmentManager, "CustomToast")
    }

    private fun setupWebView(loginUrl: String) {
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(true)
        }

        binding.webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false

                if (checkAndHandleCallback(url)) {
                    return true
                }

                if (url.startsWith("intent://")) {
                    try {
                        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        val existPackage = packageManager.getLaunchIntentForPackage(intent.`package` ?: "")

                        if (existPackage != null) {
                            startActivity(intent)
                        } else {
                            val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                            if (fallbackUrl != null) {
                                view?.loadUrl(fallbackUrl)
                            } else {
                                val marketIntent = Intent(Intent.ACTION_VIEW)
                                marketIntent.data = Uri.parse("market://details?id=" + intent.`package`)
                                startActivity(marketIntent)
                            }
                        }
                        return true
                    } catch (e: Exception) {
                    }
                } else if (url.startsWith("market://")) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                        return true
                    } catch (e: Exception) {
                    }
                }

                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (url != null && checkAndHandleCallback(url)) {
                    view?.stopLoading()
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (url != null) {
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
            null
        }
    }

    private fun handleLoginError(url: String) {
        val uri = Uri.parse(url)
        val errorDescription = uri.getQueryParameter("error_description") ?: "로그인이 취소되었습니다."
        showCustomToast(errorDescription, isSuccess = false)
        finish()
    }

    /**
     * 카카오 로그인 처리 (완전 순차 실행)
     */
    private fun processKakaoLogin(code: String) {
        lifecycleScope.launch {
            val result = SocialAuthRepository.loginWithKakaoCode(code)

            if (result.isSuccess) {
                val response = result.getOrNull()

                // 토큰 저장 확인
                val savedAccessToken = TokenManager.getAccessToken()
                val savedUserId = TokenManager.getUserId()

                // 성공 토스트
                showCustomToast("${response?.name ?: "회원"}님 환영합니다!", isSuccess = true)

                saveLoginState()
                assignTodayRecommendIfNeeded()

                // FCM 토큰 등록
                registerFcmTokenSync()

                // 캐릭터 확인
                checkCharacterAndNavigateSync()

            } else {
                val error = result.exceptionOrNull()

                showCustomToast(error?.message ?: "카카오 로그인에 실패했습니다.", isSuccess = false)
                finish()
            }
        }
    }

    /**
     * 네이버 로그인 처리 (완전 순차 실행)
     */
    private fun processNaverLogin(code: String, state: String) {
        lifecycleScope.launch {
            val result = SocialAuthRepository.loginWithNaverCode(code, state)

            if (result.isSuccess) {
                val response = result.getOrNull()

                // 토큰 저장 확인
                val savedAccessToken = TokenManager.getAccessToken()
                val savedUserId = TokenManager.getUserId()

                // 성공 토스트
                showCustomToast("${response?.name ?: "회원"}님 환영합니다!", isSuccess = true)

                saveLoginState()
                assignTodayRecommendIfNeeded()

                // FCM 토큰 등록
                registerFcmTokenSync()

                // 캐릭터 확인
                checkCharacterAndNavigateSync()

            } else {
                val error = result.exceptionOrNull()
                showCustomToast(error?.message ?: "네이버 로그인에 실패했습니다.", isSuccess = false)
                finish()
            }
        }
    }

    /**
     * FCM 토큰 등록 (suspend - 완료될 때까지 대기)
     */
    private suspend fun registerFcmTokenSync() {
        return suspendCancellableCoroutine { continuation ->
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result

                    lifecycleScope.launch {
                        try {
                            UserRepository.updateFcmToken(token)
                        } catch (e: Exception) {
                        }
                        continuation.resume(Unit)
                    }
                } else {
                    continuation.resume(Unit)
                }
            }
        }
    }

    /**
     * 캐릭터 확인 후 화면 이동 (suspend - 완료될 때까지 대기)
     */
    private suspend fun assignTodayRecommendIfNeeded() {
        val api = RetrofitClient.create(ChatbotApiService::class.java)
        val repo = ChatbotRepository(api)
        repo.assignTodayRecommendOnLogin()
            .onFailure { e ->
            }
    }

    private suspend fun checkCharacterAndNavigateSync() {
        try {
            // 현재 토큰 상태 확인
            val currentToken = TokenManager.getAccessToken()

            val result = AuthRepository.getMyCharacter()

            result.onSuccess { character ->
                navigateToMain(forceMain = true)
            }.onFailure { e ->
                if (e is HttpException) {

                    if (e.code() == 404) {
                        navigateToOnboarding()
                    } else {
                        navigateToMain(forceMain = true)
                    }
                } else {
                    navigateToMain(forceMain = true)
                }
            }
        } catch (e: Exception) {
            navigateToMain(forceMain = true)
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
