package com.example.areumdap.UI.Character

import android.os.Bundle
import android.view.LayoutInflater
import com.example.areumdap.R
import android.widget.TextView
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.areumdap.Network.RetrofitClient
import com.example.areumdap.VPAdapter.TaskPageVPAdapter
import com.example.areumdap.databinding.FragmentCharacterBinding
import com.bumptech.glide.Glide

class CharacterFragment : Fragment() {
    private var _binding: FragmentCharacterBinding? = null
    private val binding get() = _binding!!

    //뷰모델 초기화
    private val viewModel: CharacterViewModel by viewModels {
        CharacterViewModelFactory(RetrofitClient.service)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCharacterBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        // 내 캐릭터 정보 불러오는
        viewModel.fetchMyCharacter()

        val hasTask = false
        val taskAdapter = TaskPageVPAdapter(this, hasTask)
        binding.characterVp.adapter = taskAdapter

        // 사용자가 페이지를 넘기지 못하게 하는것
        binding.characterVp.isUserInputEnabled = false

        // 카테고리 목록 정의
        val categories = listOf("전체", "진로","관계","자기성찰","감정","성장","기타")

        val spinnerAdapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.item_spinner_text, // 스피너 겉모양
            R.id.tv_spinner_item,
            categories
        ) {
            // 드롭다운 화면 설정
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val inflater = LayoutInflater.from(context)
                val view = convertView ?: inflater.inflate(R.layout.item_spinner_dropdown, parent, false)

                val tv = view.findViewById<TextView>(R.id.tv_dropdown_item)
                tv.text = getItem(position)

                return view
            }
        }
        binding.taskFilterSp.adapter = spinnerAdapter

        binding.characterGroupIv.setOnClickListener {
            val characterhistoryFragment = CharacterHistoryFragment()

            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, characterhistoryFragment)
                .addToBackStack(null)
                .commit()
        }
        // 다음 단계 확인하기
        binding.characterNextLevelBtn.setOnClickListener{
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.main_frm, CharacterXpFragment())
            transaction.commit()
        }

    }

    private fun setupObservers() {
        viewModel.characterLevel.observe(viewLifecycleOwner) { levelData ->
            levelData?.let {
                updateCharacterUI(it)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateCharacterUI(levelData: com.example.areumdap.UI.Character.Data.CharacterLevelUpResponse){
        // 레벨 업데이트 (level 혹은 previousLevel 사용)
        val displayLevel = levelData.level ?: levelData.previousLevel ?: 0
        binding.characterLevelTv.text = "$displayLevel"

        // 경험치 최대치 (goalXp 혹은 requiredXpForNextLevel 사용)
        val maxProgress = levelData.goalXp ?: levelData.requiredXpForNextLevel ?: 0
        binding.characterProgressBar.max = maxProgress
        binding.characterProgressBar.progress = levelData.currentXp

        binding.characterXpLevelTv.text = "${levelData.currentXp}"
        binding.characterFinalLevelTv.text = "$maxProgress"

        // 캐릭터 이미지 로드
        Glide.with(this)
            .load(levelData.imageUrl)
            .placeholder(R.drawable.ic_character) // 로딩 중 기본 이미지
            .error(R.drawable.ic_character)       // 에러 시 기본 이미지
            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(binding.characterIv)

        // 경험치가 다 채워졌으면 다음버튼 활성화
        if(levelData.currentXp >= maxProgress && maxProgress > 0){
            binding.characterNextLevelBtn.visibility = View.VISIBLE
        } else {
            binding.characterNextLevelBtn.visibility = View.GONE
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}