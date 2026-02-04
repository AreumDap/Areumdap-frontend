plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.areumdap"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.areumdap"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ★★★ 카카오 네이티브 앱 키 설정 (발급받은 키로 변경) ★★★
        manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] = "a21690c0113fea3822765e107e26bc1e"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    // ★★★ ViewBinding 활성화 ★★★
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // 기본 Android 라이브러리
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // 테스트
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Retrofit (네트워크)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // 코루틴
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    // ★★★ 카카오 로그인 SDK ★★★
    implementation("com.kakao.sdk:v2-user:2.20.1")

    // ★★★ 네이버 로그인 SDK ★★★
    implementation("com.navercorp.nid:oauth:5.9.1")

    implementation("com.google.android.flexbox:flexbox:3.0.0")

    // 1. viewModels(), activityViewModels() 사용을 위한 KTX 라이브러리
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.activity:activity-ktx:1.8.2")
// 2. Glide (bumptech) 라이브러리
    implementation("com.github.bumptech.glide:glide:4.16.0")
    // 2. Glide (bumptech) 라이브러리
    implementation("com.github.bumptech.glide:glide:4.16.0")
}