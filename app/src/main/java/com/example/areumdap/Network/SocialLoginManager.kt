package com.example.areumdap.Network

import android.content.Context
import android.util.Log
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * 소셜 로그인 결과 데이터 클래스
 */
data class SocialLoginResult(
    val provider: String,           // "kakao" 또는 "naver"
    val accessToken: String,        // 소셜 플랫폼 액세스 토큰
    val userId: String,             // 소셜 플랫폼 사용자 ID
    val email: String?,             // 이메일 (nullable)
    val nickname: String?,          // 닉네임 (nullable)
    val profileImageUrl: String?    // 프로필 이미지 URL (nullable)
)

/**
 * 소셜 로그인 통합 관리 클래스
 */
object SocialLoginManager {

    private const val TAG = "SocialLoginManager"

    // ========================================
    // 카카오 로그인
    // ========================================

    /**
     * 카카오 로그인 실행
     * @param context Activity context
     * @return Result<SocialLoginResult>
     */
    suspend fun loginWithKakao(context: Context): Result<SocialLoginResult> {
        return suspendCoroutine { continuation ->

            // 카카오톡 설치 여부 확인
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
                // 카카오톡으로 로그인
                UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                    if (error != null) {
                        Log.e(TAG, "카카오톡 로그인 실패", error)

                        // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            continuation.resume(Result.failure(Exception("로그인이 취소되었습니다.")))
                            return@loginWithKakaoTalk
                        }

                        // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                        loginWithKakaoAccount(context) { result ->
                            continuation.resume(result)
                        }
                    } else if (token != null) {
                        Log.i(TAG, "카카오톡 로그인 성공 ${token.accessToken}")
                        getKakaoUserInfo(token.accessToken) { result ->
                            continuation.resume(result)
                        }
                    }
                }
            } else {
                // 카카오톡 미설치: 카카오계정으로 로그인
                loginWithKakaoAccount(context) { result ->
                    continuation.resume(result)
                }
            }
        }
    }

    /**
     * 카카오 계정으로 로그인 (웹뷰)
     */
    private fun loginWithKakaoAccount(
        context: Context,
        callback: (Result<SocialLoginResult>) -> Unit
    ) {
        UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
            if (error != null) {
                Log.e(TAG, "카카오계정 로그인 실패", error)
                callback(Result.failure(Exception("카카오 로그인에 실패했습니다: ${error.message}")))
            } else if (token != null) {
                Log.i(TAG, "카카오계정 로그인 성공 ${token.accessToken}")
                getKakaoUserInfo(token.accessToken, callback)
            }
        }
    }

    /**
     * 카카오 사용자 정보 가져오기
     */
    private fun getKakaoUserInfo(
        accessToken: String,
        callback: (Result<SocialLoginResult>) -> Unit
    ) {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(TAG, "카카오 사용자 정보 요청 실패", error)
                callback(Result.failure(Exception("사용자 정보를 가져올 수 없습니다.")))
            } else if (user != null) {
                Log.i(TAG, "카카오 사용자 정보 요청 성공")
                Log.i(TAG, "회원번호: ${user.id}")
                Log.i(TAG, "이메일: ${user.kakaoAccount?.email}")
                Log.i(TAG, "닉네임: ${user.kakaoAccount?.profile?.nickname}")
                Log.i(TAG, "프로필사진: ${user.kakaoAccount?.profile?.thumbnailImageUrl}")

                val result = SocialLoginResult(
                    provider = "kakao",
                    accessToken = accessToken,
                    userId = user.id.toString(),
                    email = user.kakaoAccount?.email,
                    nickname = user.kakaoAccount?.profile?.nickname,
                    profileImageUrl = user.kakaoAccount?.profile?.thumbnailImageUrl
                )
                callback(Result.success(result))
            }
        }
    }

    /**
     * 카카오 로그아웃
     */
    fun logoutKakao(callback: (Boolean) -> Unit) {
        UserApiClient.instance.logout { error ->
            if (error != null) {
                Log.e(TAG, "카카오 로그아웃 실패", error)
                callback(false)
            } else {
                Log.i(TAG, "카카오 로그아웃 성공")
                callback(true)
            }
        }
    }

    /**
     * 카카오 연결 끊기 (회원탈퇴)
     */
    fun unlinkKakao(callback: (Boolean) -> Unit) {
        UserApiClient.instance.unlink { error ->
            if (error != null) {
                Log.e(TAG, "카카오 연결 끊기 실패", error)
                callback(false)
            } else {
                Log.i(TAG, "카카오 연결 끊기 성공")
                callback(true)
            }
        }
    }

    // ========================================
    // 네이버 로그인
    // ========================================

    /**
     * 네이버 로그인 실행
     * @param context Activity context
     * @return Result<SocialLoginResult>
     */
    suspend fun loginWithNaver(context: Context): Result<SocialLoginResult> {
        return suspendCoroutine { continuation ->

            val oauthLoginCallback = object : OAuthLoginCallback {
                override fun onSuccess() {
                    Log.i(TAG, "네이버 로그인 성공")
                    val accessToken = NaverIdLoginSDK.getAccessToken()

                    if (accessToken != null) {
                        getNaverUserInfo(accessToken) { result ->
                            continuation.resume(result)
                        }
                    } else {
                        continuation.resume(Result.failure(Exception("액세스 토큰을 가져올 수 없습니다.")))
                    }
                }

                override fun onFailure(httpStatus: Int, message: String) {
                    Log.e(TAG, "네이버 로그인 실패: $httpStatus - $message")
                    val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                    val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                    continuation.resume(Result.failure(Exception("네이버 로그인 실패: $errorDescription")))
                }

                override fun onError(errorCode: Int, message: String) {
                    Log.e(TAG, "네이버 로그인 에러: $errorCode - $message")
                    onFailure(errorCode, message)
                }
            }

            NaverIdLoginSDK.authenticate(context, oauthLoginCallback)
        }
    }

    /**
     * 네이버 사용자 정보 가져오기
     */
    private fun getNaverUserInfo(
        accessToken: String,
        callback: (Result<SocialLoginResult>) -> Unit
    ) {
        NidOAuthLogin().callProfileApi(object : NidProfileCallback<NidProfileResponse> {
            override fun onSuccess(result: NidProfileResponse) {
                Log.i(TAG, "네이버 사용자 정보 요청 성공")
                val profile = result.profile

                Log.i(TAG, "회원번호: ${profile?.id}")
                Log.i(TAG, "이메일: ${profile?.email}")
                Log.i(TAG, "닉네임: ${profile?.nickname}")
                Log.i(TAG, "이름: ${profile?.name}")
                Log.i(TAG, "프로필사진: ${profile?.profileImage}")

                val socialResult = SocialLoginResult(
                    provider = "naver",
                    accessToken = accessToken,
                    userId = profile?.id ?: "",
                    email = profile?.email,
                    nickname = profile?.nickname ?: profile?.name,
                    profileImageUrl = profile?.profileImage
                )
                callback(Result.success(socialResult))
            }

            override fun onFailure(httpStatus: Int, message: String) {
                Log.e(TAG, "네이버 프로필 요청 실패: $httpStatus - $message")
                callback(Result.failure(Exception("사용자 정보를 가져올 수 없습니다.")))
            }

            override fun onError(errorCode: Int, message: String) {
                Log.e(TAG, "네이버 프로필 요청 에러: $errorCode - $message")
                onFailure(errorCode, message)
            }
        })
    }

    /**
     * 네이버 로그아웃
     */
    fun logoutNaver() {
        NaverIdLoginSDK.logout()
        Log.i(TAG, "네이버 로그아웃 성공")
    }

    /**
     * 네이버 연결 끊기 (회원탈퇴)
     */
    fun unlinkNaver(callback: (Boolean) -> Unit) {
        NidOAuthLogin().callDeleteTokenApi(object : OAuthLoginCallback {
            override fun onSuccess() {
                Log.i(TAG, "네이버 연결 끊기 성공")
                callback(true)
            }

            override fun onFailure(httpStatus: Int, message: String) {
                Log.e(TAG, "네이버 연결 끊기 실패: $message")
                callback(false)
            }

            override fun onError(errorCode: Int, message: String) {
                Log.e(TAG, "네이버 연결 끊기 에러: $message")
                callback(false)
            }
        })
    }
}