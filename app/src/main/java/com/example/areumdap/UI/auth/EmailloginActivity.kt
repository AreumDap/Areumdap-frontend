package com.example.areumdap.UI.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.areumdap.Network.AuthRepository
import com.example.areumdap.Network.TokenManager
import com.example.areumdap.UI.MainActivity
import com.example.areumdap.UI.Onboarding.OnboardingActivity
import com.example.areumdap.databinding.ActivityEmailLoginBinding
import kotlinx.coroutines.launch
import retrofit2.HttpException // [추가됨]

class EmailLoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmailLoginBinding
    private val tag = "EmailLoginActivity"

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

        // 비밀번호 찾기
        binding.tvForgotPw.setOnClickListener {
            Toast.makeText(this, "비밀번호 찾기 기능은 추후 구현 예정입니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performLogin() {
        val email = binding.etId.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // 입력값 검증
        if (email.isEmpty()) {
            Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
            binding.etId.requestFocus()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            binding.etPassword.requestFocus()
            return
        }

        // 테스트용 로그인 (1234 / 1234)
        if (email == "1234" && password == "1234") {
            Toast.makeText(this, "테스트 로그인 성공!", Toast.LENGTH_SHORT).show()
            saveLoginState()
            navigateToMain()
            return
        }

        // 이메일 형식 검증
        if (!isValidEmail(email)) {
            Toast.makeText(this, "올바른 이메일 형식을 입력해주세요.", Toast.LENGTH_SHORT).show()
            binding.etId.requestFocus()
            return
        }

        // 버튼 비활성화 (중복 클릭 방지)
        binding.btnLogin.isEnabled = false

        // API 호출
        lifecycleScope.launch {
            try {
                val result = AuthRepository.login(email, password)

                result.onSuccess { loginResponse ->
                    Toast.makeText(
                        this@EmailLoginActivity,
                        "${loginResponse.name}님 환영합니다!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // 로그인 상태 저장
                    val keepLogin = binding.cbKeepLogin.isChecked
                    if (keepLogin) {
                        saveLoginState()
                    }

                    // 로그인 성공 후 캐릭터 확인 로직 실행
                    checkCharacterAndNavigate()

                }.onFailure { error ->
                    Log.e(tag, "로그인 실패: ${error.message}")
                    Toast.makeText(
                        this@EmailLoginActivity,
                        error.message ?: "로그인에 실패했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnLogin.isEnabled = true
                }
            } catch (e: Exception) {
                Log.e(tag, "로그인 중 예외 발생: ${e.message}")
                Toast.makeText(this@EmailLoginActivity, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                binding.btnLogin.isEnabled = true
            }
        }
    }

    /**
     * [수정됨] 캐릭터 정보 조회 후 화면 분기
     */
    private fun checkCharacterAndNavigate() {
        lifecycleScope.launch {
            try {
                // 캐릭터 정보 조회 API 호출
                val result = AuthRepository.getMyCharacter()

                result.onSuccess {
                    // 성공 (200 OK) -> 캐릭터 있음 -> 메인으로
                    Log.d(tag, "캐릭터 확인됨: 메인으로 이동")
                    navigateToMain(forceMain = true)
                }.onFailure { e ->
                    // 실패 -> 에러 코드 확인
                    if (e is HttpException && e.code() == 404) {
                        // 404 Not Found -> 캐릭터 없음 -> 온보딩으로
                        Log.d(tag, "캐릭터 없음(404): 온보딩으로 이동")
                        navigateToOnboarding()
                    } else {
                        // 그 외 에러 -> 안전하게 메인으로 이동 (또는 에러 표시)
                        Log.e(tag, "캐릭터 조회 실패: ${e.message}")
                        navigateToMain(forceMain = true)
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "알 수 없는 오류: ${e.message}")
                // 예외 발생 시 안전하게 메인으로 이동
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

    // forceMain이 true면 무조건 메인으로 이동
    private fun navigateToMain(forceMain: Boolean = false) {
        val pref = getSharedPreferences("auth", MODE_PRIVATE)

        // API 결과로 forceMain=true가 오면 SharedPreferences 무시하고 메인으로
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
        // 온보딩으로 이동 시 로컬 완료 기록 초기화
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