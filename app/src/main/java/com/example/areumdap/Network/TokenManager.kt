package com.example.areumdap.Network

import android.content.Context
import android.content.SharedPreferences

/**
 * JWT 토큰 저장 및 관리 클래스
 */
object TokenManager {
    private const val PREF_NAME = "auth_token"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_SOCIAL_PROVIDER = "social_provider"
    private const val KEY_PROFILE_IMAGE = "profile_image"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun saveUserInfo(userId: Long, email: String, name: String) {
        prefs.edit()
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, name)
            .apply()
    }

    fun getUserId(): Long {
        return prefs.getLong(KEY_USER_ID, -1)
    }

    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }

    /**
     * 소셜 로그인 정보 저장
     */
    fun saveSocialLoginInfo(provider: String, profileImage: String? = null) {
        prefs.edit()
            .putString(KEY_SOCIAL_PROVIDER, provider)
            .putString(KEY_PROFILE_IMAGE, profileImage)
            .apply()
    }

    fun getSocialProvider(): String? {
        return prefs.getString(KEY_SOCIAL_PROVIDER, null)
    }

    fun getProfileImage(): String? {
        return prefs.getString(KEY_PROFILE_IMAGE, null)
    }

    fun isSocialLogin(): Boolean {
        val provider = getSocialProvider()
        return provider == "kakao" || provider == "naver"
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}