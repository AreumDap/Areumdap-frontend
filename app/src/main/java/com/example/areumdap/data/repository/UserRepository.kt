package com.example.areumdap.data.repository

import android.util.Log
import com.example.areumdap.data.api.DeviceTokenRequest
import com.example.areumdap.data.api.UpdateBirthRequest
import com.example.areumdap.data.api.UpdateNicknameRequest
import com.example.areumdap.data.api.UpdateNotificationRequest
import com.example.areumdap.data.api.UserApi
import com.example.areumdap.data.api.UserProfileResponse
import com.example.areumdap.data.source.RetrofitClient
import com.example.areumdap.data.source.TokenManager

/**
 * 사용자 정보 수정 Repository
 */
object UserRepository {

    private const val TAG = "UserRepository"

    private val userApi: UserApi by lazy {
        RetrofitClient.create(UserApi::class.java)
    }

    /**
     * 유저 프로필 조회
     */
    suspend fun getProfile(): Result<UserProfileResponse> {
        return try {
            val response = userApi.getProfile()

            if (response.isSuccessful && response.body() != null) {
                val baseResponse = response.body()!!

                if (baseResponse.isSuccess && baseResponse.data != null) {
                    Log.d(TAG, "프로필 조회 성공: ${baseResponse.data.name}")
                    Result.success(baseResponse.data)
                } else {
                    Result.failure(Exception(baseResponse.message))
                }
            } else {
                val errorMessage = when (response.code()) {
                    404 -> "유저가 존재하지 않습니다."
                    else -> "프로필 조회에 실패했습니다. (코드: ${response.code()})"
                }
                Log.e(TAG, "프로필 조회 실패: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "프로필 조회 에러: ${e.message}")
            Result.failure(Exception("네트워크 오류가 발생했습니다."))
        }
    }

    /**
     * 알림 설정 수정
     * @param enabled 알림 활성화 여부
     * @param time 알림 시간 ("HH:mm" 형식)
     */
    suspend fun updateNotification(enabled: Boolean, time: String): Result<Unit> {
        return try {
            val response = userApi.updateNotification(
                UpdateNotificationRequest(
                    notificationEnabled = enabled,
                    notificationTime = time
                )
            )

            if (response.isSuccessful) {
                Log.d(TAG, "알림 설정 수정 성공: enabled=$enabled, time=$time")
                Result.success(Unit)
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "요청 값이 잘못되었습니다."
                    404 -> "유저가 존재하지 않습니다."
                    else -> "알림 설정 수정에 실패했습니다. (코드: ${response.code()})"
                }
                Log.e(TAG, "알림 설정 수정 실패: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "알림 설정 수정 에러: ${e.message}")
            Result.failure(Exception("네트워크 오류가 발생했습니다."))
        }
    }

    /**
     * 닉네임 수정
     * @param nickname 새 닉네임
     */
    suspend fun updateNickname(nickname: String): Result<Unit> {
        return try {
            val response = userApi.updateNickname(
                UpdateNicknameRequest(nickname = nickname)
            )

            if (response.isSuccessful) {
                // 닉네임만 별도로 저장 (수정됨)
                TokenManager.saveNickname(nickname)

                Log.d(TAG, "닉네임 수정 성공: $nickname")
                Result.success(Unit)
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "요청 값이 잘못되었습니다."
                    404 -> "유저가 존재하지 않습니다."
                    else -> "닉네임 수정에 실패했습니다. (코드: ${response.code()})"
                }
                Log.e(TAG, "닉네임 수정 실패: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "닉네임 수정 에러: ${e.message}")
            Result.failure(Exception("네트워크 오류가 발생했습니다."))
        }
    }

    /**
     * 생년월일 수정
     * @param birth 생년월일 ("yyyy-MM-dd" 형식)
     */
    suspend fun updateBirth(birth: String): Result<Unit> {
        return try {
            val response = userApi.updateBirth(
                UpdateBirthRequest(birth = birth)
            )

            if (response.isSuccessful) {
                Log.d(TAG, "생년월일 수정 성공: $birth")
                Result.success(Unit)
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "요청 값이 잘못되었습니다."
                    404 -> "유저가 존재하지 않습니다."
                    else -> "생년월일 수정에 실패했습니다. (코드: ${response.code()})"
                }
                Log.e(TAG, "생년월일 수정 실패: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "생년월일 수정 에러: ${e.message}")
            Result.failure(Exception("네트워크 오류가 발생했습니다."))
        }
    }

    /**
     * FCM 토큰 서버에 전송 (기기 등록)
     */
    suspend fun updateFcmToken(token: String): Result<Unit> {
        return try {
            // 이미 등록된 토큰인지 확인
            val savedToken = TokenManager.getFcmToken()
            if (savedToken == token) {
                // 이미 서버에 등록된 토큰이면 요청을 보내지 않음
                Log.d(TAG, "이미 등록된 FCM 토큰입니다. 서버 전송 생략.")
                return Result.success(Unit)
            }

            val response = userApi.registerDevice(
                DeviceTokenRequest(
                    deviceToken = token,
                    osType = "ANDROID"
                )
            )

            if (response.isSuccessful) {
                Log.d(TAG, "FCM 기기 등록 성공")
                // 성공 시 토큰 저장
                TokenManager.saveFcmToken(token)
                Result.success(Unit)
            } else {
                Log.e(TAG, "FCM 기기 등록 실패: ${response.code()}")
                // 이미 등록된 토큰이라는 에러(500 등)가 나더라도, 클라이언트 입장에서는
                // '이미 등록됨'으로 간주하고 저장해서 다음부터 요청 안 보내게 처리
                if (response.code() == 500) {
                    Log.d(TAG, "서버 에러(중복 가능성) -> 로컬에 토큰 저장 처리")
                    TokenManager.saveFcmToken(token)
                    return Result.success(Unit)
                }
                Result.failure(Exception("기기 등록 실패"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "FCM 기기 등록 에러: ${e.message}")
            Result.failure(e)
        }
    }
}