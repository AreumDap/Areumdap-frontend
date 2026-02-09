package com.example.areumdap.UI.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.areumdap.Network.AuthRepository
import com.example.areumdap.Network.TokenManager
import com.example.areumdap.R
import com.example.areumdap.UI.auth.EmailLoginActivity
import com.example.areumdap.databinding.ActivitySignupBinding
import com.example.areumdap.databinding.FragmentToastDialogBinding
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private var isEmailVerified = false
    private val tag = "SignupActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(tag, "SignupActivity 생성됨")

        // TokenManager 초기화
        TokenManager.init(this)

        initClickListeners()
    }

    private fun initClickListeners() {
        Log.d(tag, "클릭 리스너 초기화")

        // 뒤로가기 버튼
        binding.btnBack.setOnClickListener {
            Log.d(tag, "뒤로가기 버튼 클릭")
            finish()
        }

        // 이메일 인증 요청 버튼
        binding.btnEmailCheck.setOnClickListener {
            Log.d(tag, "이메일 인증 요청 버튼 클릭")
            requestEmailVerification()
        }

        // 인증번호 확인 버튼
        binding.btnAuthConfirm.setOnClickListener {
            Log.d(tag, "인증번호 확인 버튼 클릭")
            verifyAuthCode()
        }

        // 회원가입 버튼
        binding.btnSignUp.setOnClickListener {
            Log.d(tag, "=== 회원가입 버튼 클릭됨! ===")
            performSignup()
        }
    }

    // 커스텀 토스트 표시 함수
    private fun showCustomToast(message: String, isSuccess: Boolean = true) {
        val inflater = LayoutInflater.from(this)
        val toastBinding = FragmentToastDialogBinding.inflate(inflater)

        // 토스트 메시지 설정
        toastBinding.toastTv.text = message

        // 성공/실패에 따라 아이콘 변경
        if (isSuccess) {
            toastBinding.toastIv.setImageResource(R.drawable.ic_success)
        } else {
            toastBinding.toastIv.setImageResource(R.drawable.ic_failure)
        }

        Toast(this).apply {
            duration = Toast.LENGTH_SHORT
            view = toastBinding.root
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
            show()
        }
    }

    private fun requestEmailVerification() {
        Log.d(tag, "requestEmailVerification() 시작")

        val email = binding.etEmail.text.toString().trim()

        Log.d(tag, "이메일 인증 요청: $email")

        if (email.isEmpty()) {
            Log.w(tag, "이메일이 비어있음")
            showCustomToast("이메일을 입력해주세요.", isSuccess = false)
            return
        }

        if (!isValidEmail(email)) {
            Log.w(tag, "이메일 형식이 잘못됨: $email")
            showCustomToast("올바른 이메일 형식을 입력해주세요.", isSuccess = false)
            return
        }

        // 버튼 비활성화 (중복 클릭 방지)
        binding.btnEmailCheck.isEnabled = false

        // 실제 API 호출
        lifecycleScope.launch {
            Log.d(tag, "=== 이메일 인증 API 호출 시작 ===")
            Log.d(tag, "요청 이메일: $email")

            try {
                val result = AuthRepository.sendEmailVerificationCode(email)

                result.onSuccess {
                    Log.d(tag, "✅ 이메일 인증 요청 성공")
                    showCustomToast("인증번호가 발송되었습니다. 이메일을 확인해주세요.", isSuccess = true)

                    // 버튼 텍스트 변경
                    binding.btnEmailCheck.text = "재요청"
                    binding.btnEmailCheck.isEnabled = true
                }.onFailure { error ->
                    Log.e(tag, "❌ 이메일 인증 요청 실패")
                    Log.e(tag, "에러 메시지: ${error.message}")
                    Log.e(tag, "에러 타입: ${error.javaClass.simpleName}")

                    showCustomToast("아이디 또는 비밀번호가 올바르지 않아요", isSuccess = false)
                    binding.btnEmailCheck.isEnabled = true
                }
            } catch (e: Exception) {
                Log.e(tag, "이메일 인증 요청 중 예외 발생: ${e.message}", e)
                showCustomToast("이메일 인증 요청 중 오류가 발생했습니다", isSuccess = false)
                binding.btnEmailCheck.isEnabled = true
            }
        }
    }

    private fun verifyAuthCode() {
        Log.d(tag, "verifyAuthCode() 시작")

        val email = binding.etEmail.text.toString().trim()
        val authCode = binding.etAuthCode.text.toString().trim()

        Log.d(tag, "인증 시도: 이메일=$email, 인증번호=$authCode")

        if (authCode.isEmpty()) {
            Log.w(tag, "인증번호가 비어있음")
            showCustomToast("인증번호를 입력해주세요.", isSuccess = false)
            return
        }

        // 버튼 비활성화
        binding.btnAuthConfirm.isEnabled = false

        // 실제 API 호출
        lifecycleScope.launch {
            Log.d(tag, "=== 인증번호 확인 API 호출 시작 ===")
            Log.d(tag, "요청 데이터: email=$email, code=$authCode")

            try {
                val result = AuthRepository.confirmEmailVerificationCode(email, authCode)

                result.onSuccess {
                    Log.d(tag, "✅ 이메일 인증 확인 성공!")
                    isEmailVerified = true
                    showCustomToast("이메일 인증이 완료되었습니다.", isSuccess = true)

                    binding.btnAuthConfirm.text = "인증완료"
                    binding.etAuthCode.isEnabled = false

                    Log.d(tag, "✅ isEmailVerified 변경됨: $isEmailVerified")
                }.onFailure { error ->
                    Log.e(tag, "❌ 이메일 인증 확인 실패")
                    Log.e(tag, "에러 메시지: ${error.message}")
                    Log.e(tag, "에러 타입: ${error.javaClass.simpleName}")

                    showCustomToast("인증번호 확인에 실패했습니다. 다시 시도해 주세요", isSuccess = false)

                    binding.btnAuthConfirm.isEnabled = true
                }
            } catch (e: Exception) {
                Log.e(tag, "인증번호 확인 중 예외 발생: ${e.message}", e)
                showCustomToast("인증번호 확인 중 오류가 발생했습니다", isSuccess = false)
                binding.btnAuthConfirm.isEnabled = true
            }
        }
    }

    private fun performSignup() {
        Log.d(tag, "=== performSignup() 호출됨 ===")

        // 입력값 검증
        if (!validateInputs()) {
            Log.w(tag, "❌ 입력값 검증 실패")
            return
        }

        Log.d(tag, "✅ 입력값 검증 통과 - API 호출 준비")

        val name = binding.etName.text.toString().trim()
        val birth = binding.etBirth.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPw.text.toString().trim()

        // 생년월일 형식 변환: 20030213 → 2003-02-13
        val formattedBirth = formatBirth(birth)

        Log.d(tag, "📝 회원가입 요청 데이터:")
        Log.d(tag, "   - 이름: $name")
        Log.d(tag, "   - 원래 생년월일: $birth")
        Log.d(tag, "   - 변환된 생년월일: $formattedBirth")
        Log.d(tag, "   - 이메일: $email")
        Log.d(tag, "   - 비밀번호: ${"*".repeat(password.length)}")

        // 버튼 비활성화
        binding.btnSignUp.isEnabled = false

        // 실제 API 호출
        lifecycleScope.launch {
            Log.d(tag, "=== 🚀 회원가입 API 호출 시작 ===")

            try {
                val result = AuthRepository.signUp(name, formattedBirth, email, password)

                result.onSuccess {
                    Log.d(tag, "✅ 회원가입 API 성공! 로그인 화면으로 이동")
                    showCustomToast("회원가입이 완료되었습니다!", isSuccess = true)

                    // 회원가입 후 로그인 화면으로 이동
                    navigateToLogin()
                }.onFailure { error ->
                    Log.e(tag, "❌ 회원가입 API 실패")
                    Log.e(tag, "에러 메시지: ${error.message}")
                    Log.e(tag, "에러 타입: ${error.javaClass.simpleName}")

                    showCustomToast("회원가입 실패: ${error.message}", isSuccess = false)

                    binding.btnSignUp.isEnabled = true
                }
            } catch (e: Exception) {
                Log.e(tag, "❌ 회원가입 중 예외 발생")
                Log.e(tag, "예외 메시지: ${e.message}")
                Log.e(tag, "예외 타입: ${e.javaClass.simpleName}")

                showCustomToast("회원가입 중 오류 발생: ${e.message}", isSuccess = false)
                binding.btnSignUp.isEnabled = true
            }
        }
    }

    /**
     * 생년월일 형식 변환 함수
     * 20030213 → 2003-02-13
     */
    private fun formatBirth(birth: String): String {
        Log.d(tag, "formatBirth() 호출: 입력값 = $birth")

        if (birth.length == 8) {
            val year = birth.take(4)           // 2003
            val month = birth.drop(4).take(2)  // 02
            val day = birth.drop(6).take(2)    // 13
            val formatted = "$year-$month-$day" // 2003-02-13

            Log.d(tag, "생년월일 변환: $birth → $formatted")
            return formatted
        } else {
            Log.w(tag, "생년월일 형식이 잘못됨: $birth (길이: ${birth.length})")
            return birth
        }
    }

    private fun validateInputs(): Boolean {
        Log.d(tag, "🔍 validateInputs() 검증 시작")

        val name = binding.etName.text.toString().trim()
        val birth = binding.etBirth.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPw.text.toString().trim()
        val passwordConfirm = binding.etPwConfirm.text.toString().trim()

        Log.d(tag, "📋 검증할 데이터:")
        Log.d(tag, "   - 이름: '$name' (${if(name.isEmpty()) "비어있음" else "OK"})")
        Log.d(tag, "   - 생년월일: '$birth' (길이: ${birth.length})")
        Log.d(tag, "   - 이메일: '$email'")
        Log.d(tag, "   - 이메일 인증 상태: $isEmailVerified")
        Log.d(tag, "   - 비밀번호 길이: ${password.length}")
        Log.d(tag, "   - 비밀번호 확인 일치: ${password == passwordConfirm}")

        if (name.isEmpty()) {
            Log.w(tag, "❌ 이름 빈값 에러")
            showCustomToast("이름을 입력해주세요.", isSuccess = false)
            binding.etName.requestFocus()
            return false
        }

        if (birth.isEmpty()) {
            Log.w(tag, "❌ 생년월일 빈값 에러")
            showCustomToast("생년월일을 입력해주세요.", isSuccess = false)
            binding.etBirth.requestFocus()
            return false
        }

        if (birth.length != 8) {
            Log.w(tag, "❌ 생년월일 길이 에러: ${birth.length}자리 (8자리 필요)")
            showCustomToast("생년월일을 8자리로 입력해주세요. (예: 19900101)", isSuccess = false)
            binding.etBirth.requestFocus()
            return false
        }

        if (!isValidBirth(birth)) {
            Log.w(tag, "❌ 생년월일 날짜 유효성 에러: $birth")
            showCustomToast("올바른 날짜를 입력해주세요.", isSuccess = false)
            binding.etBirth.requestFocus()
            return false
        }

        if (email.isEmpty()) {
            Log.w(tag, "❌ 이메일 빈값 에러")
            showCustomToast("이메일을 입력해주세요.", isSuccess = false)
            binding.etEmail.requestFocus()
            return false
        }

        if (!isValidEmail(email)) {
            Log.w(tag, "❌ 이메일 형식 에러: $email")
            showCustomToast("올바른 이메일 형식을 입력해주세요.", isSuccess = false)
            binding.etEmail.requestFocus()
            return false
        }

        if (!isEmailVerified) {
            Log.w(tag, "❌ 이메일 미인증 에러 - isEmailVerified = $isEmailVerified")
            Log.w(tag, "이메일 인증을 먼저 완료해야 합니다!")
            showCustomToast("이메일 인증을 완료해주세요.", isSuccess = false)
            return false
        }

        if (password.isEmpty()) {
            Log.w(tag, "❌ 비밀번호 빈값 에러")
            showCustomToast("비밀번호를 입력해주세요.", isSuccess = false)
            binding.etPw.requestFocus()
            return false
        }

        if (password.length < 6) {
            Log.w(tag, "❌ 비밀번호 길이 에러: ${password.length}자리 (최소 6자리)")
            showCustomToast("비밀번호는 6자리 이상 입력해주세요.", isSuccess = false)
            binding.etPw.requestFocus()
            return false
        }

        if (password != passwordConfirm) {
            Log.w(tag, "❌ 비밀번호 확인 불일치 에러")
            showCustomToast("비밀번호가 일치하지 않습니다.", isSuccess = false)
            binding.etPwConfirm.requestFocus()
            return false
        }

        Log.d(tag, "✅ 모든 입력값 검증 통과!")
        return true
    }

    /**
     * 생년월일 날짜 유효성 검증
     */
    private fun isValidBirth(birth: String): Boolean {
        if (birth.length != 8) return false

        try {
            val year = birth.take(4).toInt()
            val month = birth.drop(4).take(2).toInt()
            val day = birth.drop(6).take(2).toInt()

            Log.d(tag, "생년월일 파싱: ${year}년 ${month}월 ${day}일")

            // 기본적인 날짜 유효성 검증
            if (year !in 1900..2024) {
                Log.w(tag, "연도 범위 초과: $year")
                return false
            }
            if (month !in 1..12) {
                Log.w(tag, "월 범위 초과: $month")
                return false
            }
            if (day !in 1..31) {
                Log.w(tag, "일 범위 초과: $day")
                return false
            }

            // 월별 일수 검증 (간단한 버전)
            val maxDayInMonth = when (month) {
                2 -> 29 // 윤년 고려 안함 (간단화)
                4, 6, 9, 11 -> 30
                else -> 31
            }

            if (day > maxDayInMonth) {
                Log.w(tag, "${month}월 일수 초과: $day (최대: $maxDayInMonth)")
                return false
            }

            Log.d(tag, "생년월일 유효성 검증 통과")
            return true
        } catch (e: Exception) {
            Log.e(tag, "생년월일 파싱 실패: ${e.message}")
            return false
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val isValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        Log.d(tag, "이메일 형식 검증: $email -> $isValid")
        return isValid
    }

    private fun navigateToLogin() {
        Log.d(tag, "로그인 화면으로 이동")
        val intent = Intent(this, EmailLoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}