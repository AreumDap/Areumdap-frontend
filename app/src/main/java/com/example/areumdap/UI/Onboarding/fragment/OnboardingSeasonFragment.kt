package com.example.areumdap.UI.Onboarding.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.areumdap.R
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.areumdap.UI.Onboarding.OnboardingActivity
import com.example.areumdap.UI.Onboarding.OnboardingViewModel
import com.example.areumdap.databinding.FragmentOnboardingSeasonBinding
import kotlin.getValue

class OnboardingSeasonFragment: Fragment(){
    private var _binding: FragmentOnboardingSeasonBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OnboardingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingSeasonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSpring.setOnClickListener { handleSeasonClick("봄", it as com.google.android.material.button.MaterialButton) }
        binding.btnSummer.setOnClickListener { handleSeasonClick("여름", it as com.google.android.material.button.MaterialButton) }
        binding.btnAutumn.setOnClickListener { handleSeasonClick("가을", it as com.google.android.material.button.MaterialButton) }
        binding.btnWinter.setOnClickListener { handleSeasonClick("겨울", it as com.google.android.material.button.MaterialButton) }
    }

    private fun handleSeasonClick(season: String, clickedButton: com.google.android.material.button.MaterialButton) {
        val currentSelected = viewModel.selectedSeason.value

        if (currentSelected == season) {
            // 이미 선택된 계절을 다시 누른 경우 -> 취소 처리
            viewModel.selectedSeason.value = null
            viewModel.isKeywordSelected.value = false // 하단 버튼 비활성화 (pink2)
            resetAllButtons()
        } else {
            // 새로운 계절을 선택한 경우
            resetAllButtons() // 먼저 다른 버튼들 초기화

            val seasonKey = when (season) {
                "봄" -> "spring"
                "여름" -> "summer"
                "가을" -> "autumn"
                "겨울" -> "winter"
                else -> "spring"
            }
            requireActivity().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                .edit().putString("SEASON", seasonKey).apply()

            // 각 계절에 맞는 배경색
            val selectedColor = when (season) {
                "봄" -> R.color.pink2
                "여름" -> R.color.green2
                "가을" -> R.color.yellow2
                "겨울" -> R.color.blue2
                else -> R.color.white
            }

            // 각 계절에 맞는 아이콘
            val selectedIcon = when (season) {
                "봄" -> R.drawable.ic_spring
                "여름" -> R.drawable.ic_summer
                "가을" -> R.drawable.ic_fall
                "겨울" -> R.drawable.ic_winter
                else -> R.drawable.ic_spring
            }

            // 선택된 버튼 스타일 적용 (배경색, 텍스트색 흰색, 아이콘)
            clickedButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), selectedColor)
            clickedButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            clickedButton.icon = ContextCompat.getDrawable(requireContext(), selectedIcon)

            // 뷰모델 데이터 업데이트
            viewModel.selectedSeason.value = season
            viewModel.isKeywordSelected.value = true
        }
    }

    private fun resetAllButtons() {
        val buttons = listOf(binding.btnSpring, binding.btnSummer, binding.btnAutumn, binding.btnWinter)

        // 각 버튼에 대응하는 기본 아이콘
        val defaultIcons = listOf(
            R.drawable.ic_spring_color,
            R.drawable.ic_summer_color,
            R.drawable.ic_fall_color,
            R.drawable.ic_winter_color
        )

        buttons.forEachIndexed { index, button ->
            button.backgroundTintList = ContextCompat.getColorStateList(requireContext(),R.color.white)
            button.icon = ContextCompat.getDrawable(requireContext(), defaultIcons[index])
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
    }
}