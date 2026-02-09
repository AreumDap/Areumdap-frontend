package com.example.areumdap.data.api

import com.google.gson.annotations.SerializedName

/**
 * 서버의 공통 응답 형식
 */
data class BaseResponse<T>(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: T?
)

/**
 * 이메일 로그인 요청
 */
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

/**
 * 로그인 응답 (이메일/카카오/네이버 공통)
 */
data class LoginResponse(
    @SerializedName("userId") val userId: Long,
    @SerializedName("email") val email: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String
)

/**
 * OAuth 로그인 URL 응답
 */
data class OAuthLoginUrlResponse(
    @SerializedName("loginUrl") val loginUrl: String
)

/**
 * 카카오 로그인 요청 (인가 코드 전송)
 */
data class KakaoLoginRequest(
    @SerializedName("code") val code: String
)

/**
 * 네이버 로그인 요청 (인가 코드 + state 전송)
 */
data class NaverLoginRequest(
    @SerializedName("code") val code: String,
    @SerializedName("state") val state: String
)

/**
 * 회원가입 요청
 */
data class SignUpRequest(
    @SerializedName("name") val name: String,
    @SerializedName("birth") val birth: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class SendEmailVerificationCodeRequest(
    @SerializedName("email") val email: String
)

data class ConfirmEmailVerificationCodeRequest(
    @SerializedName("email") val email: String,
    @SerializedName("verificationCode") val verificationCode: String
)

data class TokenResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String
)