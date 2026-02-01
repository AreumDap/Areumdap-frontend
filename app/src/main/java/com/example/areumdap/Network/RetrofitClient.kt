package com.example.areumdap.Network

import com.example.areumdap.UI.Character.CharacterApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://areum-dap.online/"

    private val client = OkHttpClient.Builder()
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client).addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: CharacterApiService = retrofit.create(CharacterApiService::class.java)

}