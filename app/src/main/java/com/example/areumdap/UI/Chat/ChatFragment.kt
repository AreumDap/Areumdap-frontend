package com.example.areumdap.UI.Chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.areumdap.UI.MainActivity
import com.example.areumdap.databinding.FragmentChatBinding


class ChatFragment : Fragment() {
    lateinit var binding : FragmentChatBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.setToolbar(
            visible = true,
            title = "대화",
            showBackButton = true,
            subText = "질문 길게 눌러 저장"
        )
    }

    override fun onDestroyView() {
        (activity as? MainActivity)?.setToolbar(false)
        super.onDestroyView()
    }
}