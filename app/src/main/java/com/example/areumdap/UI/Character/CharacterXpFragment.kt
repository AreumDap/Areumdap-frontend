package com.example.areumdap.UI.Character

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.example.areumdap.R
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.areumdap.databinding.FragmentCharacterXpBinding

class CharacterXpFragment : Fragment() {
    lateinit var binding : FragmentCharacterXpBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCharacterXpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.characterXpCloseIv.setOnClickListener {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.main_frm, CharacterFragment())
            transaction.commit()
        }
    }
}
