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

        initializeOnboarding(savedInstanceState)
        setupBackPressedHandler()
        setupBackStackListener()
        setupObservers()
        setupNextButtonClickListener()
    }

    private fun initializeOnboarding(savedInstanceState: Bundle?) {
        // 화면 회전 등으로 인한 재생성 시 중복 Fragment 추가 방지
        if (savedInstanceState == null) {
            replaceFragment(OnboardingStartFragment(), OnboardingStep.START.value.toString())
            viewModel.currentStep.value = OnboardingStep.START.value
            updateUI(OnboardingStep.START.value)
        }
    }

    private fun setupBackPressedHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPress()
            }
        })
    }

    private fun handleBackPress() {
        val currentStep = OnboardingStep.fromValue(viewModel.currentStep.value ?: 0) ?: OnboardingStep.START

        when (currentStep) {
            OnboardingStep.START -> handleStartStepBackPress()
            // INFO와 FINAL은 같은 Fragment를 사용하므로 함께 처리
            OnboardingStep.INFO, OnboardingStep.FINAL -> {
                if (!handleInfoStepBackPress()) {
                    performDefaultBackPress()
                }
            }
            else -> performDefaultBackPress()
        }
    }

    private fun handleStartStepBackPress() {
        // 텍스트가 변경된 상태에서만 복구 처리, 아니면 기본 뒤로가기
        if (viewModel.isTextUpdated) {
            val startFragment = supportFragmentManager.findFragmentById(R.id.fcv_onboarding) as? OnboardingStartFragment
            startFragment?.resetText()
            viewModel.isTextUpdated = false
            updateUI(OnboardingStep.START.value)
        } else {
            performDefaultBackPress()
        }
    }

    /**
     * @return true면 뒤로가기가 처리됨, false면 일반 Fragment pop 수행
     * Info 화면 내에서 텍스트 단계를 거슬러 올라가야 하므로 별도 처리
     */
    private fun handleInfoStepBackPress(): Boolean {
        val currentTextStep = InfoTextStep.fromValue(viewModel.infoTextStep.value ?: 0)

        return when (currentTextStep) {
            InfoTextStep.FINAL_MESSAGE -> {
                viewModel.infoTextStep.value = InfoTextStep.NICKNAME_INPUT.value
                viewModel.isKeywordSelected.value = true
                true
            }
            InfoTextStep.NICKNAME_INPUT -> {
                viewModel.infoTextStep.value = InfoTextStep.NICKNAME_INTRO.value
                viewModel.isKeywordSelected.value = true
                binding.btnNext.text = ButtonText.I_AM.text
                true
            }
            InfoTextStep.NICKNAME_INTRO -> {
                viewModel.infoTextStep.value = InfoTextStep.AREUM_BORN.value
                true
            }
            // 첫 번째 텍스트 단계에서는 Fragment 자체를 pop
            else -> false
        }
    }

    private fun performDefaultBackPress() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        } else {
            finish()
        }
    }

    private fun setupBackStackListener() {
        // Fragment가 pop될 때 currentStep과 UI 동기화
        supportFragmentManager.addOnBackStackChangedListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fcv_onboarding)
            val step = currentFragment?.tag?.toIntOrNull() ?: OnboardingStep.START.value

            viewModel.currentStep.value = step
            updateUI(step)
        }
    }

    private fun setupNextButtonClickListener() {
        binding.btnNext.setOnClickListener {
            val currentStep = OnboardingStep.fromValue(viewModel.currentStep.value ?: 0) ?: OnboardingStep.START

            when (currentStep) {
                OnboardingStep.START -> handleStartStepClick()
                // INFO와 FINAL은 같은 Fragment를 다른 텍스트로 재사용
                OnboardingStep.INFO, OnboardingStep.FINAL -> handleInfoStepClick(currentStep)
                else -> goToNextStep(currentStep)
            }
        }
    }

    private fun handleStartStepClick() {
        // 시작 화면은 버튼을 두 번 눌러야 다음 단계로 진행 (텍스트 변경 후 이동)
        if (!viewModel.isTextUpdated) {
            val fragment = supportFragmentManager.findFragmentById(R.id.fcv_onboarding) as? OnboardingStartFragment
            fragment?.changeText()
            viewModel.isTextUpdated = true
            binding.btnNext.text = ButtonText.I_AM.text
        } else {
            goToNextStep(OnboardingStep.START)
        }
    }

    /**
     * Info 화면은 하나의 Fragment에서 4가지 텍스트를 순차 표시하므로
     * 각 텍스트 단계마다 다른 동작 수행
     */
    private fun handleInfoStepClick(currentStep: OnboardingStep) {
        val currentTextStep = InfoTextStep.fromValue(viewModel.infoTextStep.value ?: 0)

        when (currentTextStep) {
            InfoTextStep.AREUM_BORN -> {
                // "아름이가 태어났어요" → "서로를 알아가기 위해..."
                viewModel.infoTextStep.value = InfoTextStep.NICKNAME_INTRO.value
            }
            InfoTextStep.NICKNAME_INTRO -> {
                // "서로를 알아가기 위해..." → 닉네임 입력 Fragment로 이동
                goToNextStep(currentStep)
            }
            InfoTextStep.NICKNAME_INPUT -> {
                // 닉네임 입력 후 → "아름이는 oo님이..."
                if (isNicknameValid()) {
                    viewModel.infoTextStep.value = InfoTextStep.FINAL_MESSAGE.value
                    viewModel.isKeywordSelected.value = true
                    binding.btnNext.text = ButtonText.START_JOURNEY.text
                }
            }
            InfoTextStep.FINAL_MESSAGE -> {
                // 최종 메시지 → 메인 화면으로
                if (isNicknameValid()) {
                    navigateToMain()
                }
            }
        }
    }

    private fun goToNextStep(currentStep: OnboardingStep) {
        val (nextFragment, nextStep) = getNextFragmentAndStep(currentStep) ?: return

        replaceFragment(nextFragment, nextStep.value.toString())

        // 선택이 필요한 단계로 이동 시 버튼 비활성화하여 사용자 선택 유도
        if (nextStep in listOf(OnboardingStep.SEASON, OnboardingStep.KEYWORD, OnboardingStep.NICKNAME)) {
            viewModel.isKeywordSelected.value = false
        }
    }

    /**
     * @return Pair<다음 Fragment, 다음 Step> 또는 null (더 이상 진행 불가)
     * null을 반환하는 경우: 필수 조건 미충족 또는 온보딩 종료
     */
    private fun getNextFragmentAndStep(currentStep: OnboardingStep): Pair<Fragment, OnboardingStep>? {
        return when (currentStep) {
            OnboardingStep.START ->
                OnboardingSeasonFragment() to OnboardingStep.SEASON

            OnboardingStep.SEASON -> {
                // 계절 미선택 시 진행 불가
                if (viewModel.selectedSeason.value != null) {
                    OnboardingKeywordFragment() to OnboardingStep.KEYWORD
                } else null
            }

            OnboardingStep.KEYWORD -> {
                // 사용자가 직접 입력을 선택했는지에 따라 분기
                if (viewModel.isDirectInput.value == true) {
                    OnboardingCustomKeywordFragment() to OnboardingStep.CUSTOM_KEYWORD
                } else {
                    viewModel.infoTextStep.value = InfoTextStep.AREUM_BORN.value
                    OnboardingInfoFragment() to OnboardingStep.INFO
                }
            }

            OnboardingStep.CUSTOM_KEYWORD -> {
                viewModel.infoTextStep.value = InfoTextStep.AREUM_BORN.value
                OnboardingInfoFragment() to OnboardingStep.INFO
            }

            OnboardingStep.INFO ->
                OnboardingNicknameFragment() to OnboardingStep.NICKNAME

            OnboardingStep.NICKNAME -> {
                // 닉네임 미입력 시 진행 불가
                if (isNicknameValid()) {
                    viewModel.infoTextStep.value = InfoTextStep.NICKNAME_INPUT.value
                    OnboardingInfoFragment() to OnboardingStep.FINAL
                } else null
            }

            OnboardingStep.FINAL -> {
                navigateToMain()
                null
            }
        }
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fcv_onboarding, fragment, tag)
            .addToBackStack(tag)  // 뒤로가기 지원을 위해 백스택에 추가
            .commit()
    }

    private fun updateUI(stepValue: Int) {
        updateProgressBar(stepValue)
        updateButtonText(stepValue)
        updateButtonState(stepValue)
    }

    private fun updateProgressBar(stepValue: Int) {
        val step = OnboardingStep.fromValue(stepValue) ?: OnboardingStep.START
        val displayStep = calculateDisplayStep(step)

        binding.pbOnboarding.progress = displayStep
        binding.tvProgressNum.text = "$displayStep/5"
    }

    /**
     * Info 화면은 내부 텍스트 단계에 따라 진행바가 달라지므로 별도 계산
     */
    private fun calculateDisplayStep(step: OnboardingStep): Int {
        return when (step) {
            OnboardingStep.INFO, OnboardingStep.FINAL -> {
                val infoTextStep = InfoTextStep.fromValue(viewModel.infoTextStep.value ?: 0)
                infoTextStep.displayProgress
            }
            else -> step.displayProgress
        }
    }

    private fun updateButtonText(stepValue: Int) {
        val step = OnboardingStep.fromValue(stepValue) ?: OnboardingStep.START

        binding.btnNext.text = when (step) {
            OnboardingStep.START -> {
                if (viewModel.isTextUpdated) ButtonText.I_AM.text else ButtonText.START.text
            }
            OnboardingStep.FINAL -> {
                val infoTextStep = InfoTextStep.fromValue(viewModel.infoTextStep.value ?: 0)
                if (infoTextStep == InfoTextStep.FINAL_MESSAGE) {
                    ButtonText.START_JOURNEY.text
                } else {
                    ButtonText.NEXT.text
                }
            }
            else -> ButtonText.NEXT.text
        }
    }

    private fun updateButtonState(stepValue: Int) {
        val step = OnboardingStep.fromValue(stepValue) ?: OnboardingStep.START
        val (isEnabled, colorRes) = calculateButtonState(step)

        binding.btnNext.isEnabled = isEnabled
        binding.btnNext.setBackgroundColor(ContextCompat.getColor(this, colorRes))
    }

    /**
     * @return Pair<버튼 활성화 여부, 색상 리소스>
     * 각 단계별로 버튼 활성화 조건이 다르므로 분기 처리
     */
    private fun calculateButtonState(step: OnboardingStep): Pair<Boolean, Int> {
        val isEnabled = when (step) {
            // 시작 화면과 Info 화면은 항상 활성화
            OnboardingStep.START, OnboardingStep.INFO -> true

            // 선택이 필요한 단계는 ViewModel의 선택 상태 확인
            OnboardingStep.SEASON, OnboardingStep.KEYWORD,
            OnboardingStep.CUSTOM_KEYWORD, OnboardingStep.NICKNAME -> {
                viewModel.isKeywordSelected.value ?: false
            }

            OnboardingStep.FINAL -> {
                val infoTextStep = InfoTextStep.fromValue(viewModel.infoTextStep.value ?: 0)
                // 닉네임 입력 단계에서만 닉네임 검증 필요
                if (infoTextStep == InfoTextStep.NICKNAME_INPUT) {
                    isNicknameValid()
                } else {
                    true
                }
            }
        }

        val colorRes = if (isEnabled) R.color.pink1 else R.color.pink2
        return isEnabled to colorRes
    }

    private fun setupObservers() {
        observeKeywordSelection()
        observeDirectInput()
        observeNickname()
        observeInfoTextStep()
    }

    /**
     * 선택이 필요한 단계에서만 버튼 상태를 동적으로 업데이트
     * updateUI()에서 이미 초기 상태를 설정하므로, 여기서는 변경 사항만 감지
     */
    private fun observeKeywordSelection() {
        viewModel.isKeywordSelected.observe(this) { isEnabled ->
            val step = OnboardingStep.fromValue(viewModel.currentStep.value ?: 0) ?: return@observe

            val needsObserve = step in listOf(
                OnboardingStep.SEASON,
                OnboardingStep.KEYWORD,
                OnboardingStep.CUSTOM_KEYWORD,
                OnboardingStep.NICKNAME
            )

            if (needsObserve) {
                binding.btnNext.isEnabled = isEnabled
                val color = if (isEnabled) R.color.pink1 else R.color.pink2
                binding.btnNext.setBackgroundColor(ContextCompat.getColor(this, color))
            }
        }
    }

    /**
     * 키워드 선택 화면에서 "직접 입력" 클릭 시 즉시 커스텀 키워드 화면으로 전환
     * 버튼 클릭이 아닌 TextView 클릭으로 Fragment 전환하기 위해 별도 observe 필요
     */
    private fun observeDirectInput() {
        viewModel.isDirectInput.observe(this) { isDirectInput ->
            val currentStep = OnboardingStep.fromValue(viewModel.currentStep.value ?: 0)

            if (currentStep == OnboardingStep.KEYWORD && isDirectInput) {
                replaceFragment(
                    OnboardingCustomKeywordFragment(),
                    OnboardingStep.CUSTOM_KEYWORD.value.toString()
                )
                viewModel.currentStep.value = OnboardingStep.CUSTOM_KEYWORD.value
                updateUI(OnboardingStep.KEYWORD.value)
                viewModel.isKeywordSelected.value = false
            }
        }
    }

    /**
     * Info 화면의 닉네임 입력 단계에서만 실시간으로 버튼 활성화 상태 변경
     * 다른 단계에서는 nickname이 변경되어도 버튼 상태에 영향 없음
     */
    private fun observeNickname() {
        viewModel.nickname.observe(this) { nickname ->
            val step = OnboardingStep.fromValue(viewModel.currentStep.value ?: 0)
            val infoTextStep = InfoTextStep.fromValue(viewModel.infoTextStep.value ?: 0)

            if (step == OnboardingStep.INFO && infoTextStep == InfoTextStep.NICKNAME_INPUT) {
                val isEnabled = !nickname.isNullOrEmpty()
                binding.btnNext.isEnabled = isEnabled
                val color = if (isEnabled) R.color.pink1 else R.color.pink2
                binding.btnNext.setBackgroundColor(ContextCompat.getColor(this, color))
            }
        }
    }

    /**
     * Info 텍스트 단계가 바뀔 때마다 진행바와 버튼 텍스트 업데이트 필요
     */
    private fun observeInfoTextStep() {
        viewModel.infoTextStep.observe(this) {
            updateUI(viewModel.currentStep.value ?: 0)
        }
    }

    private fun isNicknameValid(): Boolean = !viewModel.nickname.value.isNullOrEmpty()

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}