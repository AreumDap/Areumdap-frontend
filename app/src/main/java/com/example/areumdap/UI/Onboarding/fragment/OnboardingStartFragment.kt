package com.example.areumdap.UI.Onboarding.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.areumdap.UI.Onboarding.OnboardingActivity
import com.example.areumdap.UI.Onboarding.OnboardingViewModel
import com.example.areumdap.databinding.FragmentOnboardingStartBinding

class OnboardingStartFragment : Fragment() {
    private var _binding: FragmentOnboardingStartBinding? = null
    private val binding get() = _binding!!

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

        // 시작 화면 진입 시 하단 버튼을 즉시 활성화(pink1) 상태로
        viewModel.isKeywordSelected.value = true
    }

    // 시작할게요 클릭시 초기진입 -> 1단계로
    fun changeText(){
        binding.tvGreetingTitle.text="그럼, 첫번째 질문 드릴게요."
        binding.tvGreetingDesc.text="나와 가장 닮은 계절은 무엇인가요?"
    }

    // 뒤로가기 시 초기진입 텍스트로
    fun resetText() {
        binding.tvGreetingTitle.text = "반가워요!"
        binding.tvGreetingDesc.text = "아름답과 함께\n나다움을 찾아가는 여정을\n시작해볼까요?"
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}