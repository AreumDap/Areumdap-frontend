package com.example.areumdap.UI.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.areumdap.UI.Onboarding.OnboardingActivity
import com.example.areumdap.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private var isEmailVerified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initClickListeners()
    }

    private fun initClickListeners() {
        // 뒤로가기 버튼
        binding.btnBack.setOnClickListener {
            finish()
        }

        // 이메일 인증 요청 버튼
        binding.btnEmailCheck.setOnClickListener {
            requestEmailVerification()
        }

        // 인증번호 확인 버튼
        binding.btnAuthConfirm.setOnClickListener {
            verifyAuthCode()
        }

        // 회원가입 버튼
        binding.btnSignUp.setOnClickListener {
            performSignup()
        }
    }

    private fun requestEmailVerification() {
        val email = binding.etEmail.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "올바른 이메일 형식을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "인증번호가 발송되었습니다. (임시: 1234)", Toast.LENGTH_LONG).show()
        binding.btnEmailCheck.text = "재요청"
    }

    private fun verifyAuthCode() {
        val authCode = binding.etAuthCode.text.toString().trim()

        if (authCode.isEmpty()) {
            Toast.makeText(this, "인증번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (authCode == "1234") {
            isEmailVerified = true
            Toast.makeText(this, "이메일 인증이 완료되었습니다.", Toast.LENGTH_SHORT).show()
            binding.btnAuthConfirm.text = "인증완료"
            binding.btnAuthConfirm.isEnabled = false
            binding.etAuthCode.isEnabled = false
        } else {
            Toast.makeText(this, "인증번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performSignup() {
        if (!validateInputs()) {
            return
        }

        Toast.makeText(this, "회원가입이 완료되었습니다!", Toast.LENGTH_SHORT).show()
        navigateToOnboarding()
    }

    private fun validateInputs(): Boolean {
        val name = binding.etName.text.toString().trim()
        val birth = binding.etBirth.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPw.text.toString().trim()
        val passwordConfirm = binding.etPwConfirm.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
            binding.etName.requestFocus()
            return false
        }

        if (birth.isEmpty()) {
            Toast.makeText(this, "생년월일을 입력해주세요.", Toast.LENGTH_SHORT).show()
            binding.etBirth.requestFocus()
            return false
        }

        if (birth.length != 8) {
            Toast.makeText(this, "생년월일을 8자리로 입력해주세요. (예: 19900101)", Toast.LENGTH_SHORT).show()
            binding.etBirth.requestFocus()
            return false
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
            binding.etEmail.requestFocus()
            return false
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "올바른 이메일 형식을 입력해주세요.", Toast.LENGTH_SHORT).show()
            binding.etEmail.requestFocus()
            return false
        }

        if (!isEmailVerified) {
            Toast.makeText(this, "이메일 인증을 완료해주세요.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            binding.etPw.requestFocus()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(this, "비밀번호는 6자리 이상 입력해주세요.", Toast.LENGTH_SHORT).show()
            binding.etPw.requestFocus()
            return false
        }

        if (password != passwordConfirm) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            binding.etPwConfirm.requestFocus()
            return false
        }

        return true
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun navigateToOnboarding() {
        val intent = Intent(this, OnboardingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}