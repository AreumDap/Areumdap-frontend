package com.example.areumdap.UI.Onboarding.fragment

import android.R.attr.text
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.areumdap.R
import com.example.areumdap.UI.Onboarding.OnboardingActivity
import com.example.areumdap.UI.Onboarding.OnboardingViewModel
import com.example.areumdap.databinding.FragmentOnboardingKeywordBinding
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip
import kotlin.getValue

class OnboardingKeywordFragment : Fragment(){
    private var _binding: FragmentOnboardingKeywordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OnboardingViewModel by activityViewModels()

    // 계절별 키워드 맵
    private val seasonKeywords = mapOf(
        "봄" to listOf("부드러운", "다정한", "낙천적인", "밝은", "순수한", "수줍은", "상냥한", "여린", "투명한"),
        "여름" to listOf("뜨거운", "선명한", "솔직한", "강렬한", "대담한", "적극적인", "추진력 있는", "분명한", "활발한"),
        "가을" to listOf("담담한", "고요한", "느긋한", "깊이 있는", "성숙한", "섬세한", "안정된", "이상적인", "침착한"),
        "겨울" to listOf("고요한", "절제된", "부드러운", "냉정한", "흔들림 없는", "무게 있는", "성숙한", "담담한", "현실적인")
    )

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
        setupSeasonText()

        // 키워드 칩 선택
        setupKeywordChips()

        // 키워드 직접 입력하기
        binding.tvDirectKeyword.setOnClickListener {
            viewModel.isDirectInput.value = true
        }
    }

    private fun setupSeasonText() {
        viewModel.selectedSeason.observe(viewLifecycleOwner) { season ->
            binding.tvSelectedSeasonResult.text = "${season}과 가장 닮아 계시군요!"
            binding.tvKeywordSelectionGuide.text = "${season}의 어떤 부분과\n가장 닮았다고 생각하시나요?"

            // 계절이 바뀌면 키워드도 다시 설정
            setupKeywordChips()
        }
    }

    private fun setupKeywordChips() {
        val selectedSeason = viewModel.selectedSeason.value ?: "봄"
        val keywords = seasonKeywords[selectedSeason] ?: seasonKeywords["봄"]!!

        // 기존 Chip 모두 제거
        binding.flexboxKeywords.removeAllViews()

        // 새 Chip 추가
        keywords.forEach { keyword ->
            val chip = createKeywordChip(keyword)

            // FlexboxLayout.LayoutParams 설정 (간격 적용)
            val params = FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(
                    5.dpToPx(),  // left
                    5.dpToPx(),  // top
                    5.dpToPx(),  // right
                    5.dpToPx()   // bottom
                )
            }
            chip.layoutParams = params

            binding.flexboxKeywords.addView(chip)
        }
    }

    private fun createKeywordChip(keyword: String): Chip {
        return Chip(requireContext()).apply {
            text = keyword

            setTextAppearance(R.style.TA_Body2)

            isCheckable = true
            isCheckedIconVisible = false

            // 초기 상태: 미선택 (흰색 배경 + 테두리)
            applyUnselectedStyle()

            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    applySelectedStyle()
                } else {
                    applyUnselectedStyle()
                }

                handleKeywordSelection(keyword, isChecked)
            }

            if (viewModel.selectedKeywords.value?.contains(keyword) == true) {
                isChecked = true
                applySelectedStyle()
            }

            setChipBackgroundColorResource(android.R.color.transparent)
        }
    }

    // 선택된 상태 스타일 적용 (핑크 배경 + 흰색 텍스트)
    private fun Chip.applySelectedStyle() {
        chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.pink2)
        setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        chipStrokeWidth = 0f
    }

    // 미선택 상태 스타일 적용 (흰색 배경 + 테두리)
    private fun Chip.applyUnselectedStyle() {
        chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.white)
        setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        chipStrokeColor = ContextCompat.getColorStateList(requireContext(), R.color.line_color)
        chipStrokeWidth = 1f * resources.displayMetrics.density
    }


    private fun handleKeywordSelection(keyword: String, isChecked: Boolean) {
        viewModel.toggleKeyword(keyword)
        updateButtonState()
    }

    // 최소 1개 이상의 키워드가 선택되어야 버튼 활성화
    private fun updateButtonState() {
        val hasSelection = viewModel.selectedKeywords.value?.isNotEmpty() == true
        viewModel.isKeywordSelected.value = hasSelection
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}