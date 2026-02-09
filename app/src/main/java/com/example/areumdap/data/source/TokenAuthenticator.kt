package com.example.areumdap.data.source

import com.example.areumdap.data.api.TokenResponse
import com.example.areumdap.data.api.AuthApi
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * 401 에러 시 자동으로 토큰 재발급
 */
class TokenAuthenticator : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {

        // 이미 2번 이상 재시도했으면 중단 (무한 루프 방지)
        if (responseCount(response) >= 2) {
            TokenManager.clearAll()
            return null
        }

        val refreshToken = TokenManager.getRefreshToken() ?: return null

        // 동기적으로 토큰 재발급
        return try {
            val newTokens = runBlocking {
                reissueTokenSync(refreshToken)
            }

            if (newTokens != null) {
                // 새 토큰 저장
                TokenManager.saveTokens(
                    newTokens.accessToken,
                    newTokens.refreshToken
                )

                // 실패했던 요청을 새 토큰으로 재시도
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${newTokens.accessToken}")
                    .build()
            } else {
                // 재발급 실패 → 로그아웃
                TokenManager.clearAll()
                null
            }

        } catch (e: Exception) {
            TokenManager.clearAll()
            null
        }
    }

    private fun responseCount(response: Response?): Int {
        var count = 1
        var current = response?.priorResponse
        while (current != null) {
            count++
            current = current.priorResponse
        }
        return count
    }

    /**
     * 토큰 재발급 API 호출 (별도 Retrofit 인스턴스 사용)
     */
    private suspend fun reissueTokenSync(refreshToken: String): TokenResponse? {
        return try {
            // 순환 참조 방지를 위해 별도 Retrofit 인스턴스 생성
            val retrofit = Retrofit.Builder()
                .baseUrl("https://areum-dap.online/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val authApi = retrofit.create(AuthApi::class.java)
            val response = authApi.reissueToken("Bearer $refreshToken")

            if (response.isSuccessful && response.body()?.isSuccess == true) {
                response.body()?.data
            } else {
                null
            }

        } catch (e: Exception) {
            null
        }
    }
}