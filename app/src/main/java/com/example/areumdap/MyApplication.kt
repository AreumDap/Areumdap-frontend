package com.example.areumdap

import android.app.Application
import com.example.areumdap.Network.TokenManager

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // TokenManager 초기화
        TokenManager.init(this)
    }
}