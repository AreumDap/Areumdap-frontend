package com.example.areumdap.UI

import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.areumdap.Network.TokenManager
import com.example.areumdap.databinding.FragmentDialogLoadingBinding
import kotlinx.coroutines.launch

class LoadingDialogFragment : DialogFragment() {
    private lateinit var binding : FragmentDialogLoadingBinding

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

        // 닉네임 설정
        val nickname = TokenManager.getUserNickname()
        
        if (!nickname.isNullOrBlank()) {
            binding.loadingIdTv.text = nickname
        } else {
            // 닉네임이 없으면 서버에서 가져오기
            // CoroutineScope 필요하므로 lifecycleScope 사용
            lifecycleScope.launch {
                try {
                    val result = com.example.areumdap.Network.UserRepository.getProfile()
                    result.onSuccess { profile ->
                        val fetchedNickname = profile.nickname ?: profile.name ?: ""
                        binding.loadingIdTv.text = fetchedNickname
                        // 다음을 위해 저장
                        if (fetchedNickname.isNotBlank()) {
                            TokenManager.saveNickname(fetchedNickname)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}