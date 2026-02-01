package com.example.areumdap.Network

import android.util.Log
import com.example.areumdap.Network.model.*

object AuthRepository {

    private val authApi: AuthApi by lazy {
        RetrofitClient.create(AuthApi::class.java)
    }

    /**
     * 이메일 로그인 (수정됨)
     */
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = authApi.login(LoginRequest(email, password))

            if (response.isSuccessful && response.body() != null) {
                val baseResponse = response.body()!!

                // 서버가 보내준 성공 여부(isSuccess) 확인
                if (baseResponse.isSuccess && baseResponse.data != null) {
                    val loginData = baseResponse.data

                    // 토큰 및 사용자 정보 저장
                    TokenManager.saveTokens(loginData.accessToken, loginData.refreshToken)
                    TokenManager.saveUserInfo(loginData.userId, loginData.email, loginData.name)

                    Result.success(loginData)
                } else {
                    // HTTP 200이지만, 비즈니스 로직상 실패인 경우 (예: 비밀번호 틀림 등)
                    Result.failure(Exception(baseResponse.message))
                }
            } else {
                // HTTP 에러 처리
                val errorMessage = when (response.code()) {
                    400 -> "이메일 또는 비밀번호 형식이 올바르지 않습니다."
                    401 -> "비밀번호가 일치하지 않습니다."
                    404 -> "존재하지 않는 계정입니다."
                    else -> "로그인에 실패했습니다. (코드: ${response.code()})"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            e.printStackTrace() // 로그캣에서 에러 내용을 확인하기 위해 추가
            Result.failure(Exception("네트워크 오류가 발생했습니다: ${e.message}"))
        }
    }

    /**
     * 회원가입
     */
    suspend fun signUp(name: String, birth: String, email: String, password: String): Result<Unit> {
        return try {
            val response = authApi.signUp(SignUpRequest(name, birth, email, password))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "입력 형식이 올바르지 않습니다."
                    401 -> "이메일 인증을 완료해주세요."
                    409 -> "이미 가입된 이메일입니다."
                    else -> "회원가입에 실패했습니다."
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("네트워크 오류가 발생했습니다."))
        }
    }

    /**
     * 이메일 인증 코드 요청
     */
    suspend fun sendEmailVerificationCode(email: String): Result<Unit> {
        return try {
            val response = authApi.sendEmailVerificationCode(SendEmailVerificationCodeRequest(email))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "올바른 이메일 형식을 입력해주세요."
                    else -> "인증 코드 전송에 실패했습니다."
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("네트워크 오류가 발생했습니다."))
        }
    }

    /**
     * 이메일 인증 코드 확인
     */
    suspend fun confirmEmailVerificationCode(email: String, verificationCode: String): Result<Unit> {
        return try {
            val response = authApi.confirmEmailVerificationCode(ConfirmEmailVerificationCodeRequest(email, verificationCode))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "올바른 이메일 형식을 입력해주세요."
                    401 -> "인증 코드가 만료되었거나 일치하지 않습니다."
                    404 -> "해당 이메일로 인증 요청을 보내지 않았습니다."
                    else -> "인증 확인에 실패했습니다."
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("네트워크 오류가 발생했습니다."))
        }
    }

    /**
     * 로그아웃
     */
    suspend fun logout(): Result<Unit> {
        return try {
            authApi.logout()
            TokenManager.clearAll()
            Result.success(Unit)
        } catch (e: Exception) {
            TokenManager.clearAll()
            Result.success(Unit)
        }
    }
}