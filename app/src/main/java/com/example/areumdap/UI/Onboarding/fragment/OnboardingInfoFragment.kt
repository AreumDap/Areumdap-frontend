package com.example.areumdap.UI.Onboarding.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.areumdap.UI.Onboarding.OnboardingViewModel
import com.example.areumdap.databinding.FragmentOnboardingInfoBinding

class OnboardingInfoFragment: Fragment() {

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

        // 시작 화면 진입 시 하단 버튼을 즉시 활성화(pink1) 상태로
        viewModel.isKeywordSelected.value = true

        // infoTextStep 관찰해서 텍스트 업데이트
        viewModel.infoTextStep.observe(viewLifecycleOwner) { step ->
            when (step) {
                0 -> showText1() // "아름이가 태어났어요"
                1 -> showText2() // "서로를 알아가기 위해..."
                2 -> showText3() // "좋아요! 앞으로 ..."
                3 -> showText4() // "아름이는 oo님이..."
            }
        }
    }

    private fun showText1() {
        binding.tvTitle.text = android.text.Html.fromHtml(
            "앞선 당신의 이야기를 통해<br>당신을 닮은 <b>아름이</b>가 태어났어요!",
            android.text.Html.FROM_HTML_MODE_LEGACY
        )
    }

    private fun showText2() {
        binding.tvTitle.text = android.text.Html.fromHtml(
            "서로를 알아가기 위해<br>당신을 뭐라고 부르면 좋을까요?",
            android.text.Html.FROM_HTML_MODE_LEGACY
        )
    }

    private fun showText3() {
        val nickname = viewModel.nickname.value
        binding.tvTitle.text = android.text.Html.fromHtml(
            "좋아요!<br>앞으로 <b>${nickname}</b>님과 <b>아름이</b>의<br>여정이 시작됩니다.",
            android.text.Html.FROM_HTML_MODE_LEGACY
        )
    }

    private fun showText4() {
        val nickname = viewModel.nickname.value
        binding.tvTitle.text = android.text.Html.fromHtml(
            "<b>아름이</b>는 <b>${nickname}</b>님이<br>나를 온전히 알게 되는 그날까지<br>함께합니다.",
            android.text.Html.FROM_HTML_MODE_LEGACY
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
