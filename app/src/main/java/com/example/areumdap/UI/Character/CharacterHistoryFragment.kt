package com.example.areumdap.UI.Character

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.areumdap.Network.RetrofitClient
import com.example.areumdap.RVAdapter.CharacterHistoryRVAdapter
import com.example.areumdap.UI.MainActivity
import com.example.areumdap.databinding.FragmentCharacterHistoryBinding


class CharacterHistoryFragment : Fragment() {
    private var _binding : FragmentCharacterHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CharacterViewModel
    private lateinit var historyAdapter: CharacterHistoryRVAdapter

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
    }
    private fun setupViewModel(){
        viewModel = ViewModelProvider(this).get(CharacterViewModel::class.java)

        viewModel.historyData.observe(viewLifecycleOwner){ response ->
            response?.let{
                binding.pastContentTv.text = it.pastDescription ?: ""
                binding.presentContentTv.text = it.presentDescription ?: ""

                // 리사이클러뷰 데이터 갱신
                historyAdapter.updateData(it.historyList)
            }
        }
        // 예외처리
        viewModel.errorMessage.observe(viewLifecycleOwner){ msg ->
        }
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