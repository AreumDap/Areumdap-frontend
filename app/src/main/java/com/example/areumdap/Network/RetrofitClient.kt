package com.example.areumdap.Network

import com.example.areumdap.Data.api.ChatbotApiService
import com.example.areumdap.Data.api.MissionApiService
import com.example.areumdap.Task.TaskApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.example.areumdap.UI.Character.CharacterApiService

/**
 * Retrofit 클라이언트 설정
 */
object RetrofitClient {

    // 서버 베이스 URL
    private const val BASE_URL = "https://areum-dap.online/"



    /**
     * 인증 인터셉터 - 모든 요청에 AccessToken 자동 첨부
     */
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()

        // 토큰이 필요없는 요청들 (소셜 로그인 추가)
        val noAuthPaths = listOf(
            "/api/auth/login",
            "/api/auth/signup",
            "/api/auth/email-verification",
            "/api/auth/social-login",  // 소셜 로그인 경로
            "/api/auth/token/reissue"  // 토큰 재발급 경로 (인증 불필요)
        )

        val path = originalRequest.url.encodedPath

        // 토큰이 필요없는 요청이면 그대로 진행
        if (noAuthPaths.any { path.contains(it) }) {
            return@Interceptor chain.proceed(originalRequest)
        }

        // AccessToken이 있으면 헤더에 추가
        val accessToken = try {
            TokenManager.getAccessToken()
        } catch (e: Exception) {
            null
        }

        val newRequest = if (accessToken != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }

        chain.proceed(newRequest)
    }

    /**
     * 로깅 인터셉터
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * OkHttpClient 설정
     */
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .authenticator(TokenAuthenticator())
        .build()

    /**
     * Retrofit 인스턴스
     */
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /**
     * API 서비스 생성 (범용)
     */
    fun <T> create(service: Class<T>): T {
        return retrofit.create(service)
    }

    /**
     * AuthApi 인스턴스 (편의용)
     */
    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    /**
     * CharacterHistoryFragment에서 사용하는 service 객체
     */
    val service: CharacterApiService by lazy {
        retrofit.create(CharacterApiService::class.java)
    }
    val taskService: TaskApiService = retrofit.create(TaskApiService::class.java)

    val missionApi: MissionApiService by lazy {
        retrofit.create(MissionApiService::class.java)
    }
    val chatbotApiService: ChatbotApiService by lazy {
        retrofit.create(ChatbotApiService::class.java)
    }

}