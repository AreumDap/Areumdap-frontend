package com.example.areumdap.UI.Onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.areumdap.R
import com.example.areumdap.UI.auth.MainActivity
import com.example.areumdap.UI.auth.ToastDialogFragment
import com.example.areumdap.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private val viewModel: OnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // =========================================================
        // [추가된 부분] 로그인 화면에서 전달된 환영 메시지 확인 및 표시
        // =========================================================
        val toastMessage = intent.getStringExtra("TOAST_MESSAGE")
        if (toastMessage != null) {
            val toast = ToastDialogFragment(toastMessage, R.drawable.ic_success)
            toast.show(supportFragmentManager, "WelcomeToast")
        }

        initializeOnboarding(savedInstanceState)
        setupBackPressedHandler()
        setupBackStackListener()
        setupObservers()
        setupNextButtonClickListener()

        binding.ivBack.setOnClickListener {
            handleBackPress()
        }
    }

    private fun initializeOnboarding(savedInstanceState: Bundle?) {
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
        val currentStep =
            OnboardingStep.fromValue(viewModel.currentStep.value ?: 0) ?: OnboardingStep.START

        when (currentStep) {
            OnboardingStep.START -> handleStartStepBackPress()
            OnboardingStep.INFO, OnboardingStep.FINAL -> {
                if (!handleInfoStepBackPress()) {
                    performDefaultBackPress()
                }
            }
            else -> performDefaultBackPress()
        }
    }

    private fun handleStartStepBackPress() {
        if (viewModel.isTextUpdated) {
            val startFragment =
                supportFragmentManager.findFragmentById(R.id.fcv_onboarding) as? OnboardingStartFragment
            startFragment?.resetText()
            viewModel.isTextUpdated = false
            updateUI(OnboardingStep.START.value)
        } else {
            performDefaultBackPress()
        }
    }

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
        supportFragmentManager.addOnBackStackChangedListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fcv_onboarding)
            val step = currentFragment?.tag?.toIntOrNull() ?: OnboardingStep.START.value

            viewModel.currentStep.value = step
            updateUI(step)
        }
    }

    private fun setupNextButtonClickListener() {
        binding.btnNext.setOnClickListener {
            val currentStep =
                OnboardingStep.fromValue(viewModel.currentStep.value ?: 0) ?: OnboardingStep.START

            when (currentStep) {
                OnboardingStep.START -> handleStartStepClick()
                OnboardingStep.INFO, OnboardingStep.FINAL -> handleInfoStepClick(currentStep)
                else -> goToNextStep(currentStep)
            }
        }
    }

    private fun handleStartStepClick() {
        if (!viewModel.isTextUpdated) {
            val fragment =
                supportFragmentManager.findFragmentById(R.id.fcv_onboarding) as? OnboardingStartFragment
            fragment?.changeText()
            viewModel.isTextUpdated = true
            binding.btnNext.text = ButtonText.I_AM.text
        } else {
            goToNextStep(OnboardingStep.START)
        }
    }

    private fun handleInfoStepClick(currentStep: OnboardingStep) {
        val currentTextStep = InfoTextStep.fromValue(viewModel.infoTextStep.value ?: 0)

        when (currentTextStep) {
            InfoTextStep.AREUM_BORN -> {
                viewModel.infoTextStep.value = InfoTextStep.NICKNAME_INTRO.value
            }

            InfoTextStep.NICKNAME_INTRO -> {
                goToNextStep(currentStep)
            }

            InfoTextStep.NICKNAME_INPUT -> {
                if (isNicknameValid()) {
                    viewModel.infoTextStep.value = InfoTextStep.FINAL_MESSAGE.value
                    viewModel.isKeywordSelected.value = true
                    binding.btnNext.text = ButtonText.START_JOURNEY.text
                }
            }

            InfoTextStep.FINAL_MESSAGE -> {
                if (isNicknameValid()) {
                    viewModel.infoTextStep.value = 4
                }
            }
        }
    }

    private fun goToNextStep(currentStep: OnboardingStep) {
        val (nextFragment, nextStep) = getNextFragmentAndStep(currentStep) ?: return

        replaceFragment(nextFragment, nextStep.value.toString())

        if (nextStep in listOf(
                OnboardingStep.SEASON,
                OnboardingStep.KEYWORD,
                OnboardingStep.NICKNAME
            )
        ) {
            viewModel.isKeywordSelected.value = false
        }
    }

    private fun getNextFragmentAndStep(currentStep: OnboardingStep): Pair<Fragment, OnboardingStep>? {
        return when (currentStep) {
            OnboardingStep.START ->
                OnboardingSeasonFragment() to OnboardingStep.SEASON

            OnboardingStep.SEASON -> {
                if (viewModel.selectedSeason.value != null) {
                    OnboardingKeywordFragment() to OnboardingStep.KEYWORD
                } else null
            }

            OnboardingStep.KEYWORD -> {
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
            .addToBackStack(tag)
            .commit()
    }

    private fun updateUI(stepValue: Int) {
        updateProgressBar(stepValue)
        updateButtonText(stepValue)
        updateButtonState(stepValue)

        if (stepValue == OnboardingStep.START.value) {
            binding.ivBack.visibility = View.INVISIBLE
        } else {
            binding.ivBack.visibility = View.VISIBLE
        }
    }

    private fun updateProgressBar(stepValue: Int) {
        val step = OnboardingStep.fromValue(stepValue) ?: OnboardingStep.START
        val displayStep = calculateDisplayStep(step)

        binding.pbOnboarding.progress = displayStep
        binding.tvProgressNum.text = "$displayStep/5"
    }

    private fun calculateDisplayStep(step: OnboardingStep): Int {
        return when (step) {
            OnboardingStep.INFO -> {
                val infoTextStep = InfoTextStep.fromValue(viewModel.infoTextStep.value ?: 0)
                infoTextStep.displayProgress
            }
            OnboardingStep.FINAL -> OnboardingStep.FINAL.displayProgress
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

    private fun calculateButtonState(step: OnboardingStep): Pair<Boolean, Int> {
        val isEnabled = when (step) {
            OnboardingStep.START, OnboardingStep.INFO -> true
            OnboardingStep.SEASON, OnboardingStep.KEYWORD,
            OnboardingStep.CUSTOM_KEYWORD, OnboardingStep.NICKNAME -> {
                viewModel.isKeywordSelected.value ?: false
            }
            OnboardingStep.FINAL -> {
                val infoTextStep = InfoTextStep.fromValue(viewModel.infoTextStep.value ?: 0)
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

    private fun observeInfoTextStep() {
        viewModel.infoTextStep.observe(this) {
            updateUI(viewModel.currentStep.value ?: 0)
        }
    }

    private fun isNicknameValid(): Boolean = !viewModel.nickname.value.isNullOrEmpty()

    public fun navigateToMain() {
        getSharedPreferences("auth", MODE_PRIVATE)
            .edit()
            .putBoolean("onboarding_done", true)
            .apply()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}