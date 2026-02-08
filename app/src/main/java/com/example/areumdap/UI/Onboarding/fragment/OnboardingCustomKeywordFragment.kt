package com.example.areumdap.UI.Onboarding.fragment

import android.R.id.input
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.areumdap.UI.Onboarding.OnboardingViewModel
import com.example.areumdap.databinding.FragmentOnboardingCustomKeywordBinding
import com.example.areumdap.databinding.FragmentOnboardingInfoBinding
import com.example.areumdap.databinding.FragmentOnboardingKeywordBinding
import kotlin.getValue

class OnboardingCustomKeywordFragment: Fragment(){
    private var _binding: FragmentOnboardingCustomKeywordBinding? = null
    private val binding get() = _binding!!

    private val seasonToKorean = mapOf(
        "SPRING" to "봄",
        "SUMMER" to "여름",
        "FALL" to "가을",
        "WINTER" to "겨울"
    )

    private val viewModel: OnboardingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingCustomKeywordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isKeywordSelected.value = !binding.etKeywordWrite.text.isNullOrEmpty()

        // 선택한 계절에 따라 텍스트 변경
        viewModel.selectedSeason.observe(viewLifecycleOwner) { season ->
            val seasonKorean = seasonToKorean[season] ?: "봄"

            val seasonTextResult = "${seasonKorean}과 가장 닮아 계시군요!"
            val seasonKeywordGuide = "${seasonKorean}의 어떤 부분과\n가장 닮았다고 생각하시나요?"
            binding.tvSelectedSeasonResult.text = android.text.Html.fromHtml(seasonTextResult, android.text.Html.FROM_HTML_MODE_LEGACY)
            binding.tvKeywordSelectionGuide.text = android.text.Html.fromHtml(seasonKeywordGuide, android.text.Html.FROM_HTML_MODE_LEGACY)
        }


        // 키워드 직접 입력
        binding.etKeywordWrite.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                val isValid = input.isNotBlank()

                viewModel.isKeywordSelected.value = isValid
                viewModel.directKeyword.value = input.trim()
            }
        })

        // 키워드 선택
        binding.tvSelectKeyword.setOnClickListener {
            viewModel.isDirectInput.value = false
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}