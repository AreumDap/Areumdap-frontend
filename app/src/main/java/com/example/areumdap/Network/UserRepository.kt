package com.example.areumdap.Network

import android.util.Log

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
                // 로컬에도 저장
                val userId = TokenManager.getUserId()
                val email = TokenManager.getUserEmail() ?: ""
                TokenManager.saveUserInfo(userId, email, nickname)

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
            val response = userApi.registerDevice(
                DeviceTokenRequest(
                    deviceToken = token,
                    osType = "ANDROID"
                )
            )

            if (response.isSuccessful) {
                Log.d(TAG, "FCM 기기 등록 성공")
                Result.success(Unit)
            } else {
                Log.e(TAG, "FCM 기기 등록 실패: ${response.code()}")
                Result.failure(Exception("기기 등록 실패"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "FCM 기기 등록 에러: ${e.message}")
            Result.failure(e)
        }
    }
}