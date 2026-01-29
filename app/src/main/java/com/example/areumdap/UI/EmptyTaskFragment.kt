package com.example.areumdap.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.areumdap.R
import com.example.areumdap.UI.Chat.ChatFragment
import com.example.areumdap.databinding.FragmentEmptyTaskBinding

class EmptyTaskFragment: Fragment() {
    lateinit var binding: FragmentEmptyTaskBinding

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
            val chatFragment = ChatFragment()

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, chatFragment)
                .addToBackStack(null)
                .commit()
        }
    }
}