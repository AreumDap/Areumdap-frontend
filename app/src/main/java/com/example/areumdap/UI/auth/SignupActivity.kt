package com.example.areumdap.UI.auth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.areumdap.R
import com.example.areumdap.data.repository.AuthRepository
import com.example.areumdap.data.source.TokenManager
import com.example.areumdap.databinding.ActivitySignupBinding
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private var isEmailVerified = false
    private var verifiedEmail = ""
    private val tag = "SignupActivity"

    // 비밀번호 보이기/숨기기 상태
    private var isPwVisible = false
    private var isPwConfirmVisible = false

    // 색상 정의
    private val colorError = Color.parseColor("#EB7383")
    private val colorSuccess = Color.parseColor("#4CAF50")

    // 비밀번호 검증 딜레이를 위한 Handler
    private val handler = Handler(Looper.getMainLooper())
    private var passwordValidationRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(tag, "SignupActivity 생성됨")

        // TokenManager 초기화
        TokenManager.init(this)

        initClickListeners()
        initTextWatchers()
        initPasswordToggle()
        updateSignUpButtonState()
    }

    private fun initClickListeners() {
        Log.d(tag, "클릭 리스너 초기화")

        binding.btnBack.setOnClickListener { finish() }

        binding.btnEmailCheck.setOnClickListener { requestEmailVerification() }

        binding.btnAuthConfirm.setOnClickListener { verifyAuthCode() }

        binding.btnSignUp.setOnClickListener { performSignup() }
    }

    /**
     * 비밀번호 보이기/숨기기 토글 설정
     */
    private fun initPasswordToggle() {
        binding.btnPwToggle.setOnClickListener {
            isPwVisible = !isPwVisible
            togglePasswordVisibility(binding.etPw, binding.btnPwToggle, isPwVisible)
        }

        binding.btnPwConfirmToggle.setOnClickListener {
            isPwConfirmVisible = !isPwConfirmVisible
            togglePasswordVisibility(binding.etPwConfirm, binding.btnPwConfirmToggle, isPwConfirmVisible)
        }
    }

    private fun togglePasswordVisibility(
        editText: android.widget.EditText,
        toggleButton: android.widget.ImageButton,
        isVisible: Boolean
    ) {
        if (isVisible) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            toggleButton.setImageResource(R.drawable.ic_visibility)
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            toggleButton.setImageResource(R.drawable.ic_visibility_off)
        }
        editText.setSelection(editText.text.length)
    }

    /**
     * 실시간 유효성 검사를 위한 TextWatcher 설정
     */
    private fun initTextWatchers() {
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString().trim()

                if (verifiedEmail.isNotEmpty() && email != verifiedEmail) {
                    isEmailVerified = false
                    verifiedEmail = ""
                    binding.btnAuthConfirm.text = "확인"
                    binding.btnAuthConfirm.isEnabled = true
                    binding.etAuthCode.isEnabled = true
                    binding.etAuthCode.text?.clear()
                    hideMessage(binding.tvAuthMessage)
                }

                if (email.isNotEmpty()) {
                    if (!isValidEmail(email)) {
                        showMessage(binding.tvEmailMessage, "이메일 형식과 일치하지 않습니다.", false)
                    } else {
                        hideMessage(binding.tvEmailMessage)
                    }
                } else {
                    hideMessage(binding.tvEmailMessage)
                }
                updateSignUpButtonState()
            }
        })

        binding.etPw.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                passwordValidationRunnable?.let { handler.removeCallbacks(it) }
                passwordValidationRunnable = Runnable { validatePasswordField() }
                handler.postDelayed(passwordValidationRunnable!!, 1000)

                validatePasswordConfirmField()
                updateSignUpButtonState()
            }
        })

        binding.etPwConfirm.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validatePasswordConfirmField()
                updateSignUpButtonState()
            }
        })

        binding.etName.addTextChangedListener(simpleTextWatcher { updateSignUpButtonState() })
        binding.etBirth.addTextChangedListener(simpleTextWatcher { updateSignUpButtonState() })
    }

    private fun simpleTextWatcher(afterChanged: () -> Unit): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { afterChanged() }
        }
    }

    private fun validatePasswordField() {
        val password = binding.etPw.text.toString()
        if (password.isNotEmpty()) {
            if (!isValidPassword(password)) {
                showMessage(binding.tvPwMessage, "비밀번호 형식이 올바르지 않습니다.", false)
            } else {
                hideMessage(binding.tvPwMessage)
            }
        } else {
            hideMessage(binding.tvPwMessage)
        }
    }

    private fun validatePasswordConfirmField() {
        val password = binding.etPw.text.toString()
        val passwordConfirm = binding.etPwConfirm.text.toString()

        if (passwordConfirm.isNotEmpty()) {
            if (password != passwordConfirm) {
                showMessage(binding.tvPwConfirmMessage, "비밀번호가 일치하지 않습니다.", false)
            } else {
                hideMessage(binding.tvPwConfirmMessage)
            }
        } else {
            hideMessage(binding.tvPwConfirmMessage)
        }
    }

    private fun showMessage(textView: TextView, message: String, isSuccess: Boolean) {
        textView.text = message
        textView.setTextColor(if (isSuccess) colorSuccess else colorError)
        textView.visibility = View.VISIBLE
    }

    private fun hideMessage(textView: TextView) {
        textView.visibility = View.GONE
    }

    private fun updateSignUpButtonState() {
        val name = binding.etName.text.toString().trim()
        val birth = binding.etBirth.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPw.text.toString()
        val passwordConfirm = binding.etPwConfirm.text.toString()

        val isNameValid = name.isNotEmpty()
        val isBirthValid = birth.length == 8 && isValidBirth(birth)
        val isEmailValid = isValidEmail(email)
        val isPasswordValid = isValidPassword(password)
        val isPasswordMatch = password == passwordConfirm && passwordConfirm.isNotEmpty()

        val canSignUp = isNameValid && isBirthValid && isEmailValid &&
                isEmailVerified && isPasswordValid && isPasswordMatch

        binding.btnSignUp.isEnabled = canSignUp
        binding.btnSignUp.alpha = if (canSignUp) 1.0f else 0.5f
    }

    // ============ [수정됨] 커스텀 토스트 표시 함수 ============
    private fun showCustomToast(message: String, isSuccess: Boolean = true) {
        // 액티비티가 종료된 상태라면 토스트를 띄우지 않음
        if (isFinishing || isDestroyed) return

        // 성공 여부에 따라 아이콘 선택
        // (프로젝트의 실제 아이콘 리소스 이름과 맞춰주세요. 예: ic_error 또는 ic_failure)
        val iconRes = if (isSuccess) {
            R.drawable.ic_success
        } else {
            // 기존 코드에 ic_failure라고 되어 있어서 유지했습니다.
            // 만약 ic_error로 통일했다면 R.drawable.ic_error 로 변경하세요.
            R.drawable.ic_failure
        }

        val toast = ToastDialogFragment(message, iconRes)
        // AppCompatActivity이므로 supportFragmentManager 사용
        toast.show(supportFragmentManager, "CustomToast")
    }

    private fun requestEmailVerification() {
        val email = binding.etEmail.text.toString().trim()

        if (email.isEmpty()) {
            showMessage(binding.tvEmailMessage, "이메일을 입력해주세요.", false)
            return
        }

        if (!isValidEmail(email)) {
            showMessage(binding.tvEmailMessage, "이메일 형식과 일치하지 않습니다.", false)
            return
        }

        binding.btnEmailCheck.isEnabled = false
        hideMessage(binding.tvEmailMessage)

        lifecycleScope.launch {
            try {
                val result = AuthRepository.sendEmailVerificationCode(email)
                result.onSuccess {
                    showMessage(binding.tvEmailMessage, "입력하신 이메일로 인증번호를 발송했습니다.", true)
                    binding.btnEmailCheck.text = "재요청"
                    binding.btnEmailCheck.isEnabled = true
                }.onFailure { error ->
                    if (error is HttpException) {
                        when (error.code()) {
                            400, 401, 403, 404 -> showMessage(binding.tvEmailMessage, "이메일 인증 요청에 실패했습니다.", false)
                            else -> showCustomToast("잠시 후 다시 시도해 주세요", isSuccess = false)
                        }
                    } else {
                        showCustomToast("잠시 후 다시 시도해 주세요", isSuccess = false)
                    }
                    binding.btnEmailCheck.isEnabled = true
                }
            } catch (e: Exception) {
                showCustomToast("다시 시도해 주세요", isSuccess = false)
                binding.btnEmailCheck.isEnabled = true
            }
        }
    }

    private fun verifyAuthCode() {
        val email = binding.etEmail.text.toString().trim()
        val authCode = binding.etAuthCode.text.toString().trim()

        if (authCode.isEmpty()) {
            showMessage(binding.tvAuthMessage, "인증번호를 입력해주세요.", false)
            return
        }
        if (authCode.length != 6) {
            showMessage(binding.tvAuthMessage, "인증번호 6자리를 입력해주세요.", false)
            return
        }

        binding.btnAuthConfirm.isEnabled = false
        hideMessage(binding.tvAuthMessage)

        lifecycleScope.launch {
            try {
                val result = AuthRepository.confirmEmailVerificationCode(email, authCode)
                result.onSuccess {
                    isEmailVerified = true
                    verifiedEmail = email
                    showMessage(binding.tvAuthMessage, "인증이 완료되었습니다.", true)
                    binding.btnAuthConfirm.text = "인증완료"
                    binding.etAuthCode.isEnabled = false
                    updateSignUpButtonState()
                }.onFailure { error ->
                    if (error is HttpException) {
                        when (error.code()) {
                            400, 401, 403, 404 -> showMessage(binding.tvAuthMessage, "인증번호가 일치하지 않습니다.", false)
                            else -> showCustomToast("잠시 후 다시 시도해 주세요", isSuccess = false)
                        }
                    } else {
                        showMessage(binding.tvAuthMessage, "인증번호가 일치하지 않습니다.", false)
                    }
                    binding.btnAuthConfirm.isEnabled = true
                }
            } catch (e: Exception) {
                showCustomToast("다시 시도해 주세요", isSuccess = false)
                binding.btnAuthConfirm.isEnabled = true
            }
        }
    }

    private fun performSignup() {
        if (!validateInputs()) return

        val name = binding.etName.text.toString().trim()
        val birth = binding.etBirth.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPw.text.toString().trim()
        val formattedBirth = formatBirth(birth)

        binding.btnSignUp.isEnabled = false
        binding.btnSignUp.alpha = 0.5f

        lifecycleScope.launch {
            try {
                val result = AuthRepository.signUp(name, formattedBirth, email, password)
                result.onSuccess {
                    showCustomToast("회원가입이 완료되었습니다!", isSuccess = true)
                    navigateToLogin()
                }.onFailure { error ->
                    if (error is HttpException) {
                        when (error.code()) {
                            409 -> showMessage(binding.tvEmailMessage, "이미 가입된 이메일입니다.", false)
                            400 -> showCustomToast("입력 정보를 다시 확인해주세요", isSuccess = false)
                            else -> showCustomToast("회원가입에 실패했습니다. 잠시 후 다시 시도해 주세요", isSuccess = false)
                        }
                    } else {
                        showCustomToast("회원가입에 실패했습니다. 잠시 후 다시 시도해 주세요", isSuccess = false)
                    }
                    binding.btnSignUp.isEnabled = true
                    binding.btnSignUp.alpha = 1.0f
                }
            } catch (e: Exception) {
                showCustomToast("회원가입에 실패했습니다. 다시 시도해 주세요", isSuccess = false)
                binding.btnSignUp.isEnabled = true
                binding.btnSignUp.alpha = 1.0f
            }
        }
    }

    private fun formatBirth(birth: String): String {
        return if (birth.length == 8) {
            val year = birth.take(4)
            val month = birth.drop(4).take(2)
            val day = birth.drop(6).take(2)
            "$year-$month-$day"
        } else {
            birth
        }
    }

    private fun validateInputs(): Boolean {
        val name = binding.etName.text.toString().trim()
        val birth = binding.etBirth.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPw.text.toString().trim()
        val passwordConfirm = binding.etPwConfirm.text.toString().trim()

        if (name.isEmpty()) {
            showCustomToast("이름을 입력해주세요.", isSuccess = false)
            binding.etName.requestFocus()
            return false
        }
        if (birth.isEmpty() || birth.length != 8) {
            showCustomToast("생년월일을 8자리로 입력해주세요.", isSuccess = false)
            binding.etBirth.requestFocus()
            return false
        }
        if (!isValidBirth(birth)) {
            showCustomToast("올바른 생년월일을 입력해주세요.", isSuccess = false)
            binding.etBirth.requestFocus()
            return false
        }
        if (email.isEmpty() || !isValidEmail(email)) {
            showMessage(binding.tvEmailMessage, "이메일 형식과 일치하지 않습니다.", false)
            binding.etEmail.requestFocus()
            return false
        }
        if (!isEmailVerified) {
            showMessage(binding.tvEmailMessage, "이메일 인증을 완료해주세요.", false)
            return false
        }
        if (!isValidPassword(password)) {
            showMessage(binding.tvPwMessage, "비밀번호 형식이 올바르지 않습니다.", false)
            binding.etPw.requestFocus()
            return false
        }
        if (password != passwordConfirm) {
            showMessage(binding.tvPwConfirmMessage, "비밀번호가 일치하지 않습니다.", false)
            binding.etPwConfirm.requestFocus()
            return false
        }
        return true
    }

    private fun isValidBirth(birth: String): Boolean {
        if (birth.length != 8) return false
        try {
            val year = birth.take(4).toInt()
            val month = birth.drop(4).take(2).toInt()
            val day = birth.drop(6).take(2).toInt()
            if (year !in 1900..2024) return false
            if (month !in 1..12) return false
            if (day !in 1..31) return false
            val maxDayInMonth = when (month) {
                2 -> 29 // 간단히 29로 처리 (윤년 체크 필요 시 추가)
                4, 6, 9, 11 -> 30
                else -> 31
            }
            if (day > maxDayInMonth) return false
            return true
        } catch (e: Exception) {
            return false
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        if (password.length < 8) return false
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { "!@#\$%^&*()_+=-".contains(it) }
        return hasLetter && hasDigit && hasSpecial
    }

    private fun navigateToLogin() {
        val intent = Intent(this, EmailLoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        passwordValidationRunnable?.let { handler.removeCallbacks(it) }
    }
}