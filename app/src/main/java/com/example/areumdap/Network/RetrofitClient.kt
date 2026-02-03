package com.example.areumdap.Network

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

    private const val BASE_URL = "https://areum-dap.online/"

    /**
     * 인증 인터셉터 - 모든 요청에 AccessToken 자동 첨부
     */
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()

        // 토큰이 필요없는 요청들
        val noAuthPaths = listOf(
            "/api/auth/login",
            "/api/auth/signup",
            "/api/auth/email-verification",
            "/api/oauth/kakao/login-uri",  // 카카오 URL 조회
            "/api/oauth/kakao/login",       // 카카오 로그인
            "/api/oauth/naver/login-uri",  // 네이버 URL 조회
            "/api/oauth/naver/login"       // 네이버 로그인
        )

        val path = originalRequest.url.encodedPath

        if (noAuthPaths.any { path.contains(it) }) {
            return@Interceptor chain.proceed(originalRequest)
        }

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

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun <T> create(service: Class<T>): T {
        return retrofit.create(service)
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val service: CharacterApiService by lazy {
        retrofit.create(CharacterApiService::class.java)
    }

    val taskService: TaskApiService = retrofit.create(TaskApiService::class.java)
}