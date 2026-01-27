package com.example.areumdap.UI.Character

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.areumdap.databinding.FragmentCharacterHistoryBinding


class CharacterHistoryFragment : Fragment() {
    lateinit var binding : FragmentCharacterHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCharacterHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }
}