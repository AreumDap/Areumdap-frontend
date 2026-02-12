package com.example.areumdap.UI.Character

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.areumdap.data.source.RetrofitClient
import com.example.areumdap.adapter.CharacterHistoryRVAdapter
import com.example.areumdap.UI.auth.MainActivity
import com.example.areumdap.databinding.FragmentCharacterHistoryBinding
import com.bumptech.glide.Glide
import com.example.areumdap.R
import com.example.areumdap.data.model.HistoryItem
import com.example.areumdap.data.repository.CharacterViewModelFactory


class CharacterHistoryFragment : Fragment() {
    private var _binding : FragmentCharacterHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CharacterViewModel
    private lateinit var historyAdapter: CharacterHistoryRVAdapter
    private var attemptedGeneration = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCharacterHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apiService = RetrofitClient.service
        val factory = CharacterViewModelFactory(apiService)
        viewModel = ViewModelProvider(this, factory).get(CharacterViewModel::class.java)

        setupToolbar()
        setupRecyclerView()
        setupViewModel()

        viewModel.fetchCharacterHistory()
        viewModel.fetchMyCharacter()
    }
    private fun setupViewModel(){
        viewModel.characterLevel.observe(viewLifecycleOwner) { levelData ->
            levelData?.imageUrl?.let { url ->
                // 상단 메인 캐릭터 이미지 로드
                Glide.with(this)
                    .load(url)
                    .error(R.drawable.ic_character)
                    .into(binding.characterHistoryIv)
            }
            updateCombinedHistory()
        }

        viewModel.historyData.observe(viewLifecycleOwner){ response ->
            response?.let{
                binding.pastContentTv.text = it.pastDescription ?: ""
                binding.presentContentTv.text = it.presentDescription ?: ""



                // 히스토리 목록 이미지들 미리 불러오기
                it.historyList?.forEach { historyItem ->
                    historyItem.imageUrl?.let { url ->
                        Glide.with(this).load(url).preload()
                    }
                }
                
                // 만약 내용이 비어있다면 자동 생성 요청 (최초 1회만)
                if (it.pastDescription.isNullOrEmpty() && it.presentDescription.isNullOrEmpty() && !attemptedGeneration) {

                    attemptedGeneration = true
                    viewModel.requestHistorySummary()
                }

                updateCombinedHistory()
            }
        }
    }

    private fun updateCombinedHistory() {
        val historyItems = viewModel.historyData.value?.historyList ?: emptyList()
        val currentLevelData = viewModel.characterLevel.value

        // 레벨별로 아이템을 저장할 맵
        val levelMap = mutableMapOf<Int, HistoryItem>()
        //  히스토리 데이터
        historyItems.forEach { item ->
            levelMap[item.level] = item
        }

        // 현재 레벨 이미지 URL 파싱
        var templateBaseUrl: String? = null
        currentLevelData?.imageUrl?.let { url ->
            val regex = "(.*stage)(\\d+)(\\.png)".toRegex()
            val match = regex.find(url)
            if (match != null) {
                templateBaseUrl = match.groupValues[1]
            }
        }

        // 현재 레벨 데이터
        currentLevelData?.let { current ->
            val level = current.level ?: current.currentLevel ?: 0
            if (level > 0 || current.imageUrl != null) {
                levelMap[level] = HistoryItem(
                    level = level,
                    achievedDate = "",
                    imageUrl = current.imageUrl
                )
            }
        }

        val combinedList = mutableListOf<HistoryItem>()
        val maxLevelInData = levelMap.keys.maxOrNull() ?: 1
        val maxLevelToShow = maxOf(4, maxLevelInData)

        // 현재 레벨
        val currentLevel = currentLevelData?.currentLevel ?: currentLevelData?.level ?: 0

        for (level in 1..maxLevelToShow) {
            var item = levelMap[level]

            if ((item == null || item.imageUrl.isNullOrEmpty()) 
                && templateBaseUrl != null 
                && level <= currentLevel) {
                
                val inferredUrl = "${templateBaseUrl}${level}.png"
                item = HistoryItem(
                    level = level,
                    achievedDate = item?.achievedDate ?: "",
                    imageUrl = inferredUrl
                )
            } else if (item == null) {
                item = HistoryItem(
                    level = level,
                    achievedDate = "",
                    imageUrl = null
                )
            }
            
            combinedList.add(item!!)
        }

        historyAdapter.updateData(combinedList)
    }

    private fun setupRecyclerView(){
        historyAdapter = CharacterHistoryRVAdapter(emptyList())
        binding.characterHistoryRv.apply{
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
        }
    }

    private fun setupToolbar(){
        (activity as? MainActivity)?.setToolbar(
            visible = true,
            title = "",
            showBackButton = true,
            subText = null,
            backgroundColor = android.graphics.Color.TRANSPARENT
        )
    }

    override fun onDestroyView() {
        (activity as? MainActivity)?.setToolbar(false)
        _binding = null
        super.onDestroyView()
    }
}