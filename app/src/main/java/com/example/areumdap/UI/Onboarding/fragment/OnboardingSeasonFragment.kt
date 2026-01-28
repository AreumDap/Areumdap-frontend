package com.example.areumdap.UI.onboarding.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.areumdap.R
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.areumdap.UI.onboarding.OnboardingActivity
import com.example.areumdap.UI.onboarding.OnboardingViewModel
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

        // 1. 진행바를 1/5 단계로 업데이트합니다.
        (activity as? OnboardingActivity)?.updateProgress(1)

        binding.btnSpring.setOnClickListener { handleSeasonClick("봄", it as com.google.android.material.button.MaterialButton) }
        binding.btnSummer.setOnClickListener { handleSeasonClick("여름", it as com.google.android.material.button.MaterialButton) }
        binding.btnAutumn.setOnClickListener { handleSeasonClick("가을", it as com.google.android.material.button.MaterialButton) }
        binding.btnWinter.setOnClickListener { handleSeasonClick("겨울", it as com.google.android.material.button.MaterialButton) }

//        // 2. 계절 선택 시 버튼 활성화 로직
//        // --- 봄 버튼 ---
//        binding.btnSpring.setOnClickListener {
//            // 버튼 배경색 변경
//            binding.btnSpring.backgroundTintList = ContextCompat.getColorStateList(
//                requireContext(),
//                R.color.pink2
//            )
//            // 버튼 내부 아이콘 변경 (선택된 상태의 아이콘으로)
//            binding.btnSpring.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_spring)
//
//            // 버튼 글자색 변경 (흰색 또는 대비되는 색으로)
//            binding.btnSpring.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
//
//            // 하단 '다음' 버튼 활성화 (액티비티의 버튼 색을 pink1으로 변경)
//            viewModel.isKeywordSelected.value = true
//
//            // 선택된 데이터 저장
//            viewModel.selectedSeason.value = "봄"
//        }
//        // --- 여름 버튼 ---
//        binding.btnSummer.setOnClickListener {
//            binding.btnSummer.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.green2)
//            binding.btnSummer.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
//
//            binding.btnSummer.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_summer)
//
//            viewModel.isKeywordSelected.value = true
//            viewModel.selectedSeason.value = "여름"
//        }
//
//        // --- 가을 버튼 ---
//        binding.btnAutumn.setOnClickListener {
//            binding.btnAutumn.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.yellow2)
//            binding.btnAutumn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
//
//            binding.btnAutumn.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_fall)
//
//            viewModel.isKeywordSelected.value = true
//            viewModel.selectedSeason.value = "가을"
//        }
//
//        // --- 겨울 버튼 ---
//        binding.btnWinter.setOnClickListener {
//            binding.btnWinter.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue2)
//            binding.btnWinter.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
//
//            binding.btnWinter.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_winter)
//
//            viewModel.isKeywordSelected.value = true
//            viewModel.selectedSeason.value = "겨울"
//        }
    }

    private fun handleSeasonClick(season: String, clickedButton: com.google.android.material.button.MaterialButton) {
        // 프래그먼트 변수 대신 뷰모델에 저장된 현재 값을 가져옵니다.
        val currentSelected = viewModel.selectedSeason.value

        if (currentSelected == season) {
            // 1. 이미 선택된 계절을 다시 누른 경우 -> 취소 처리
            viewModel.selectedSeason.value = null
            viewModel.isKeywordSelected.value = false // 하단 버튼 비활성화 (pink2)
            resetAllButtons()
        } else {
            // 새로운 계절을 선택한 경우
            resetAllButtons() // 먼저 다른 버튼들 초기화

            // 각 계절에 맞는 배경색 결정
            val selectedColor = when (season) {
                "봄" -> R.color.pink2
                "여름" -> R.color.green2
                "가을" -> R.color.yellow2
                "겨울" -> R.color.blue2
                else -> R.color.white
            }

            // 각 계절에 맞는 '활성화' 아이콘 결정
            val selectedIcon = when (season) {
                "봄" -> R.drawable.ic_spring
                "여름" -> R.drawable.ic_summer
                "가을" -> R.drawable.ic_fall
                "겨울" -> R.drawable.ic_winter
                else -> R.drawable.ic_spring
            }

            // 선택된 버튼 스타일 적용
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

        // 2. 각 버튼에 대응하는 기본 아이콘(비활성 상태) 리스트
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