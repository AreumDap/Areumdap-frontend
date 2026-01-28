package com.example.areumdap.UI.Onboarding.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.areumdap.UI.onboarding.OnboardingViewModel
import com.example.areumdap.databinding.FragmentOnboardingCustomKeywordBinding
import kotlin.getValue

class OnboardingCustomKeywordFragment: Fragment(){
    private var _binding: FragmentOnboardingCustomKeywordBinding? = null
    private val binding get() = _binding!!

    // 액티비티와 뷰모델을 공유하여 버튼 상태를 제어합니다.
    private val viewModel: OnboardingViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

}
