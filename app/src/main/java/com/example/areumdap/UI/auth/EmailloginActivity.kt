package com.example.areumdap.UI.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.areumdap.R
import com.example.areumdap.UI.Onboarding.OnboardingActivity
import com.example.areumdap.databinding.ActivityEmailLoginBinding
import com.example.areumdap.data.api.ChatbotApiService
import com.example.areumdap.data.repository.AuthRepository
import com.example.areumdap.data.repository.ChatbotRepository
import com.example.areumdap.data.repository.UserRepository
import com.example.areumdap.data.source.RetrofitClient
import com.example.areumdap.data.source.TokenManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import retrofit2.HttpException

class EmailLoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmailLoginBinding
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TokenManager 초기화
        TokenManager.init(this)

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

        // 비밀번호 보기 토글
        binding.btnPasswordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.btnPasswordToggle.setImageResource(R.drawable.ic_visibility)
            } else {
                binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.btnPasswordToggle.setImageResource(R.drawable.ic_visibility_off)
            }
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }
    }

    // [수정됨] ToastDialogFragment를 사용하여 커스텀 토스트 띄우기
    private fun showCustomToast(message: String, isSuccess: Boolean = true) {
        // 액티비티가 종료된 상태라면 실행하지 않음
        if (isFinishing || isDestroyed) return

        // 성공/실패에 따른 아이콘 선택
        // (프로젝트에 있는 실제 아이콘 이름으로 확인해주세요. 예: ic_error 또는 ic_failure)
        val iconRes = if (isSuccess) {
            R.drawable.ic_success
        } else {
            R.drawable.ic_error
        }

        val toast = ToastDialogFragment(message, iconRes)
        // Activity에서는 supportFragmentManager를 사용합니다.
        toast.show(supportFragmentManager, "CustomToast")
    }

    private fun performLogin() {
        val email = binding.etId.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // --- 입력값 검증 ---
        if (email.isEmpty()) {
            showCustomToast("아이디(이메일)를 입력해주세요.", isSuccess = false)
            binding.etId.requestFocus()
            return
        }

        if (password.isEmpty()) {
            showCustomToast("비밀번호를 입력해주세요.", isSuccess = false)
            binding.etPassword.requestFocus()
            return
        }

        if (!isValidEmail(email)) {
            showCustomToast("이메일 형식이 올바르지 않아요.", isSuccess = false)
            binding.etId.requestFocus()
            return
        }

        // 버튼 비활성화 (중복 클릭 방지)
        binding.btnLogin.isEnabled = false

        // --- API 호출 ---
        lifecycleScope.launch {
            try {
                // AuthRepository.login 호출
                val result = AuthRepository.login(email, password)

                result.onSuccess { loginResponse ->
                    // 로그인 성공
                    showCustomToast("${loginResponse.name}님 환영합니다!", isSuccess = true)

                    // 로그인 유지 설정 확인
                    if (binding.cbKeepLogin.isChecked) {
                        saveLoginState()
                    }

                    // FCM 토큰 등록 후 화면 이동
                    assignTodayRecommendIfNeeded()
                    registerFcmTokenAndNavigate()

                }.onFailure { error ->
                    val errorMessage = error.message.toString()

                    when {
                        // 1. Retrofit HttpException인 경우
                        error is HttpException -> {
                            when (error.code()) {
                                400 -> showCustomToast("입력 정보를 다시 확인해주세요.", false)
                                401 -> showCustomToast("비밀번호가 일치하지 않습니다.", false)
                                403 -> showCustomToast("이미 탈퇴한 계정입니다.", false)
                                404 -> showCustomToast("존재하지 않는 이메일입니다.", false)
                                else -> showCustomToast("서버 오류가 발생했습니다. (코드: ${error.code()})", false)
                            }
                        }

                        // 2. 에러 메시지에 코드가 포함된 경우
                        errorMessage.contains("401") -> showCustomToast("비밀번호가 일치하지 않습니다.", false)
                        errorMessage.contains("404") -> showCustomToast("존재하지 않는 이메일입니다.", false)
                        errorMessage.contains("403") -> showCustomToast("사용할 수 없는 계정입니다.", false)
                        errorMessage.contains("400") -> showCustomToast("입력 형식이 잘못되었습니다.", false)

                        // 3. 그 외
                        else -> showCustomToast("로그인에 실패했어요. 잠시 후 다시 시도해 주세요", false)
                    }

                    // 실패 시 버튼 다시 활성화
                    binding.btnLogin.isEnabled = true
                }
            } catch (e: Exception) {
                showCustomToast("네트워크 오류가 발생했습니다.", isSuccess = false)
                binding.btnLogin.isEnabled = true
            }
        }
    }

    private fun registerFcmTokenAndNavigate() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                checkCharacterAndNavigate()
                return@addOnCompleteListener
            }

            val token = task.result

            lifecycleScope.launch {
                try {
                    UserRepository.updateFcmToken(token)
                } catch (e: Exception) {
                } finally {
                    checkCharacterAndNavigate()
                }
            }
        }
    }

    private suspend fun assignTodayRecommendIfNeeded() {
        val api = RetrofitClient.create(ChatbotApiService::class.java)
        val repo = ChatbotRepository(api)
        repo.assignTodayRecommendOnLogin()
            .onFailure { e ->
            }
    }

    private fun checkCharacterAndNavigate() {
        lifecycleScope.launch {
            try {
                val result = AuthRepository.getMyCharacter()
                result.onSuccess {
                    navigateToMain(forceMain = true)
                }.onFailure { e ->
                    // 404면 캐릭터 없음 -> 온보딩, 그 외엔 에러라도 메인으로
                    if ((e is HttpException && e.code() == 404) || e.message?.contains("404") == true) {
                        navigateToOnboarding()
                    } else {
                        navigateToMain(forceMain = true)
                    }
                }
            } catch (e: Exception) {
                navigateToMain(forceMain = true)
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun saveLoginState() {
        getSharedPreferences("auth", MODE_PRIVATE)
            .edit()
            .putBoolean("keep_login", true)
            .apply()
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
}
