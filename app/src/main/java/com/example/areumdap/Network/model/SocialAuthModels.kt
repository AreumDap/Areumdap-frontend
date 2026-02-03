package com.example.areumdap.Network.model

import com.google.gson.annotations.SerializedName

/**
 * 소셜 로그인 요청 (서버로 전송)
 * 소셜 플랫폼에서 받은 토큰을 서버로 전송하여 자체 JWT 발급
 */
data class SocialLoginRequest(
    @SerializedName("provider") val provider: String,           // "kakao" 또는 "naver"
    @SerializedName("accessToken") val accessToken: String,     // 소셜 플랫폼 액세스 토큰
    @SerializedName("socialId") val socialId: String,           // 소셜 플랫폼 사용자 ID
    @SerializedName("email") val email: String?,                // 이메일 (nullable)
    @SerializedName("nickname") val nickname: String?,          // 닉네임 (nullable)
    @SerializedName("profileImageUrl") val profileImageUrl: String?  // 프로필 이미지 (nullable)
)

/**
 * 소셜 로그인 응답 (서버에서 수신)
 * 기존 LoginResponse와 동일한 구조 사용 가능
 */
data class SocialLoginResponse(
    @SerializedName("userId") val userId: Long,
    @SerializedName("email") val email: String?,
    @SerializedName("name") val name: String,
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("isNewUser") val isNewUser: Boolean = false  // 신규 회원 여부
)