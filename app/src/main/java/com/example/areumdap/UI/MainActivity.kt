package com.example.areumdap.UI

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.areumdap.R
import com.example.areumdap.UI.Archive.ArchiveFragment
import com.example.areumdap.UI.Character.CharacterFragment
import com.example.areumdap.UI.Home.HomeFragment
import com.example.areumdap.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initBottomNavigation()
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