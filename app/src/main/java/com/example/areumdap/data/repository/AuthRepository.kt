package com.example.areumdap.data.repository

import com.example.areumdap.data.api.AuthApi
import com.example.areumdap.data.source.RetrofitClient
import com.example.areumdap.data.source.TokenManager
import com.example.areumdap.data.repository.UserRepository
import com.example.areumdap.data.api.ConfirmEmailVerificationCodeRequest
import com.example.areumdap.data.api.LoginRequest
import com.example.areumdap.data.api.LoginResponse
import com.example.areumdap.data.api.SendEmailVerificationCodeRequest
import com.example.areumdap.data.api.SignUpRequest
import com.example.areumdap.data.model.CharacterLevelUpResponse
import retrofit2.HttpException

object AuthRepository {

    private val authApi: AuthApi by lazy {
        RetrofitClient.create(AuthApi::class.java)
    }

    /**
     * 데모 자동 로그인 (파라미터 없이 테스트 계정으로 로그인)
     */
    suspend fun testLogin(): Result<LoginResponse> {
        return try {
            val response = authApi.testLogin()

            if (response.isSuccessful && response.body() != null) {
                val baseResponse = response.body()!!

                if (baseResponse.isSuccess && baseResponse.data != null) {
                    val loginData = baseResponse.data

                    // 토큰 저장
                    TokenManager.saveTokens(
                        loginData.accessToken ?: "",
                        loginData.refreshToken ?: ""
                    )

                    // 유저 정보 저장
                    val userIdLong: Long = try {
                        loginData.userId?.toString()?.toLong() ?: 0L
                    } catch (e: Exception) {
                        0L
                    }
                    TokenManager.saveUserInfo(
                        userIdLong,
                        loginData.email ?: "",
                        loginData.name ?: ""
                    )

                    // 프로필 닉네임 조회 및 저장
                    try {
                        val profileResult = UserRepository.getProfile()
                        profileResult.onSuccess { profile ->
                            profile.nickname?.let { TokenManager.saveNickname(it) }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    Result.success(loginData)
                } else {
                    Result.failure(Exception(baseResponse.message))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
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

                    // 1. 토큰 저장
                    TokenManager.saveTokens(
                        loginData.accessToken ?: "",
                        loginData.refreshToken ?: ""
                    )

                    // 2. 유저 정보 저장
                    val userIdLong: Long = try {
                        loginData.userId?.toString()?.toLong() ?: 0L
                    } catch (e: Exception) {
                        0L
                    }

                    TokenManager.saveUserInfo(
                        userIdLong,
                        loginData.email ?: "",
                        loginData.name ?: ""
                    )

                    // 3. 프로필 정보 조회 및 저장
                    try {
                        val profileResult = UserRepository.getProfile()
                        profileResult.onSuccess { profile ->
                            profile.nickname?.let {
                                TokenManager.saveNickname(it)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    Result.success(loginData)
                } else {
                    // 서버에서 200 OK를 줬지만 isSuccess가 false인 경우
                    Result.failure(Exception(baseResponse.message))
                }
            } else {
                // ★ 중요 ★
                // 여기서 문자열로 바꾸지 않고 HttpException을 그대로 던져야
                // Activity에서 error.code() == 401을 체크할 수 있습니다.
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
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
                Result.failure(HttpException(response))
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
                Result.failure(HttpException(response))
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
            val response = authApi.confirmEmailVerificationCode(
                ConfirmEmailVerificationCodeRequest(email, verificationCode)
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(HttpException(response))
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
                TokenManager.clearAll()
                Result.success(Unit)
            } else {
                Result.failure(HttpException(response))
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
    suspend fun getMyCharacter(): Result<CharacterLevelUpResponse?> {
        return try {
            val response = authApi.getMyCharacterInfo()

            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data

                data?.imageUrl?.let { url ->
                    val season = when {
                        url.contains("spring") -> "SPRING"
                        url.contains("summer") -> "SUMMER"
                        url.contains("fall") -> "FALL"
                        url.contains("winter") -> "WINTER"
                        else -> null
                    }
                    if (season != null) {
                        TokenManager.saveSeason(season)
                    }
                }
                Result.success(data)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}