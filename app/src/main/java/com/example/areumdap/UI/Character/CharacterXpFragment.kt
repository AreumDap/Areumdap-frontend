package com.example.areumdap.UI.Character

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.example.areumdap.R
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.areumdap.Network.RetrofitClient
import com.example.areumdap.UI.MainActivity
import com.example.areumdap.databinding.FragmentCharacterXpBinding
import kotlin.getValue

class CharacterXpFragment : Fragment() {
    lateinit var binding : FragmentCharacterXpBinding

    private val viewModel: CharacterViewModel by viewModels {
        CharacterViewModelFactory(RetrofitClient.service)
    }

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

        (activity as? MainActivity)?.setBottomNavVisibility(true)

        viewModel.fetchCharacterLevel()

        viewModel.characterLevel.observe(viewLifecycleOwner){ data ->
            data?.let{
                // 다음 성장 경험치
                binding.characterNextXpTv.text = "${it.requiredXpForNextLevel}"
                binding.characterXpLevelTv.text = "${it.requiredXpForNextLevel}"
            }
        }

        binding.characterXpCloseIv.setOnClickListener {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.main_frm, CharacterFragment())
            transaction.commit()
        }
    }
}
