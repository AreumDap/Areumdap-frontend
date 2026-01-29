package com.example.areumdap.UI.Chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.areumdap.R
import com.example.areumdap.databinding.FragmentCoversationSummaryBinding

class ConversationSummaryFragment : Fragment() {
    lateinit var binding: FragmentCoversationSummaryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCoversationSummaryBinding.inflate(inflater,container,false)
        binding.nextBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, TaskGuideFragment())
                .addToBackStack(null)
                .commit()

        }
        return binding.root
    }

}