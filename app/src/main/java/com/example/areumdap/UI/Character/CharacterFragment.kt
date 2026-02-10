package com.example.areumdap.UI.Character

import android.os.Bundle
import android.view.LayoutInflater
import com.example.areumdap.R
import android.widget.TextView
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.areumdap.data.source.RetrofitClient
import com.example.areumdap.adapter.TaskPageVPAdapter
import com.example.areumdap.databinding.FragmentCharacterBinding
import com.bumptech.glide.Glide
import com.example.areumdap.data.model.CharacterLevelUpResponse
import com.example.areumdap.data.repository.CharacterViewModelFactory

class CharacterFragment : Fragment() {
    private var _binding: FragmentCharacterBinding? = null
    private val binding get() = _binding!!

    // 뷰모델 초기화 (Activity Scope로 변경)
    private val viewModel: CharacterViewModel by activityViewModels {
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
        viewModel.fetchMyCharacter()
        
        // 사용자가 페이지를 넘기지 못하게 하는것
        binding.characterVp.isUserInputEnabled = false

        // 카테고리 목록 정의
        val categories = listOf("전체", "진로","관계","자기성찰","감정","성장","기타")

        val spinnerAdapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.item_spinner_text,
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

        // 태그 매핑
        val tagMap = mapOf(
            "전체" to "전체",
            "진로" to "CAREER",
            "관계" to "RELATION",
            "자기성찰" to "SELF_REFLECTION",
            "감정" to "EMOTION",
            "성장" to "GROWTH",
            "기타" to "ELSE"
        )

        binding.taskFilterSp.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = categories[position]
                val selectedTag = tagMap[selectedCategory]
                viewModel.setSelectedTag(selectedTag)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

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
                
                val hasTask = it.missions?.any { mission -> 
                    val status = mission.status?.uppercase() ?: ""
                    status != "COMPLETED" && status != "DONE" && mission.isCompleted != true && (mission.dDay ?: 0) >= 0 && !viewModel.isMissionCompleted(mission.missionId)
                } ?: false
                
                // 과제가 있을 때만 필터 스피너 보이기
                binding.taskFilterSp.visibility = if (hasTask) View.VISIBLE else View.GONE
                
                val taskAdapter = TaskPageVPAdapter(this, hasTask)
                binding.characterVp.adapter = taskAdapter
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
            }
        }
    }

    private fun updateCharacterUI(levelData: CharacterLevelUpResponse){
        // 레벨 업데이트
        val displayLevel = levelData.level ?: levelData.previousLevel ?: 0
        binding.characterLevelTv.text = "$displayLevel"

        // 경험치 최대치
        val maxProgress = levelData.maxXp
        binding.characterProgressBar.max = maxProgress
        binding.characterProgressBar.progress = levelData.currentXp

        binding.characterXpLevelTv.text = "${levelData.currentXp}"
        binding.characterFinalLevelTv.text = "$maxProgress"
        
        // 캐릭터 이미지 로드
        Glide.with(this)
            .load(levelData.imageUrl)
            .error(R.drawable.ic_character)
            .into(binding.characterIv)

        // 경험치가 다 채워졌으면 다음버튼 활성화
        if(levelData.currentXp >= maxProgress && maxProgress > 0){
            binding.characterNextLevelBtn.visibility = View.VISIBLE
        } else {
            binding.characterNextLevelBtn.visibility = View.GONE
        }
    }
    override fun onResume() {
        super.onResume()
        viewModel.fetchMyCharacter()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}