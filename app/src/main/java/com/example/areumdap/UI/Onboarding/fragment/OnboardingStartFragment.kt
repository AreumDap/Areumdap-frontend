package com.example.areumdap.UI.onboarding.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.areumdap.UI.onboarding.OnboardingActivity
import com.example.areumdap.UI.onboarding.OnboardingViewModel
import com.example.areumdap.databinding.FragmentOnboardingStartBinding

class OnboardingStartFragment : Fragment() {
    private var _binding: FragmentOnboardingStartBinding? = null
    private val binding get() = _binding!!

    // 액티비티와 뷰모델을 공유하여 버튼 상태를 제어합니다.
    private val viewModel: OnboardingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 현재 단계를 0으로 설정하여 프로그레스바가 0/5가 되도록 합니다.
        (activity as? OnboardingActivity)?.updateProgress(0)

        // 2. 시작 화면 진입 시 하단 버튼을 즉시 활성화(pink1) 상태로 바꿉니다.
        // 이 값을 true로 설정하면 OnboardingActivity의 setupObserve 로직에 의해 버튼이 활성화됩니다.
        viewModel.isKeywordSelected.value = true
    }

    fun changeText(){
        binding.tvGreetingTitle.text="그럼, 첫번째 질문 드릴게요."
        binding.tvGreetingDesc.text="나와 가장 닮은 계절은 무엇인가요?"
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // 메모리 누수를 방지하기 위해 바인딩 객체를 해제합니다.
        _binding = null
    }
}