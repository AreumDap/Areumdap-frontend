package com.example.areumdap.data.api

import com.example.areumdap.data.model.CharacterLevelUpResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {

    @POST("/api/auth/token/reissue")
    suspend fun reissueToken(
        @Header("Authorization") refreshToken: String
    ): Response<BaseResponse<TokenResponse>>

    /**
     * 이메일 로그인
     */
    @POST("/api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<BaseResponse<LoginResponse>>

    // ========================================
    // 카카오 OAuth
    // ========================================

    /**
     * 카카오 로그인 URL 조회
     */
    @GET("/api/oauth/kakao/login-uri")
    suspend fun getKakaoLoginUri(): Response<BaseResponse<OAuthLoginUrlResponse>>

    /**
     * 카카오 로그인 (인가 코드 전송)
     */
    @POST("/api/oauth/kakao/login")
    suspend fun kakaoLogin(
        @Body request: KakaoLoginRequest
    ): Response<BaseResponse<LoginResponse>>

    // ========================================
    // 네이버 OAuth
    // ========================================

    /**
     * 네이버 로그인 URL 조회
     */
    @GET("/api/oauth/naver/login-uri")
    suspend fun getNaverLoginUri(): Response<BaseResponse<OAuthLoginUrlResponse>>

    /**
     * 네이버 로그인 (인가 코드 + state 전송) - POST 방식
     */
    @POST("/api/oauth/naver/login")
    suspend fun naverLogin(
        @Body request: NaverLoginRequest
    ): Response<BaseResponse<LoginResponse>>

    // ========================================
    // 기타 인증 API
    // ========================================

    /**
     * 회원가입
     */
    @POST("/api/auth/signup")
    suspend fun signUp(
        @Body request: SignUpRequest
    ): Response<Unit>

    /**
     * 이메일 인증 코드 요청
     */
    @POST("/api/auth/email-verification")
    suspend fun sendEmailVerificationCode(
        @Body request: SendEmailVerificationCodeRequest
    ): Response<Unit>

    /**
     * 이메일 인증 코드 확인
     */
    @POST("/api/auth/email-verification/confirm")
    suspend fun confirmEmailVerificationCode(
        @Body request: ConfirmEmailVerificationCodeRequest
    ): Response<Unit>

    /**
     * 로그아웃
     */
    @POST("/api/auth/logout")
    suspend fun logout(): Response<Unit>

    /**
     * 회원탈퇴
     */
    @DELETE("/api/auth/withdraw")
    suspend fun withdraw(): Response<BaseResponse<Unit>>

    /**
     * 내 캐릭터 조회
     */
    @GET("/api/characters/me")
    suspend fun getMyCharacterInfo(): Response<BaseResponse<CharacterLevelUpResponse>>
}