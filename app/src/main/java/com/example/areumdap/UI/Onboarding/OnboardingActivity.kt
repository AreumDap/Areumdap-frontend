package com.example.areumdap.UI.onboarding

import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.areumdap.R
import com.example.areumdap.UI.Onboarding.fragment.OnboardingCustomKeywordFragment
import com.example.areumdap.UI.onboarding.fragment.OnboardingKeywordFragment
import com.example.areumdap.UI.onboarding.fragment.OnboardingNicknameFragment
import com.example.areumdap.UI.onboarding.fragment.OnboardingResultFragment
import com.example.areumdap.UI.onboarding.fragment.OnboardingSeasonFragment
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

        binding.btnNext.setOnClickListener {
            val current = viewModel.currentStep.value ?: 0

            // 0단계(시작 화면)이고 아직 텍스트가 바뀌지 않은 경우
            if (current == 0 && !viewModel.isTextUpdated) {
                // 현재 fcv_onboarding에 떠 있는 프래그먼트를 찾아 함수 호출
                val startFragment = supportFragmentManager.findFragmentById(R.id.fcv_onboarding) as? OnboardingStartFragment
                startFragment?.changeText()

                viewModel.isTextUpdated = true
                binding.btnNext.text = "나는..." // 버튼 텍스트 변경
                return@setOnClickListener // 화면 전환 없이 종료
            } else if (current == 1 && viewModel.selectedSeason.value!=null) {
                binding.btnNext.text = "다음으로" // 버튼 텍스트 변경
            }


                // 이미 텍스트가 바뀌었거나 다음 단계인 경우 화면 전환
            val nextStep = current + 1
            val nextFragment: androidx.fragment.app.Fragment? = when (nextStep) {
                1 -> OnboardingSeasonFragment()
                2 -> OnboardingKeywordFragment()
                3 -> OnboardingCustomKeywordFragment()
                4 -> OnboardingResultFragment()
                5 -> OnboardingNicknameFragment()
                else -> null
            }

            if (nextFragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fcv_onboarding, nextFragment)
                    .addToBackStack(null)
                    .commit()

                viewModel.currentStep.value = nextStep
                updateProgress(nextStep) // 1/5 등으로 업데이트

                // 화면이 넘어갔으므로 버튼 다시 비활성화 (선택 유도)
                viewModel.isKeywordSelected.value = false
            }
        }    }

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
                    setBackgroundColor(ContextCompat.getColor(context, R.color.pink1))
                } else {
                    binding.btnNext.setBackgroundColor(ContextCompat.getColor(context, R.color.pink2))
                }
            }
        }
    }
}