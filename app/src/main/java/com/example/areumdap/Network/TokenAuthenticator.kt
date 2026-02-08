package com.example.areumdap.Network

import android.util.Log
import com.example.areumdap.Network.model.TokenResponse
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

    companion object {
        // 동시성 처리를 위한 Lock 객체
        private val lock = Any()
        private const val BASE_URL = "https://areum-dap.online/"
        private const val TAG = "TokenAuthenticator"
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        synchronized(lock) {
            // 1. 이미 2번 이상 재시도했으면 중단 (무한 루프 방지)
            if (responseCount(response) >= 2) {
                Log.w(TAG, "재시도 횟수 초과. 로그아웃 처리.")
                TokenManager.clearAll()
                return null
            }

            // 2. 이미 다른 스레드가 토큰을 갱신했는지 확인 (Critical Section 진입 후 확인)
            val currentAccessToken = TokenManager.getAccessToken()
            val requestAccessToken = response.request.header("Authorization")?.removePrefix("Bearer ")

            // 저장된 토큰이 요청 보낼 때의 토큰과 다르다면, 이미 갱신된 것임 -> 갱신된 토큰으로 즉시 재요청
            if (currentAccessToken != null && currentAccessToken != requestAccessToken) {
                Log.d(TAG, "이미 토큰이 갱신되었습니다. 새 토큰으로 재요청합니다.")
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentAccessToken")
                    .build()
            }

            // 3. 리프레시 토큰 가져오기
            val refreshToken = TokenManager.getRefreshToken()
            if (refreshToken == null) {
                Log.e(TAG, "리프레시 토큰이 없습니다. 재발급 불가.")
                return null
            }

            // 4. 토큰 갱신 시도
            return try {
                val newTokens = runBlocking {
                    reissueTokenSync(refreshToken)
                }

                if (newTokens != null) {
                    Log.i(TAG, "토큰 재발급 성공")
                    // 새 토큰 저장
                    TokenManager.saveTokens(newTokens.accessToken, newTokens.refreshToken)

                    // 실패했던 요청을 새 토큰으로 재시도
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${newTokens.accessToken}")
                        .build()
                } else {
                    Log.e(TAG, "토큰 재발급 실패 (서버 응답 오류). 로그아웃 처리.")
                    TokenManager.clearAll()
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "토큰 재발급 중 예외 발생: ${e.message}")
                TokenManager.clearAll()
                null
            }
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
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val authApi = retrofit.create(AuthApi::class.java)
            val response = authApi.reissueToken("Bearer $refreshToken")

            if (response.isSuccessful && response.body()?.isSuccess == true) {
                response.body()?.data
            } else {
                Log.e(TAG, "재발급 API 실패: Code=${response.code()}")
                null
            }

        } catch (e: Exception) {
            Log.e(TAG, "재발급 API 네트워크 오류: ${e.message}")
            null
        }
    }
}