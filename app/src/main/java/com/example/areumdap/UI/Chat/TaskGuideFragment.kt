package com.example.areumdap.UI.Chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.areumdap.R
import com.example.areumdap.UI.Character.CharacterFragment
import com.example.areumdap.UI.Home.HomeFragment
import com.example.areumdap.databinding.FragmentTaskGuideBinding


class TaskGuideFragment: Fragment() {
    lateinit var binding : FragmentTaskGuideBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTaskGuideBinding.inflate(inflater,container,false)
        binding.btnTaskPage.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, CharacterFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.btnHome.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, HomeFragment())
                .addToBackStack(null)
                .commit()
        }
        return binding.root
    }
}