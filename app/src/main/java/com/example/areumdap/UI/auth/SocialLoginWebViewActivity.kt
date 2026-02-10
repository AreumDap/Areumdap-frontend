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

        Log.d(tag, "==================================================")
        Log.d(tag, "🚀 SocialLoginWebViewActivity 시작")
        Log.d(tag, "로그인 타입: $loginType")
        Log.d(tag, "로그인 URL: $loginUrl")
        Log.d(tag, "==================================================")

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
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(true)
        }

        binding.webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                Log.d(tag, "🌐 URL 로딩 감지: $url")

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
                        Log.e(tag, "Intent 처리 실패: ${e.message}")
                    }
                } else if (url.startsWith("market://")) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                        return true
                    } catch (e: Exception) {
                        Log.e(tag, "Market 처리 실패: ${e.message}")
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
                    Log.d(tag, "📄 페이지 완료: $url")
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
                        Log.d(tag, "🔑 카카오 인가 코드 획득: $code")
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
                        Log.d(tag, "🔑 네이버 인가 코드 획득")
                        Log.d(tag, "   code: $code")
                        Log.d(tag, "   state: $state")
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
        Log.e(tag, "❌ 로그인 에러: $errorDescription")
        Toast.makeText(this, errorDescription, Toast.LENGTH_SHORT).show()
        finish()
    }

    /**
     * 카카오 로그인 처리 (완전 순차 실행)
     */
    private fun processKakaoLogin(code: String) {
        lifecycleScope.launch {
            Log.d(tag, "==================================================")
            Log.d(tag, "1️⃣ [카카오 로그인] API 호출 시작")
            Log.d(tag, "   전송할 code: $code")

            val result = SocialAuthRepository.loginWithKakaoCode(code)

            if (result.isSuccess) {
                val response = result.getOrNull()
                Log.d(tag, "2️⃣ [카카오 로그인] API 성공!")
                Log.d(tag, "   userId: ${response?.userId}")
                Log.d(tag, "   name: ${response?.name}")
                Log.d(tag, "   email: ${response?.email}")
                Log.d(tag, "   accessToken: ${response?.accessToken?.take(30)}...")
                Log.d(tag, "   refreshToken: ${response?.refreshToken?.take(30)}...")

                // 토큰 저장 확인
                val savedAccessToken = TokenManager.getAccessToken()
                val savedUserId = TokenManager.getUserId()
                Log.d(tag, "3️⃣ [토큰 저장 확인]")
                Log.d(tag, "   저장된 accessToken: ${savedAccessToken?.take(30)}...")
                Log.d(tag, "   저장된 userId: $savedUserId")

                Toast.makeText(
                    this@SocialLoginWebViewActivity,
                    "${response?.name ?: "회원"}님 환영합니다!",
                    Toast.LENGTH_SHORT
                ).show()

                saveLoginState()

                // FCM 토큰 등록
                Log.d(tag, "4️⃣ [FCM 토큰] 등록 시작")
                registerFcmTokenSync()
                Log.d(tag, "5️⃣ [FCM 토큰] 등록 완료")

                // 캐릭터 확인
                Log.d(tag, "6️⃣ [캐릭터 확인] API 호출 시작")
                checkCharacterAndNavigateSync()

            } else {
                val error = result.exceptionOrNull()
                Log.e(tag, "❌ [카카오 로그인] API 실패!")
                Log.e(tag, "   에러 메시지: ${error?.message}")
                Log.e(tag, "   에러 타입: ${error?.javaClass?.simpleName}")
                Log.d(tag, "==================================================")

                Toast.makeText(
                    this@SocialLoginWebViewActivity,
                    error?.message ?: "카카오 로그인에 실패했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    /**
     * 네이버 로그인 처리 (완전 순차 실행)
     */
    private fun processNaverLogin(code: String, state: String) {
        lifecycleScope.launch {
            Log.d(tag, "==================================================")
            Log.d(tag, "1️⃣ [네이버 로그인] API 호출 시작")
            Log.d(tag, "   전송할 code: $code")
            Log.d(tag, "   전송할 state: $state")

            val result = SocialAuthRepository.loginWithNaverCode(code, state)

            if (result.isSuccess) {
                val response = result.getOrNull()
                Log.d(tag, "2️⃣ [네이버 로그인] API 성공!")
                Log.d(tag, "   userId: ${response?.userId}")
                Log.d(tag, "   name: ${response?.name}")
                Log.d(tag, "   email: ${response?.email}")
                Log.d(tag, "   accessToken: ${response?.accessToken?.take(30)}...")
                Log.d(tag, "   refreshToken: ${response?.refreshToken?.take(30)}...")

                // 토큰 저장 확인
                val savedAccessToken = TokenManager.getAccessToken()
                val savedUserId = TokenManager.getUserId()
                Log.d(tag, "3️⃣ [토큰 저장 확인]")
                Log.d(tag, "   저장된 accessToken: ${savedAccessToken?.take(30)}...")
                Log.d(tag, "   저장된 userId: $savedUserId")

                Toast.makeText(
                    this@SocialLoginWebViewActivity,
                    "${response?.name ?: "회원"}님 환영합니다!",
                    Toast.LENGTH_SHORT
                ).show()

                saveLoginState()

                // FCM 토큰 등록
                Log.d(tag, "4️⃣ [FCM 토큰] 등록 시작")
                registerFcmTokenSync()
                Log.d(tag, "5️⃣ [FCM 토큰] 등록 완료")

                // 캐릭터 확인
                Log.d(tag, "6️⃣ [캐릭터 확인] API 호출 시작")
                checkCharacterAndNavigateSync()

            } else {
                val error = result.exceptionOrNull()
                Log.e(tag, "❌ [네이버 로그인] API 실패!")
                Log.e(tag, "   에러 메시지: ${error?.message}")
                Log.e(tag, "   에러 타입: ${error?.javaClass?.simpleName}")
                Log.d(tag, "==================================================")

                Toast.makeText(
                    this@SocialLoginWebViewActivity,
                    error?.message ?: "네이버 로그인에 실패했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
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
                    Log.d(tag, "   FCM 토큰 획득: ${token.take(30)}...")

                    lifecycleScope.launch {
                        try {
                            UserRepository.updateFcmToken(token)
                            Log.d(tag, "   FCM 토큰 서버 등록 성공")
                        } catch (e: Exception) {
                            Log.e(tag, "   FCM 등록 실패: ${e.message}")
                        }
                        continuation.resume(Unit)
                    }
                } else {
                    Log.e(tag, "   FCM 토큰 가져오기 실패: ${task.exception?.message}")
                    continuation.resume(Unit)
                }
            }
        }
    }

    /**
     * 캐릭터 확인 후 화면 이동 (suspend - 완료될 때까지 대기)
     */
    private suspend fun checkCharacterAndNavigateSync() {
        try {
            // 현재 토큰 상태 확인
            val currentToken = TokenManager.getAccessToken()
            Log.d(tag, "   현재 저장된 토큰: ${currentToken?.take(30)}...")

            val result = AuthRepository.getMyCharacter()

            result.onSuccess { character ->
                Log.d(tag, "7️⃣ [캐릭터 확인] 성공! 캐릭터 있음")
                Log.d(tag, "   캐릭터 정보: $character")
                Log.d(tag, "   → 메인 화면으로 이동")
                Log.d(tag, "==================================================")
                navigateToMain(forceMain = true)
            }.onFailure { e ->
                if (e is HttpException) {
                    Log.d(tag, "7️⃣ [캐릭터 확인] HTTP 에러")
                    Log.d(tag, "   HTTP 코드: ${e.code()}")
                    Log.d(tag, "   메시지: ${e.message()}")

                    if (e.code() == 404) {
                        Log.d(tag, "   → 캐릭터 없음, 온보딩으로 이동")
                        Log.d(tag, "==================================================")
                        navigateToOnboarding()
                    } else {
                        Log.e(tag, "   → 기타 에러, 메인으로 이동")
                        Log.d(tag, "==================================================")
                        navigateToMain(forceMain = true)
                    }
                } else {
                    Log.e(tag, "7️⃣ [캐릭터 확인] 기타 에러")
                    Log.e(tag, "   에러 타입: ${e.javaClass.simpleName}")
                    Log.e(tag, "   에러 메시지: ${e.message}")
                    Log.d(tag, "   → 메인으로 이동")
                    Log.d(tag, "==================================================")
                    navigateToMain(forceMain = true)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "7️⃣ [캐릭터 확인] 예외 발생!")
            Log.e(tag, "   예외 타입: ${e.javaClass.simpleName}")
            Log.e(tag, "   예외 메시지: ${e.message}")
            e.printStackTrace()
            Log.d(tag, "   → 메인으로 이동")
            Log.d(tag, "==================================================")
            navigateToMain(forceMain = true)
        }
    }

    private fun saveLoginState() {
        getSharedPreferences("auth", MODE_PRIVATE)
            .edit()
            .putBoolean("keep_login", true)
            .apply()
        Log.d(tag, "   로그인 상태 저장 완료 (keep_login = true)")
    }

    private fun navigateToOnboarding() {
        getSharedPreferences("auth", MODE_PRIVATE)
            .edit()
            .putBoolean("onboarding_done", false)
            .apply()

        Log.d(tag, "🚪 OnboardingActivity로 이동")
        val intent = Intent(this, OnboardingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToMain(forceMain: Boolean = false) {
        val pref = getSharedPreferences("auth", MODE_PRIVATE)
        val isOnboardingDone = if (forceMain) true else pref.getBoolean("onboarding_done", false)

        val intent = if (isOnboardingDone) {
            Log.d(tag, "🚪 MainActivity로 이동")
            Intent(this, MainActivity::class.java)
        } else {
            Log.d(tag, "🚪 OnboardingActivity로 이동 (온보딩 미완료)")
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