package com.example.areumdap.UI.Character

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.example.areumdap.R
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.areumdap.data.source.RetrofitClient
import com.bumptech.glide.Glide
import com.example.areumdap.UI.auth.MainActivity
import com.example.areumdap.data.repository.CharacterViewModelFactory
import com.example.areumdap.databinding.FragmentCharacterXpBinding
import kotlin.getValue

class CharacterXpFragment : Fragment() {
    lateinit var binding : FragmentCharacterXpBinding

    private val viewModel: CharacterViewModel by activityViewModels {
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
            data?.let {

                // 진화 후의 정보를 바로 보여준다.
                // 레벨: 현재 달성한 레벨
                binding.characterXpLevelTv.text = "${it.displayLevel}"
                
                // 필요 경험치: 다음 단계로 가기 위한 목표치 (maxXp)
                binding.characterNextXpTv.text = "${it.maxXp}"

                // 이미지: 진화된 캐릭터 이미지
                binding.characterXpIv.visibility = View.VISIBLE
                Glide.with(this)
                    .load(it.imageUrl)
                    .error(R.drawable.ic_character)
                    .into(binding.characterXpIv)
            }
        }

        binding.characterXpCloseIv.setOnClickListener {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.main_frm, CharacterFragment())
            transaction.commit()
        }
        
        // 에러 메시지 관찰
        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            msg?.let { 
                 android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
                 Log.e("CharacterXpFragment", "Error occurred: $it")
            }
        }
    }
}
