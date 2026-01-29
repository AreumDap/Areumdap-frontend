package com.example.areumdap.UI.Character

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.areumdap.R
import com.example.areumdap.RVAdapter.CharacterHistoryRVAdapter
import com.example.areumdap.UI.MainActivity
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.setToolbar(
            visible = true,
            title = "",
            showBackButton = true,
            subText = null,
            backgroundColor = android.graphics.Color.TRANSPARENT
        )

        val historyImages = listOf(
            R.drawable.ic_character,
            R.drawable.ic_character,
            R.drawable.ic_character
        )

        val historyAdapter = CharacterHistoryRVAdapter(historyImages)
        binding.characterHistoryRv.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    override fun onDestroyView() {
        (activity as? MainActivity)?.setToolbar(false)
        super.onDestroyView()
    }
}