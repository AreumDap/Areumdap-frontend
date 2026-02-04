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
import com.bumptech.glide.Glide
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
                // 성장한 레벨 (currentLevel 혹은 level 사용)
                binding.characterXpLevelTv.text = "${it.currentLevel ?: it.level ?: 0}"
                // 다음 성장 버튼을 위해 필요한 경험치 (requiredXpForNextLevel 혹은 goalXp 사용)
                binding.characterNextXpTv.text = "${it.requiredXpForNextLevel ?: it.goalXp ?: 0}"

                // 캐릭터 이미지 로드
                Glide.with(this)
                    .load(it.imageUrl)
                    .placeholder(R.drawable.ic_character)
                    .error(R.drawable.ic_character)
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(binding.characterXpIv)
            }
        }

        binding.characterXpCloseIv.setOnClickListener {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.main_frm, CharacterFragment())
            transaction.commit()
        }
    }
}
