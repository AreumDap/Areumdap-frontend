package com.example.areumdap.UI.Onboarding.fragment

import android.R.attr.text
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.areumdap.UI.Onboarding.OnboardingActivity
import com.example.areumdap.UI.Onboarding.OnboardingViewModel
import com.example.areumdap.databinding.FragmentOnboardingKeywordBinding
import kotlin.getValue

class OnboardingKeywordFragment : Fragment(){
    private var _binding: FragmentOnboardingKeywordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OnboardingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingKeywordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isKeywordSelected.value = false
        viewModel.isDirectInput.value = false

        // 선택한 계절에 따라 텍스트 변경
        viewModel.selectedSeason.observe(viewLifecycleOwner) { season ->
            val seasonTextResult = "${season}과 가장 닮아 계시군요!"
            val seasonKeywordGuide = "${season}의 어떤 부분과\n가장 닮았다고 생각하시나요?"
            binding.tvSelectedSeasonResult.text = android.text.Html.fromHtml(seasonTextResult, android.text.Html.FROM_HTML_MODE_LEGACY)
            binding.tvKeywordSelectionGuide.text = android.text.Html.fromHtml(seasonKeywordGuide, android.text.Html.FROM_HTML_MODE_LEGACY)
            }

        binding.tvDirectKeyword.setOnClickListener {
            viewModel.isDirectInput.value = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}