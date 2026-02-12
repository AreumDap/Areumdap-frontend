package com.example.areumdap.UI.record

import android.os.Bundle
import android.text.TextPaint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.doOnLayout
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
import com.example.areumdap.R
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
                val chip = layoutInflater.inflate(R.layout.item_hashtag, binding.hashtagGroup, false) as com.google.android.material.chip.Chip
                chip.text = tag
                binding.hashtagGroup.addView(chip)
            }
    }

    fun wrapByWord(text: String, paint: TextPaint, maxWidthPx: Float): String {
        val words = text.split(Regex("\\s+"))
        val sb = StringBuilder()
        var line = ""

        for (w in words) {
            val candidate = if (line.isEmpty()) w else "$line $w"
            if (paint.measureText(candidate) <= maxWidthPx) {
                line = candidate
            } else {
                if (sb.isNotEmpty()) sb.append('\n')
                sb.append(line)
                line = w
            }
        }
        if (line.isNotEmpty()) {
            if (sb.isNotEmpty()) sb.append('\n')
            sb.append(line)
        }
        return sb.toString()
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

            val reportId = requireArguments().getLong("reportId", -1L)
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
                                binding.reportSummaryTv.doOnLayout {
                                    val width = binding.reportSummaryTv.width - binding.reportSummaryTv.paddingLeft - binding.reportSummaryTv.paddingRight
                                    binding.reportSummaryTv.text = wrapByWord(data.summaryContent, binding.reportSummaryTv.paint, width.toFloat())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
