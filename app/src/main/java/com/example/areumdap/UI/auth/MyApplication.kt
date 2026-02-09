package com.example.areumdap.UI.auth

import android.app.Application
import com.example.areumdap.data.source.TokenManager

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // TokenManager 초기화
        TokenManager.init(this)
    }
}