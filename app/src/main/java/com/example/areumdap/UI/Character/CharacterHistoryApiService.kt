package com.example.areumdap.UI.Character

import com.example.areumdap.UI.Character.Data.CharacterHistoryResponse
import retrofit2.Response
import retrofit2.http.GET

interface CharacterHistoryApiService {
    @GET("api/characters/history")
    suspend fun getCharacterHistory(): Response<CharacterHistoryResponse>
}