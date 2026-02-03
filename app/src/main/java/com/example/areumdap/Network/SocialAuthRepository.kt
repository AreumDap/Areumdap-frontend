package com.example.areumdap.Network

import android.content.Context
import android.util.Log
import com.example.areumdap.Network.model.SocialLoginRequest
import com.example.areumdap.Network.model.SocialLoginResponse

/**
 * 소셜 로그인 Repository
 * 소셜 로그인 처리 및 서버 연동
 */
object SocialAuthRepository {

    private const val TAG = "SocialAuthRepository"

    private val authApi: AuthApi by lazy {
        RetrofitClient.create(AuthApi::class.java)
    }

    /**
     * 카카오 로그인 전체 프로세스
     * 1. 카카오 SDK로 로그인
     * 2. 받은 정보를 서버로 전송
     * 3. 서버에서 JWT 발급받아 저장
     */
    suspend fun loginWithKakao(context: Context): Result<SocialLoginResponse> {
        return try {
            // 1단계: 카카오 SDK 로그인
            val socialResult = SocialLoginManager.loginWithKakao(context)

            socialResult.fold(
                onSuccess = { socialData ->
                    Log.d(TAG, "카카오 로그인 성공: ${socialData.userId}")

                    // 2단계: 서버에 소셜 로그인 요청
                    sendSocialLoginToServer(socialData.provider, socialData)
                },
                onFailure = { error ->
                    Log.e(TAG, "카카오 로그인 실패: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "카카오 로그인 예외: ${e.message}")
            Result.failure(Exception("카카오 로그인 중 오류가 발생했습니다."))
        }
    }

    /**
     * 네이버 로그인 전체 프로세스
     */
    suspend fun loginWithNaver(context: Context): Result<SocialLoginResponse> {
        return try {
            // 1단계: 네이버 SDK 로그인
            val socialResult = SocialLoginManager.loginWithNaver(context)

            socialResult.fold(
                onSuccess = { socialData ->
                    Log.d(TAG, "네이버 로그인 성공: ${socialData.userId}")

                    // 2단계: 서버에 소셜 로그인 요청
                    sendSocialLoginToServer(socialData.provider, socialData)
                },
                onFailure = { error ->
                    Log.e(TAG, "네이버 로그인 실패: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "네이버 로그인 예외: ${e.message}")
            Result.failure(Exception("네이버 로그인 중 오류가 발생했습니다."))
        }
    }

    /**
     * 서버에 소셜 로그인 정보 전송
     */
    private suspend fun sendSocialLoginToServer(
        provider: String,
        socialData: SocialLoginResult
    ): Result<SocialLoginResponse> {
        return try {
            val request = SocialLoginRequest(
                provider = provider,
                accessToken = socialData.accessToken,
                socialId = socialData.userId,
                email = socialData.email,
                nickname = socialData.nickname,
                profileImageUrl = socialData.profileImageUrl
            )

            val response = authApi.socialLogin(request)

            if (response.isSuccessful && response.body() != null) {
                val baseResponse = response.body()!!

                if (baseResponse.isSuccess && baseResponse.data != null) {
                    val loginData = baseResponse.data

                    // 토큰 및 사용자 정보 저장
                    TokenManager.saveTokens(loginData.accessToken, loginData.refreshToken)
                    TokenManager.saveUserInfo(loginData.userId, loginData.email ?: "", loginData.name)

                    // 소셜 로그인 정보 저장
                    TokenManager.saveSocialLoginInfo(provider)

                    Log.d(TAG, "서버 로그인 성공: ${loginData.name}")
                    Result.success(loginData)
                } else {
                    Result.failure(Exception(baseResponse.message))
                }
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "잘못된 요청입니다."
                    401 -> "인증에 실패했습니다."
                    500 -> "서버 오류가 발생했습니다."
                    else -> "소셜 로그인에 실패했습니다. (코드: ${response.code()})"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "서버 요청 실패: ${e.message}")
            Result.failure(Exception("서버 연결에 실패했습니다: ${e.message}"))
        }
    }

    /**
     * 소셜 로그아웃
     */
    fun logout() {
        val provider = TokenManager.getSocialProvider()

        when (provider) {
            "kakao" -> SocialLoginManager.logoutKakao { /* 완료 */ }
            "naver" -> SocialLoginManager.logoutNaver()
        }

        TokenManager.clearAll()
    }

    /**
     * 소셜 연결 끊기 (회원탈퇴)
     */
    fun unlink(callback: (Boolean) -> Unit) {
        val provider = TokenManager.getSocialProvider()

        when (provider) {
            "kakao" -> SocialLoginManager.unlinkKakao(callback)
            "naver" -> SocialLoginManager.unlinkNaver(callback)
            else -> callback(true)
        }

        TokenManager.clearAll()
    }
}