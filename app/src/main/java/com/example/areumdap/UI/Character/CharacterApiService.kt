package com.example.areumdap.UI.Character

import com.example.areumdap.UI.Character.Data.CharacterHistoryResponse
import com.example.areumdap.UI.Character.Data.CharacterLevelUpResponse
import retrofit2.Response
import retrofit2.http.GET

interface CharacterApiService {
    @GET("api/characters/history")
    suspend fun getCharacterHistory(): Response<CharacterHistoryResponse>

    @GET("api/characters/level")
    suspend fun postCharacterLevel() : Response<CharacterLevelUpResponse>
}