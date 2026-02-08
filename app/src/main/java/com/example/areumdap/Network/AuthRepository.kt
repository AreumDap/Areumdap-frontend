package com.example.areumdap.Network

import com.example.areumdap.Network.model.*
import retrofit2.HttpException

object AuthRepository {

    private val authApi: AuthApi by lazy {
        RetrofitClient.create(AuthApi::class.java)
    }

    /**
     * 이메일 로그인
     */
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = authApi.login(LoginRequest(email, password))

            if (response.isSuccessful && response.body() != null) {
                val baseResponse = response.body()!!

                if (baseResponse.isSuccess && baseResponse.data != null) {
                    val loginData = baseResponse.data

                    // 1. 토큰 저장 (null이면 빈 문자열 처리)
                    TokenManager.saveTokens(
                        loginData.accessToken ?: "",
                        loginData.refreshToken ?: ""
                    )

                    // 2. 유저 정보 저장 [수정된 부분]
                    // userId가 Int이거나 null일 수 있으므로 Long으로 안전하게 변환합니다.
                    // 만약 userId가 null이면 0L(0)으로 저장합니다.
                    val userIdLong: Long = try {
                        loginData.userId?.toString()?.toLong() ?: 0L
                    } catch (e: Exception) {
                        0L // 변환 실패시 0 저장
                    }

                    TokenManager.saveUserInfo(
                        userIdLong,
                        loginData.email ?: "",  // null이면 빈 문자열
                        loginData.name ?: ""    // null이면 빈 문자열
                    )

                    // 3. 프로필 정보(닉네임 등) 추가 조회 및 저장
                    try {
                        // UserRepository를 통해 프로필 조회
                        val profileResult = UserRepository.getProfile()
                        profileResult.onSuccess { profile ->
                            profile.nickname?.let { 
                                TokenManager.saveNickname(it) 
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // 프로필 조회 실패해도 로그인은 성공 처리
                    }

                    Result.success(loginData)
                } else {
                    Result.failure(Exception(baseResponse.message))
                }
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "이메일 또는 비밀번호 형식이 올바르지 않습니다."
                    401 -> "비밀번호가 일치하지 않습니다."
                    404 -> "존재하지 않는 계정입니다."
                    else -> "로그인에 실패했습니다. (코드: ${response.code()})"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
     * 포기하기 (회원탈퇴)
     */
    suspend fun withdraw(): Result<Unit> {
        return try {
            val response = authApi.withdraw()
            if (response.isSuccessful) {
                // 성공 시에도 로컬 데이터 클리어
                TokenManager.clearAll()
                Result.success(Unit)
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "유저가 존재하지 않습니다."
                    else -> "탈퇴 처리에 실패했습니다. (코드: ${response.code()})"
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

    /**
     * 내 캐릭터 조회
     */
    suspend fun getMyCharacter(): Result<Any?> {
        return try {
            val response = authApi.getMyCharacterInfo()

            if (response.isSuccessful) {
                Result.success(response.body())
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}