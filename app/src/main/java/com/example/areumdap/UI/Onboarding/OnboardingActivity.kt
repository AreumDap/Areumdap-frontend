//package com.example.areumdap.UI.Onboarding
//
//import android.graphics.Color
//import android.os.Bundle
//import androidx.activity.viewModels
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.content.ContextCompat
//import com.example.areumdap.R
//import com.example.areumdap.UI.Onboarding.fragment.OnboardingCustomKeywordFragment
//import com.example.areumdap.UI.Onboarding.fragment.OnboardingKeywordFragment
//import com.example.areumdap.UI.Onboarding.fragment.OnboardingNicknameFragment
//import com.example.areumdap.UI.Onboarding.fragment.OnboardingResultFragment
//import com.example.areumdap.UI.Onboarding.fragment.OnboardingSeasonFragment
//import com.example.areumdap.UI.Onboarding.fragment.OnboardingStartFragment
//import com.example.areumdap.databinding.ActivityOnboardingBinding
//
//class OnboardingActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityOnboardingBinding
//
//    private val viewModel: OnboardingViewModel by viewModels()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityOnboardingBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//
//        if (savedInstanceState == null) {
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.fcv_onboarding, OnboardingStartFragment()) // fcv_onboarding은 XML의 컨테이너 ID
//                .commit()
//        }
//
//        setupObserve()
//
//        binding.btnNext.setOnClickListener {
//            val current = viewModel.currentStep.value ?: 0
//
//            // 0단계(시작 화면)이고 아직 텍스트가 바뀌지 않은 경우
//            if (current == 0 && !viewModel.isTextUpdated) {
//                // 현재 fcv_onboarding에 떠 있는 프래그먼트를 찾아 함수 호출
//                val startFragment = supportFragmentManager.findFragmentById(R.id.fcv_onboarding) as? OnboardingStartFragment
//                startFragment?.changeText()
//
//                viewModel.isTextUpdated = true
//                binding.btnNext.text = "나는..." // 버튼 텍스트 변경
//                return@setOnClickListener // 화면 전환 없이 종료
//            } else if (current == 1 && viewModel.selectedSeason.value!=null) {
//                binding.btnNext.text = "다음으로" // 버튼 텍스트 변경
//            }
//
//
//                // 이미 텍스트가 바뀌었거나 다음 단계인 경우 화면 전환
//            val nextStep = current + 1
//            val nextFragment: androidx.fragment.app.Fragment? = when (nextStep) {
//                1 -> OnboardingSeasonFragment()
//                2 -> OnboardingKeywordFragment()
//                3 -> OnboardingCustomKeywordFragment()
//                4 -> OnboardingResultFragment()
//                5 -> OnboardingNicknameFragment()
//                else -> null
//            }
//
//            if (nextFragment != null) {
//                supportFragmentManager.beginTransaction()
//                    .replace(R.id.fcv_onboarding, nextFragment)
//                    .addToBackStack(null)
//                    .commit()
//
//                viewModel.currentStep.value = nextStep
//                updateProgress(nextStep) // 1/5 등으로 업데이트
//
//                // 화면이 넘어갔으므로 버튼 다시 비활성화 (선택 유도)
//                viewModel.isKeywordSelected.value = false
//            }
//        }    }
//
//    // 진행바 업데이트 함수
//    fun updateProgress(step: Int) {
//        binding.pbOnboarding.progress = step
//        binding.tvProgressNum.text = "$step/5"
//    }
//
//    private fun setupObserve() {
//        // 뷰모델의 선택 상태를 관찰하여 버튼 활성화 제어
//        viewModel.isKeywordSelected.observe(this) { isEnabled ->
//            binding.btnNext.apply {
//                this.isEnabled = isEnabled
//                // 활성화 상태에 따라 색상 변경
//                if (isEnabled) {
//                    setBackgroundColor(ContextCompat.getColor(context, R.color.pink1))
//                } else {
//                    binding.btnNext.setBackgroundColor(ContextCompat.getColor(context, R.color.pink2))
//                }
//            }
//        }
//    }
//}

