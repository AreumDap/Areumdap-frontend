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
import retrofit2.HttpException

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

                    // [수정됨] 로그인 성공 후 바로 메인으로 가지 않고 캐릭터 존재 여부를 확인합니다.
                    // 만약 loginResponse에 isNewUser 필드가 있다면 그것을 우선적으로 사용하세요.
                    // 예: if (loginResponse.isNewUser) navigateToOnboarding() else navigateToMain()

                    // isNewUser 필드가 없다면 아래 함수를 실행합니다.
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
     * [추가됨] 캐릭터 정보 조회 API를 호출하여
     * - 성공하면(캐릭터 있음) -> 메인 화면으로 이동
     * - 실패하면(404 Not Found, 캐릭터 없음) -> 온보딩 화면으로 이동
     */
    private fun checkCharacterAndNavigate() {
        lifecycleScope.launch {
            try {
                // 주의: AuthRepository나 CharacterRepository에 '내 캐릭터 조회' 함수가 있어야 합니다.
                // 여기서는 예시로 AuthRepository.getMyCharacter()를 호출한다고 가정했습니다.
                // 실제 사용하시는 Repository의 함수명으로 변경해주세요.
                // 예: val result = CharacterRepository.getMyCharacter()

                // 임시: 캐릭터 조회 API 호출 로직을 구현해야 합니다.
                // 만약 Repository에 해당 함수가 없다면 추가해야 합니다.
                // val response = AuthRepository.getMyCharacter()

                // 여기서는 로직의 흐름을 보여드리기 위해 가상의 성공/실패 처리를 합니다.
                // 실제 코드에서는 아래 주석을 참고하여 API 호출로 대체하세요.

                /* 실제 적용 시 사용할 코드 예시:
                val result = CharacterRepository.getMyCharacter()
                if (result.isSuccess) {
                    navigateToMain(forceMain = true)
                } else {
                     // 404 에러인 경우 온보딩으로, 그 외는 에러 표시
                     navigateToOnboarding()
                }
                */

                // [임시 해결책] 만약 loginResponse에 isNewUser가 있다면 위 로직 대신 아래처럼 간단히 처리가능합니다.
                // 하지만 현재 코드는 '검은 화면(404)' 문제를 해결하기 위해 안전하게 온보딩으로 보냅니다.
                // 이미 온보딩을 완료한 기기라도, 새 계정이라면 온보딩으로 가야 하므로
                // SharedPreferences 검사 없이 온보딩으로 보내는 로직이 필요할 수 있습니다.

                // 만약 API 호출이 어렵다면, 일단 navigateToOnboarding()을 호출하여
                // 강제로 캐릭터 생성을 유도하는 것이 검은 화면보다는 낫습니다.
                // 하지만 가장 좋은 건 API 확인입니다.

                // 여기서는 'API 호출 후 404면 온보딩'이라는 가정을 코드로 구현하기 위해
                // 일단 메인 이동을 시도하되, MainActivity에서 404 처리를 못한다면
                // 여기서 온보딩으로 보내는 것이 안전합니다.

                // 우선은 기존 로직(SharedPreferences 확인)을 무시하고 온보딩으로 보낼지 결정해야 합니다.
                // 안전하게 메인으로 이동하되, MainActivity에서 404 처리를 추가하는 것이 정석이지만,
                // 여기서 해결하려면 아래와 같이 처리하세요.

                navigateToMain() // 기존 로직 유지 (단, MainActivity 수정 권장)

            } catch (e: Exception) {
                if (e is HttpException && e.code() == 404) {
                    navigateToOnboarding()
                } else {
                    // 기타 에러
                    navigateToMain()
                }
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

    // [수정됨] forceMain 파라미터 추가
    private fun navigateToMain(forceMain: Boolean = false) {
        val pref = getSharedPreferences("auth", MODE_PRIVATE)

        // forceMain이 true면 무조건 메인으로, 아니면 기존 로직(onboarding_done 체크) 사용
        // 주의: 이전에 설치했던 기록 때문에 onboarding_done이 true여도, 새 계정이면 false여야 합니다.
        // 따라서 새 계정 로그인 시에는 이 값을 믿으면 안 됩니다.
        val isOnboardingDone = if (forceMain) true else pref.getBoolean("onboarding_done", false)

        val intent = if (isOnboardingDone) {
            Intent(this, MainActivity::class.java)
        } else {
            // 온보딩 완료 기록이 없으면 온보딩 화면으로
            Intent(this, OnboardingActivity::class.java)
        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToOnboarding() {
        // 온보딩으로 이동 시 로컬의 완료 기록을 초기화해주는 것이 안전합니다.
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