package com.example.areumdap.Network

import com.example.areumdap.Network.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    /**
     * 이메일 로그인
     * 반환 타입을 BaseResponse로 감싸주어야 합니다.
     */
    @POST("/api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<BaseResponse<LoginResponse>>

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
}