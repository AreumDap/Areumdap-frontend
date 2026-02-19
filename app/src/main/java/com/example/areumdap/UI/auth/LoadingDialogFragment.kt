package com.example.areumdap.UI.auth

import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.areumdap.data.source.TokenManager
import com.example.areumdap.databinding.FragmentDialogLoadingBinding
import androidx.lifecycle.lifecycleScope
import com.example.areumdap.data.repository.UserRepository
import kotlinx.coroutines.launch

class LoadingDialogFragment : DialogFragment() {
    private lateinit var binding : FragmentDialogLoadingBinding

    companion object {
        private const val ARG_CUSTOM_MESSAGE = "custom_message"
        private const val ARG_HIDE_FIRST_LINE = "hide_first_line"

        fun newInstance(customMessage: String? = null, hideFirstLine: Boolean = false): LoadingDialogFragment {
            return LoadingDialogFragment().apply {
                arguments = Bundle().apply {
                    customMessage?.let { putString(ARG_CUSTOM_MESSAGE, it) }
                    putBoolean(ARG_HIDE_FIRST_LINE, hideFirstLine)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDialogLoadingBinding.inflate(inflater, container, false)
        
        // 배경 투명하게 설정
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, 
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 로딩 애니메이션 시작
        val animationDrawable = binding.loadingIv.drawable as AnimationDrawable
        animationDrawable.start()

        // Arguments에서 커스텀 메시지 확인
        val customMessage = arguments?.getString(ARG_CUSTOM_MESSAGE)
        val hideFirstLine = arguments?.getBoolean(ARG_HIDE_FIRST_LINE, false) ?: false

        if (hideFirstLine) {
            binding.loadingFirstTv.visibility = View.GONE
        }

        // 닉네임은 항상 표시
        // 닉네임은 항상 표시
        val nickname = TokenManager.getUserNickname()
        if (nickname.isNullOrBlank()) {
            // 닉네임이 없으면 서버에서 가져오기
            viewLifecycleOwner.lifecycleScope.launch {
                val result = UserRepository.getProfile()
                result.onSuccess { profile ->
                    val newNickname = profile.nickname ?: ""
                    if (newNickname.isNotBlank()) {
                        TokenManager.saveNickname(newNickname)
                        binding.loadingIdTv.text = newNickname
                    }
                }
            }
        } else {
            binding.loadingIdTv.text = nickname
        }

        // 커스텀 메시지가 있으면 loading_second_tv 텍스트만 변경
        if (customMessage != null) {
            binding.loadingSecondTv.text = customMessage
        }
    }
}