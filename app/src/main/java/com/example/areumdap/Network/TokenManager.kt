package com.example.areumdap.Network

import android.content.Context
import android.content.SharedPreferences

/**
 * JWT 토큰 저장 및 관리 클래스
 * 소셜 로그인 정보 저장 기능 추가
 */
object TokenManager {
    private const val PREF_NAME = "auth_token"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_NAME = "user_name"

    // ★★★ 소셜 로그인 관련 키 추가 ★★★
    private const val KEY_SOCIAL_PROVIDER = "social_provider"  // "kakao", "naver", "email"
    private const val KEY_SOCIAL_ID = "social_id"
    private const val KEY_PROFILE_IMAGE = "profile_image"

    private lateinit var prefs: SharedPreferences

    /**
     * 초기화 - Application 또는 Activity에서 호출
     */
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 토큰 한번에 저장
     */
    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    /**
     * AccessToken 가져오기
     */
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    /**
     * RefreshToken 가져오기
     */
    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    /**
     * 사용자 정보 저장
     */
    fun saveUserInfo(userId: Long, email: String, name: String) {
        prefs.edit()
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, name)
            .apply()
    }

    /**
     * 사용자 ID 가져오기
     */
    fun getUserId(): Long {
        return prefs.getLong(KEY_USER_ID, -1)
    }

    /**
     * 사용자 이름 가져오기
     */
    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    /**
     * 사용자 이메일 가져오기
     */
    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    /**
     * 로그인 여부 확인
     */
    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }

    // ========================================
    // ★★★ 소셜 로그인 관련 메서드 추가 ★★★
    // ========================================

    /**
     * 소셜 로그인 정보 저장
     * @param provider "kakao", "naver", "email"
     */
    fun saveSocialLoginInfo(provider: String, socialId: String? = null, profileImage: String? = null) {
        prefs.edit()
            .putString(KEY_SOCIAL_PROVIDER, provider)
            .putString(KEY_SOCIAL_ID, socialId)
            .putString(KEY_PROFILE_IMAGE, profileImage)
            .apply()
    }

    /**
     * 소셜 로그인 제공자 가져오기
     * @return "kakao", "naver", "email", 또는 null
     */
    fun getSocialProvider(): String? {
        return prefs.getString(KEY_SOCIAL_PROVIDER, null)
    }

    /**
     * 소셜 ID 가져오기
     */
    fun getSocialId(): String? {
        return prefs.getString(KEY_SOCIAL_ID, null)
    }

    /**
     * 프로필 이미지 URL 가져오기
     */
    fun getProfileImage(): String? {
        return prefs.getString(KEY_PROFILE_IMAGE, null)
    }

    /**
     * 소셜 로그인 여부 확인
     */
    fun isSocialLogin(): Boolean {
        val provider = getSocialProvider()
        return provider == "kakao" || provider == "naver"
    }

    /**
     * 모든 토큰 및 사용자 정보 삭제 (로그아웃 시)
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}