package com.example.areumdap.UI.auth

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.areumdap.R
import com.example.areumdap.UI.Archive.ArchiveFragment
import com.example.areumdap.UI.Character.CharacterFragment
import com.example.areumdap.UI.Home.HomeFragment
import com.example.areumdap.UI.record.RecordFragment
import com.example.areumdap.databinding.ActivityMainBinding
import androidx.lifecycle.lifecycleScope
import com.example.areumdap.data.repository.UserRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private val toolbarLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
            updateToolbarForFragment(f)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applySeasonTheme()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setToolbar(false)
        supportFragmentManager.registerFragmentLifecycleCallbacks(toolbarLifecycleCallbacks, true)

        // =========================================================
        // [추가된 부분] 로그인 화면에서 전달된 환영 메시지 확인 및 표시
        // =========================================================
        val toastMessage = intent.getStringExtra("TOAST_MESSAGE")
        if (toastMessage != null) {
            val toast = ToastDialogFragment(toastMessage, R.drawable.ic_success)
            toast.show(supportFragmentManager, "WelcomeToast")
        }

        initBottomNavigation()
        checkFcmToken()
        askNotificationPermission()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // Granted
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun checkFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("MainActivity", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("MainActivity", "FCM Token: $token")

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    UserRepository.updateFcmToken(token)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun initBottomNavigation(){
        setToolbar(false)
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frm, HomeFragment())
            .commitAllowingStateLoss()

        binding.mainBnv.setOnItemSelectedListener { item ->
            when (item.itemId){
                R.id.archiveFragment -> {
                    setToolbar(false)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, ArchiveFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }
                R.id.homeFragment -> {
                    setToolbar(false)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, HomeFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

                R.id.settingFragment -> {
                    setToolbar(false)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm,SettingFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

                R.id.recordFragment -> {
                    setToolbar(false)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, RecordFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

                R.id.characterFragment -> {
                    setToolbar(false)
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
            "SPRING" -> setTheme(R.style.Theme_AreumDap_Spring)
            "SUMMER" -> setTheme(R.style.Theme_AreumDap_Summer)
            "FALL" -> setTheme(R.style.Theme_AreumDap_Fall)
            "WINTER" -> setTheme(R.style.Theme_AreumDap_Winter)
        }
    }

    fun setToolbar(
        visible: Boolean,
        title: String = "",
        showBackButton: Boolean = false,
        subText: String? = null,
        onBackClick:(()->Unit)? = null,
        backgroundColor: Int = Color.WHITE
    ) {
        binding.characterToolBar.root.visibility = if (visible) View.VISIBLE else View.GONE

        if (visible) {
            binding.characterToolBar.root.setBackgroundColor(backgroundColor)
            binding.characterToolBar.tvTitle.text = title
            binding.characterToolBar.ivBack.visibility = if (showBackButton) View.VISIBLE else View.GONE
            binding.characterToolBar.tvSub.visibility = if (subText != null) View.VISIBLE else View.GONE
            binding.characterToolBar.tvSub.text = subText ?: ""

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

    fun goToCharacterFragment() {
        binding.mainBnv.selectedItemId = R.id.characterFragment
    }

    private fun updateToolbarForFragment(fragment: Fragment?) {
        when (fragment) {
            is HomeFragment,
            is RecordFragment,
            is ArchiveFragment,
            is CharacterFragment,
            is SettingFragment -> setToolbar(false)
            else -> Unit
        }
    }
}
