package com.example.areumdap.UI.Onboarding.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.areumdap.UI.Onboarding.OnboardingActivity
import com.example.areumdap.UI.Onboarding.OnboardingViewModel
import com.example.areumdap.databinding.FragmentOnboardingNicknameBinding
import kotlin.getValue

class OnboardingNicknameFragment : Fragment(){
    private var _binding: FragmentOnboardingNicknameBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OnboardingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingNicknameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isKeywordSelected.value = false

        // 닉네임 입력 감지 리스너
        binding.etNickname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().trim()

                // 뷰모델에 닉네임 저장
                viewModel.nickname.value = input

                // 글자가 1글자 이상 -> 버튼 활성화 (Pink1)
                // 글자가 없으면 -> 버튼 비활성화 (Pink2)
                viewModel.isKeywordSelected.value = input.isNotEmpty()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
