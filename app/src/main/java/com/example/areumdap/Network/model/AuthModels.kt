package com.example.areumdap.Network.model

import com.google.gson.annotations.SerializedName

/**
 * [추가] 서버의 공통 응답 형식을 처리하는 클래스 (택배 박스)
 */
data class BaseResponse<T>(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: T? // 실제 내용물
)

/**
 * 이메일 로그인 요청
 */
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

/**
 * 로그인 응답 (내용물)
 */
data class LoginResponse(
    @SerializedName("userId") val userId: Long,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String
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