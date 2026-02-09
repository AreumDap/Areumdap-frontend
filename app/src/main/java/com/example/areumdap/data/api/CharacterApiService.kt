package com.example.areumdap.data.api

import com.example.areumdap.data.model.CharacterCreateRequest
import com.example.areumdap.data.model.CharacterCreateResponse
import com.example.areumdap.data.model.CharacterHistoryResponse
import com.example.areumdap.data.model.CharacterLevelUpResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CharacterApiService {
    @GET("api/characters/history")
    suspend fun getCharacterHistory(): Response<BaseResponse<CharacterHistoryResponse>>

    @POST("api/characters/level")
    suspend fun postCharacterLevel() : Response<BaseResponse<CharacterLevelUpResponse>>

    @GET("api/characters/me")
    suspend fun getMycharacter(): Response<BaseResponse<CharacterLevelUpResponse>>

    @POST("api/characters/history/summary")
    suspend fun postCharacterHistorySummary(): Response<BaseResponse<String>>

    @POST("api/characters")
    suspend fun createCharacter(
        @Body request: CharacterCreateRequest
    ): Response<BaseResponse<CharacterCreateResponse>>
}