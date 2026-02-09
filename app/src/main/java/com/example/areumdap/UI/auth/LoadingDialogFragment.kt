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
        val nickname = TokenManager.getUserNickname() ?: ""
        binding.loadingIdTv.text = nickname
    }
}