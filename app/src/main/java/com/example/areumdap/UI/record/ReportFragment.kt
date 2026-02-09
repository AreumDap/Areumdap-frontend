package com.example.areumdap.UI.record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.areumdap.data.api.ChatReportApiService
import com.example.areumdap.data.repository.ChatReportRepositoryImpl
import com.example.areumdap.data.source.RetrofitClient
import com.example.areumdap.adapter.DiscoveryRVAdapter
import com.example.areumdap.adapter.ReportTaskRVAdapter
import com.example.areumdap.databinding.FragmentReportBinding
import kotlinx.coroutines.launch

class ReportFragment: Fragment() {
    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    private val discoveryRVAdapter = DiscoveryRVAdapter()

    private val reportTaskRVAdapter = ReportTaskRVAdapter()
    private val viewModel: ChatThreadViewModel by viewModels {
        val api = RetrofitClient.create(ChatReportApiService::class.java)
        val repo = ChatReportRepositoryImpl(api)
        ChatThreadViewModelFactory(repo)
    }

    private fun renderHashtags(tags: List<String>) {
        binding.hashtagGroup.removeAllViews()

        tags
            .filter { it.isNotBlank() }
            .take(5)
            .forEach { tag ->
                val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                    text = "#$tag"
                    isClickable = false
                    isCheckable = false
                }
                binding.hashtagGroup.addView(chip)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.foundCardRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = discoveryRVAdapter
        }

        binding.reportTaskRv.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = reportTaskRVAdapter

            val reportId = requireArguments().getLong("threadId", -1L)
            if (reportId == -1L) return

            viewModel.loadReport(reportId)

            collectReportState()

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun collectReportState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.reportState.collect { state ->
                        when (state) {
                            is ReportUiState.Idle -> Unit

                            is ReportUiState.Loading -> {
                                // 로딩뷰 있으면 표시
                            }

                            is ReportUiState.Error -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT)
                                    .show()
                            }

                            is ReportUiState.Success -> {
                                val data = state.data
                                binding.sumDateTv.text = data.createdAt
                                renderHashtags(data.reportTags.map { it.tag })
                                discoveryRVAdapter.submitList(data.insightContents)
                                reportTaskRVAdapter.submitList(data.missions)
                                binding.reportMessageCntTv.text = "${data.messageCount}개"
                                binding.reportTimeCntTv.text = "${data.durationMinutes}분"
                                binding.reportSummaryTv.text = data.summaryContent
                            }
                        }
                    }
                }
            }
        }
    }
}