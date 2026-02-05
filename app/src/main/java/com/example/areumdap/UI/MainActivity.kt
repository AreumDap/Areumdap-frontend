package com.example.areumdap.UI

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.areumdap.R
import com.example.areumdap.UI.Archive.ArchiveFragment
import com.example.areumdap.UI.Character.CharacterFragment
import com.example.areumdap.UI.Home.HomeFragment
import com.example.areumdap.UI.record.RecordFragment
import com.example.areumdap.databinding.ActivityMainBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        applySeasonTheme()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initBottomNavigation()
        checkFcmToken()
        askNotificationPermission()
    }

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: Display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
                 requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)

            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun checkFcmToken() {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                android.util.Log.w("MainActivity", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            android.util.Log.d("MainActivity", "FCM Token: $token")

            // 서버에 토큰 전송
            lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    com.example.areumdap.Network.UserRepository.updateFcmToken(token)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun initBottomNavigation(){
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frm, HomeFragment())
            .commitAllowingStateLoss()

        binding.mainBnv.setOnItemSelectedListener { item ->
            when (item.itemId){
                R.id.archiveFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, ArchiveFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }
                R.id.homeFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, HomeFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

                R.id.settingFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm,SettingFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

                R.id.recordFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, RecordFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

                R.id.characterFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, CharacterFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }
            }
            false
            }
            binding.mainBnv.selectedItemId = R.id.homeFragment
        }

    private fun applySeasonTheme() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        when (prefs.getString("SEASON", "spring")) {
            "spring" -> setTheme(R.style.Theme_AreumDap_Spring)
            "summer" -> setTheme(R.style.Theme_AreumDap_Summer)
            "autumn" -> setTheme(R.style.Theme_AreumDap_Autumn)
            "winter" -> setTheme(R.style.Theme_AreumDap_Winter)
        }
    }

    /**
     * 툴바 설정 함수
     * @param visible 툴바 노출 여부
     * @param title 툴바 타이틀 (기본값: 빈 문자열)
     * @param showBackButton 뒤로가기 버튼 노출 여부 (기본값: false)
     * @param subText 서브 텍스트 (기본값: null, null이면 숨김)
     */
    fun setToolbar(
        visible: Boolean,
        title: String = "",
        showBackButton: Boolean = false,
        subText: String? = null,
        onBackClick:(()->Unit)? = null,
        backgroundColor: Int = android.graphics.Color.WHITE
    ) {
        binding.characterToolBar.root.visibility = if (visible) View.VISIBLE else View.GONE

        if (visible) {
            binding.characterToolBar.root.setBackgroundColor(backgroundColor)
            binding.characterToolBar.tvTitle.text = title
            binding.characterToolBar.ivBack.visibility = if (showBackButton) View.VISIBLE else View.GONE
            binding.characterToolBar.tvSub.visibility = if (subText != null) View.VISIBLE else View.GONE
            binding.characterToolBar.tvSub.text = subText ?: ""

            // 뒤로가기 버튼 클릭 리스너
            binding.characterToolBar.ivBack.setOnClickListener {
                onBackClick?.invoke() ?: onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    fun setBottomNavVisibility(visible: Boolean) {
        binding.mainBnv.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun goToHome() {
        binding.mainBnv.selectedItemId = R.id.homeFragment
    }
    }