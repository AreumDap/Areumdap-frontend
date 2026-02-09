package com.example.areumdap.data.repository

import android.util.Log
import com.example.areumdap.data.api.KakaoLoginRequest
import com.example.areumdap.data.api.LoginResponse
import com.example.areumdap.data.api.NaverLoginRequest
import com.example.areumdap.data.api.AuthApi
import com.example.areumdap.data.source.RetrofitClient
import com.example.areumdap.data.source.TokenManager

/**
 * 소셜 로그인 Repository (웹뷰 방식)
 */
object SocialAuthRepository {

    private const val TAG = "SocialAuthRepository"

    private val authApi: AuthApi by lazy {
        RetrofitClient.create(AuthApi::class.java)
    }

    // ========================================
    // 카카오 로그인
    // ========================================

    /**
     * 카카오 로그인 URL 조회
     */
    suspend fun getKakaoLoginUrl(): Result<String> {
        return try {
            val response = authApi.getKakaoLoginUri()

            if (response.isSuccessful && response.body() != null) {
                val baseResponse = response.body()!!

                if (baseResponse.isSuccess && baseResponse.data != null) {
                    Log.d(TAG, "카카오 로그인 URL 조회 성공: ${baseResponse.data.loginUrl}")
                    Result.success(baseResponse.data.loginUrl)
                } else {
                    Result.failure(Exception(baseResponse.message))
                }
            } else {
                Result.failure(Exception("카카오 로그인 URL 조회 실패 (코드: ${response.code()})"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "카카오 로그인 URL 조회 에러: ${e.message}")
            Result.failure(Exception("서버 연결에 실패했습니다."))
        }
    }

    /**
     * 카카오 로그인 (인가 코드로 JWT 발급)
     */
    suspend fun loginWithKakaoCode(code: String): Result<LoginResponse> {
        return try {
            val response = authApi.kakaoLogin(KakaoLoginRequest(code))

            if (response.isSuccessful && response.body() != null) {
                val baseResponse = response.body()!!

                if (baseResponse.isSuccess && baseResponse.data != null) {
                    val loginData = baseResponse.data

                    // 토큰 및 사용자 정보 저장
                    TokenManager.saveTokens(loginData.accessToken, loginData.refreshToken)
                    TokenManager.saveUserInfo(
                        loginData.userId,
                        loginData.email ?: "",
                        loginData.name ?: ""
                    )
                    TokenManager.saveSocialLoginInfo("kakao")

                    Log.d(TAG, "카카오 로그인 성공: ${loginData.name}")
                    Result.success(loginData)
                } else {
                    Result.failure(Exception(baseResponse.message))
                }
            } else {
                val errorMessage = when (response.code()) {
                    409 -> "이미 해당 이메일로 가입된 계정이 있습니다."
                    else -> "카카오 로그인에 실패했습니다. (코드: ${response.code()})"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "카카오 로그인 에러: ${e.message}")
            Result.failure(Exception("서버 연결에 실패했습니다."))
        }
    }

    // ========================================
    // 네이버 로그인
    // ========================================

    /**
     * 네이버 로그인 URL 조회
     */
    suspend fun getNaverLoginUrl(): Result<String> {
        return try {
            val response = authApi.getNaverLoginUri()

            if (response.isSuccessful && response.body() != null) {
                val baseResponse = response.body()!!

                if (baseResponse.isSuccess && baseResponse.data != null) {
                    Log.d(TAG, "네이버 로그인 URL 조회 성공: ${baseResponse.data.loginUrl}")
                    Result.success(baseResponse.data.loginUrl)
                } else {
                    Result.failure(Exception(baseResponse.message))
                }
            } else {
                Result.failure(Exception("네이버 로그인 URL 조회 실패 (코드: ${response.code()})"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "네이버 로그인 URL 조회 에러: ${e.message}")
            Result.failure(Exception("서버 연결에 실패했습니다."))
        }
    }

    /**
     * 네이버 로그인 (인가 코드 + state로 JWT 발급) - POST 방식
     */
    suspend fun loginWithNaverCode(code: String, state: String): Result<LoginResponse> {
        return try {
            val response = authApi.naverLogin(NaverLoginRequest(code, state))

            if (response.isSuccessful && response.body() != null) {
                val baseResponse = response.body()!!

                if (baseResponse.isSuccess && baseResponse.data != null) {
                    val loginData = baseResponse.data

                    // 토큰 및 사용자 정보 저장
                    TokenManager.saveTokens(loginData.accessToken, loginData.refreshToken)
                    TokenManager.saveUserInfo(
                        loginData.userId,
                        loginData.email ?: "",
                        loginData.name ?: ""
                    )
                    TokenManager.saveSocialLoginInfo("naver")

                    Log.d(TAG, "네이버 로그인 성공: ${loginData.name}")
                    Result.success(loginData)
                } else {
                    Result.failure(Exception(baseResponse.message))
                }
            } else {
                val errorMessage = when (response.code()) {
                    409 -> "이미 해당 이메일로 가입된 계정이 있습니다."
                    else -> "네이버 로그인에 실패했습니다. (코드: ${response.code()})"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "네이버 로그인 에러: ${e.message}")
            Result.failure(Exception("서버 연결에 실패했습니다."))
        }
    }

    /**
     * 로그아웃
     */
    fun logout() {
        TokenManager.clearAll()
    }
}
