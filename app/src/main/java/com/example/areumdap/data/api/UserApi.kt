package com.example.areumdap.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface UserApi {

    /**
     * 유저 온보딩 저장
     */
    @POST("/api/users/onboarding")
    suspend fun saveOnboarding(@Body request: OnboardingRequest): Response<BaseResponse<Unit>>

    /**
     * 유저 프로필 조회
     */
    @GET("/api/users/profile")
    suspend fun getProfile(): Response<BaseResponse<UserProfileResponse>>

    /**
     * 알림 설정 수정
     */
    @PATCH("/api/users/notification")
    suspend fun updateNotification(
        @Body request: UpdateNotificationRequest
    ): Response<BaseResponse<Unit>>

    /**
     * 닉네임 수정
     */
    @PATCH("/api/users/nickname")
    suspend fun updateNickname(
        @Body request: UpdateNicknameRequest
    ): Response<BaseResponse<Unit>>

    /**
     * 생년월일 수정
     */
    @PATCH("/api/users/birth")
    suspend fun updateBirth(
        @Body request: UpdateBirthRequest
    ): Response<BaseResponse<Unit>>

    /**
     * 기기 토큰 등록
     */
    @POST("/api/device")
    suspend fun registerDevice(
        @Body request: DeviceTokenRequest
    ): Response<BaseResponse<Unit>>
}

/**
 * 기기 토큰 등록 요청
 */
data class DeviceTokenRequest(
    val deviceToken: String,
    val osType: String
)

/**
 * 알림 설정 요청
 */
data class UpdateNotificationRequest(
    val notificationEnabled: Boolean,
    val notificationTime: String  // "HH:mm" 형식
)

/**
 * 닉네임 수정 요청
 */
data class UpdateNicknameRequest(
    val nickname: String
)

/**
 * 생년월일 수정 요청
 */
data class UpdateBirthRequest(
    val birth: String  // "yyyy-MM-dd" 형식
)

/**
 * 유저 온보딩 저장 요청
 */
data class OnboardingRequest(
    @SerializedName("nickname")
    val nickname: String
)

/**
 * 유저 프로필 응답
 */
data class UserProfileResponse(
    val userId: Long,
    val name: String?,
    val birth: String?,
    val notificationEnabled: Boolean,
    val nickname: String?,
    val pushNotificationTime: String?
)