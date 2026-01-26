package com.example.areumdap.login

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.areumdap.R

class SignupActivity : AppCompatActivity() {

    // 이메일 인증 여부 확인 플래그
    private var isEmailVerified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val etName = findViewById<EditText>(R.id.etName)
        val etBirth = findViewById<EditText>(R.id.etBirth)
        val etEmail = findViewById<EditText>(R.id.etEmailSignup)
        val btnEmailAuth = findViewById<Button>(R.id.btnEmailAuth)
        val etAuthCode = findViewById<EditText>(R.id.etAuthCode)
        val btnAuthConfirm = findViewById<Button>(R.id.btnAuthConfirm)
        val etPw = findViewById<EditText>(R.id.etPwSignup)
        val etPwConfirm = findViewById<EditText>(R.id.etPwConfirmSignup)
        val btnSignupComplete = findViewById<Button>(R.id.btnSignupComplete)

        // 뒤로가기
        btnBack.setOnClickListener {
            finish()
        }

        // 이메일 인증 요청 (기획서 3-1, 4번)
        btnEmailAuth.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                // TODO: 서버 인증번호 발송 API 호출
                Toast.makeText(this, "인증번호가 발송되었습니다. (테스트용: 123456)", Toast.LENGTH_LONG).show()
            }
        }

        // 인증번호 확인 (기획서 5번)
        btnAuthConfirm.setOnClickListener {
            val code = etAuthCode.text.toString().trim()

            // TODO: 서버 인증번호 검증 API 호출
            if (code == "123456") {
                isEmailVerified = true
                Toast.makeText(this, "인증이 완료되었습니다.", Toast.LENGTH_SHORT).show()

                // 인증 완료 후 이메일 수정 방지
                etEmail.isEnabled = false
                btnEmailAuth.isEnabled = false
            } else {
                Toast.makeText(this, "인증번호가 틀렸습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 회원가입 완료 요청 (기획서 8번)
        btnSignupComplete.setOnClickListener {
            val name = etName.text.toString().trim()
            val birth = etBirth.text.toString().trim()
            val pw = etPw.text.toString().trim()
            val pwConfirm = etPwConfirm.text.toString().trim()

            // 1. 필수 입력값 검사
            if (name.isEmpty() || birth.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. 생년월일 포맷 검사
            if (birth.length != 8) {
                Toast.makeText(this, "생년월일은 8자리여야 합니다. (예: 19990101)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. 이메일 인증 선행 검사
            if (!isEmailVerified) {
                Toast.makeText(this, "이메일 인증을 진행해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 4. 비밀번호 일치 여부 검사
            if (pw != pwConfirm) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 5. 비밀번호 길이 검사 (최소 8자)
            if (pw.length < 8) {
                Toast.makeText(this, "비밀번호는 8자 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: 회원가입 서버 API 호출 (POST)
            Toast.makeText(this, "회원가입 성공! 로그인해주세요.", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}