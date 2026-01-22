package com.example.areumdap.UI.onboarding

import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.areumdap.R
import com.example.areumdap.UI.onboarding.fragment.OnboardingStartFragment
import com.example.areumdap.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding

    private val viewModel: OnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObserve()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fcv_onboarding, OnboardingStartFragment()) // fcv_onboarding은 XML의 컨테이너 ID
                .commit()
        }
    }

    // 진행바 업데이트 함수
    fun updateProgress(step: Int) {
        binding.pbOnboarding.progress = step
        binding.tvProgressNum.text = "$step/5"
    }

    private fun setupObserve() {
        // 뷰모델의 선택 상태를 관찰하여 버튼 활성화 제어
        viewModel.isKeywordSelected.observe(this) { isEnabled ->
            binding.btnNext.apply {
                this.isEnabled = isEnabled
                // 활성화 상태에 따라 색상 변경
                if (isEnabled) {
                    setBackgroundColor(ContextCompat.getColor(context, R.color.main_pink))
                } else {
                    binding.btnNext.setBackgroundColor(ContextCompat.getColor(context, R.color.sub_pink))
                }
            }
        }
    }
}