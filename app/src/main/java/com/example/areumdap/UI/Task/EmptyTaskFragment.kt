package com.example.areumdap.UI.Task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.areumdap.R
import com.example.areumdap.UI.Chat.ChatFragment
import com.example.areumdap.UI.Chat.ChatViewModel
import com.example.areumdap.UI.auth.MainActivity
import com.example.areumdap.databinding.FragmentEmptyTaskBinding
import kotlinx.coroutines.launch

class EmptyTaskFragment: Fragment() {
    lateinit var binding: FragmentEmptyTaskBinding
    
    private val chatViewModel: ChatViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmptyTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.emptyTaskRecommendBtn.setOnClickListener {
            (activity as? MainActivity)?.goToHome()
        }

        binding.emptyTaskAiBtn.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                // 채팅 시작
                chatViewModel.startChat(content = "", userQuestionId = null)

                val chatFragment = ChatFragment()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, chatFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
}