package com.example.areumdap.UI.Onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.areumdap.R
import com.example.areumdap.UI.MainActivity
import com.example.areumdap.UI.Onboarding.fragment.*
import com.example.areumdap.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private val viewModel: OnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 앱 켜지면 무조건 '시작 화면' & 초기화
        if (savedInstanceState == null) {
            // 태그("0")를 붙여서 시작 화면 배치
            replaceFragment(OnboardingStartFragment(), "0")
            viewModel.currentStep.value = 0
            updateUI(0)
            binding.btnNext.text = "시작할게요"
        }

        // 2. 뒤로가기 버튼 처리 (시스템 뒤로가기 가로채기)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentStep = viewModel.currentStep.value ?: 0

                // [특수 상황] 0단계이고, 텍스트가 바뀐 상태라면? -> 원상복구
                if (currentStep == 0 && viewModel.isTextUpdated) {
                    val startFragment =
                        supportFragmentManager.findFragmentById(R.id.fcv_onboarding) as? OnboardingStartFragment
                    startFragment?.resetText() // 프래그먼트에 만들어둔 초기화 함수 호출

                    viewModel.isTextUpdated = false
                    updateUI(0) // 버튼 글씨 "시작할게요"로 복구
                    return // 여기서 종료 (앱 종료 안 함)
                }

                // 그 외 상황: 일반적인 뒤로가기 동작
                if (supportFragmentManager.backStackEntryCount > 1) { // 1보다 클 때만 pop (0단계는 유지)
                    supportFragmentManager.popBackStack()
                } else {
                    finish() // 더 이상 뒤로 갈 곳 없으면 앱 종료
                }
            }
        })

        // ✨ [핵심] 뒤로가기 감지 리스너 (이게 있어야 뒤로 갔을 때 진행바가 고쳐집니다!)
        supportFragmentManager.addOnBackStackChangedListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fcv_onboarding)
            // 프래그먼트에 붙여둔 꼬리표(Tag)를 읽어서 현재 단계를 알아냄
            val step = currentFragment?.tag?.toIntOrNull() ?: 0

            viewModel.currentStep.value = step
            updateUI(step)
        }

        setupObserve() // 뷰모델 관찰

        // 2. 버튼 클릭 이벤트
        binding.btnNext.setOnClickListener {
            val currentStep = viewModel.currentStep.value ?: 0

            // [Step 0] 시작 화면 특수 로직 ("시작할게요" -> "나는...")
            if (currentStep == 0) {
                if (!viewModel.isTextUpdated) {
                    val fragment =
                        supportFragmentManager.findFragmentById(R.id.fcv_onboarding) as? OnboardingStartFragment
                    fragment?.changeText()
                    viewModel.isTextUpdated = true
                    binding.btnNext.text = "나는..."
                    return@setOnClickListener // 이동 안 하고 멈춤
                }
            }

            // [다음 단계로 이동]
            goToNextStep(currentStep)
        }
    }

    // 다음 단계가 무엇인지 판단하고 이동하는 함수
    private fun goToNextStep(currentStep: Int) {
        var nextFragment: Fragment? = null
        var nextStepNum = currentStep + 1

        when (currentStep) {
            0 -> { // 시작 -> 계절 (Step 1)
                nextFragment = OnboardingSeasonFragment()
                nextStepNum = 1
            }

            1 -> { // 계절 -> 키워드 (Step 2)
                if (viewModel.selectedSeason.value != null) {
                    nextFragment = OnboardingKeywordFragment()
                    nextStepNum = 2
                }
            }

            2 -> { // 키워드 -> (직접 입력 OR 결과)
                if (viewModel.isDirectInput.value == true) {
                    // A코스: 직접 입력 -> 커스텀 화면 (Step 3)
                    nextFragment = OnboardingCustomKeywordFragment()
                    nextStepNum = 3
                } else {
                    // B코스: 그냥 선택 -> 결과 화면 (Step 3 건너뛰고 Step 4로 점프!)
                    nextFragment = OnboardingResultFragment() // 혹은 InfoFragment
                    nextStepNum = 4
                }
            }

            3 -> { // 커스텀 -> 결과 (Step 4)
                nextFragment = OnboardingResultFragment()
                nextStepNum = 4
            }

            4 -> { // 결과 -> 닉네임 (Step 5)
                nextFragment = OnboardingNicknameFragment()
                nextStepNum = 5
            }

            5 -> { // 닉네임 -> 메인 (끝!)
                // 닉네임 입력 확인 (비어있지 않을 때만)
                if (!viewModel.nickname.value.isNullOrEmpty()) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                return
            }
        }

        // 실제로 화면 교체
        if (nextFragment != null) {
            // 이동할 때 step 숫자를 Tag(꼬리표)로 같이 보냄
            replaceFragment(nextFragment, nextStepNum.toString())

            // 화면 넘어갔으니 버튼 선택 상태 초기화 (다음 화면의 선택을 위해)
            if (nextStepNum == 1 || nextStepNum == 2 || nextStepNum == 5) {
                viewModel.isKeywordSelected.value = false
            }
        }
    }

    // [헬퍼] 프래그먼트 교체 코드 (Tag 저장 기능 추가)
    private fun replaceFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fcv_onboarding, fragment, tag) // Tag 붙이기
            .addToBackStack(tag) // 백스택에도 Tag 붙이기
            .commit()
    }

    // [헬퍼] UI 업데이트 (진행바, 버튼 글씨, 색상)
    private fun updateUI(step: Int) {
        // 1. 진행바 설정 (화면 번호에 맞춰서 사용자에게 보여줄 단계 설정)
        val displayStep = when (step) {
            0 -> 0
            1 -> 1 // 계절
            2 -> 2 // 키워드
            3 -> 3 // 결과1
            4 -> 4 // 결과2
            5 -> 4 // 닉네임
            else -> 5
        }
        binding.pbOnboarding.progress = displayStep
        binding.tvProgressNum.text = "$displayStep/5"

        // 2. 버튼 글씨 설정
        when (step) {
            0 -> binding.btnNext.text = if (viewModel.isTextUpdated) "나는..." else "시작할게요"
            7 -> binding.btnNext.text = "여정 시작하기"
            else -> binding.btnNext.text = "다음으로"
        }

// 3. ✨ [수정] 버튼 활성화 상태 강제 동기화 (여기가 핵심!)
        if (step == 0 || step == 4) {
            // 무조건 활성화되는 단계 (시작, 결과)
            binding.btnNext.isEnabled = true
            binding.btnNext.setBackgroundColor(ContextCompat.getColor(this, R.color.pink1))
        } else {
            // 선택이 필요한 단계 (계절:1, 키워드:2,  닉네임:5)
            // -> 뷰모델의 현재 값을 확인해서 버튼 색깔을 맞춤
            val isSelected = viewModel.isKeywordSelected.value ?: false
            binding.btnNext.isEnabled = isSelected

            val color = if (isSelected) R.color.pink1 else R.color.pink2
            binding.btnNext.setBackgroundColor(ContextCompat.getColor(this, color))
        }
    }

    private fun setupObserve() {
        viewModel.isKeywordSelected.observe(this) { isEnabled ->
            val step = viewModel.currentStep.value ?: 0

            // 계절(1), 키워드(2), 닉네임(5) 단계일 때만 뷰모델의 말을 듣습니다.
            if (step == 1 || step == 2 || step == 3 || step == 5) {
                binding.btnNext.isEnabled = isEnabled
                val color = if (isEnabled) R.color.pink1 else R.color.pink2
                binding.btnNext.setBackgroundColor(ContextCompat.getColor(this, color))
            }

            viewModel.isDirectInput.observe(this) { isDirectInput ->
                val currentStep = viewModel.currentStep.value ?: 0

                if (currentStep == 2 && isDirectInput) {
                    replaceFragment(OnboardingCustomKeywordFragment(), "3")
                    viewModel.currentStep.value = 3
                    updateUI(2)
                    viewModel.isKeywordSelected.value = false
                } else if (currentStep == 2 && !isDirectInput) { }
            }
        }
    }
}