package com.example.areumdap.UI.onboarding.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.areumdap.UI.onboarding.OnboardingActivity
import com.example.areumdap.UI.onboarding.OnboardingViewModel
import com.example.areumdap.databinding.FragmentOnboardingInfoBinding
import com.example.areumdap.databinding.FragmentOnboardingKeywordBinding
import kotlin.getValue

class OnboardingResultFragment: Fragment() {
    private var _binding: FragmentOnboardingInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OnboardingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 액티비티의 함수를 호출하여 단계 표시 (프로그레스바에 사용)
        (activity as? OnboardingActivity)?.updateProgress(5)
    }

}
