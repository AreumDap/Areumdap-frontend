package com.example.areumdap.UI.Character

import com.example.areumdap.UI.Character.Data.CharacterHistoryResponse
import com.example.areumdap.UI.Character.Data.CharacterLevelUpResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST

interface CharacterApiService {
    @GET("api/characters/history")
    suspend fun getCharacterHistory(): Response<CharacterHistoryResponse>

    @POST("api/characters/level")
    suspend fun postCharacterLevel() : Response<CharacterLevelUpResponse>

    @GET("api/characters/me")
    suspend fun getMycharacter(): Response<CharacterLevelUpResponse>
}