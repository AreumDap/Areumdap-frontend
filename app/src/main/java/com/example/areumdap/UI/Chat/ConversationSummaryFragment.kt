package com.example.areumdap.UI.Chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.areumdap.Network.TokenManager
import com.example.areumdap.R
import com.example.areumdap.UI.Chat.data.ChatViewModel
import com.example.areumdap.UI.Chat.data.ReportUiState
import com.example.areumdap.UI.Chat.data.SummaryUiState
import com.example.areumdap.UI.LoadingDialogFragment
import com.example.areumdap.databinding.FragmentCoversationSummaryBinding
import com.example.areumdap.domain.model.Category
import kotlinx.coroutines.launch

class ConversationSummaryFragment : Fragment() {
    private var _binding: FragmentCoversationSummaryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by activityViewModels()

    private var reportRequested = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoversationSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recommendTag = arguments?.getString("recommend_tag")
        if (!recommendTag.isNullOrBlank()) {
            viewModel.setRecommendTag(recommendTag)
        }

        val category = recommendTag?.let { tag ->
            runCatching { Category.valueOf(tag) }.getOrNull()
        } ?: Category.ETC

        binding.sumCatIv.setImageResource(category.iconRes)
        binding.sumCatTv.text = category.label
        binding.sumCatTv.setTextColor(ContextCompat.getColor(requireContext(), category.colorRes))

        val threadId = viewModel.getLastEndedThreadId()
        val accessToken = TokenManager.getAccessToken().orEmpty()
        if (threadId == null) {
            Toast.makeText(requireContext(), "대화 정보를 찾을 수 없어요.", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.fetchSummary(accessToken, threadId)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.summaryState.collect { state ->
                        val loadingDialog =
                            childFragmentManager.findFragmentByTag("LoadingDialog") as? LoadingDialogFragment

                        when (state) {
                            is SummaryUiState.Idle -> Unit
                            is SummaryUiState.Loading -> {
                                if (loadingDialog == null) {
                                    LoadingDialogFragment().show(
                                        childFragmentManager,
                                        "LoadingDialog"
                                    )
                                }
                            }

                            is SummaryUiState.Success -> {
                                loadingDialog?.dismiss()

                                val data = state.data
                                val s = state.data.summaryContent

                                binding.sumTitleTv.text = s.title
                                binding.sumBodyTv.text = s.summary
                                val cat = Category.fromServerTag(data.tag)
                                binding.sumCatIv.setImageResource(cat.iconRes)
                                binding.sumCatTv.text = cat.label
                                binding.sumCatTv.setTextColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        cat.colorRes
                                    )
                                )
                                if (!reportRequested) {
                                    reportRequested = true
                                    viewModel.createReportForSummaryScreen()
                                }
                            }

                            is SummaryUiState.Error -> {
                                loadingDialog?.dismiss()
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }

                launch {
                    viewModel.reportState.collect { state ->
                        when (state) {
                            is ReportUiState.Idle -> Unit
                            is ReportUiState.Loading -> Unit
                            is ReportUiState.Success -> {
                                android.util.Log.d("ConversationSummary", "createReport success")
                            }

                            is ReportUiState.Error -> {
                                android.util.Log.e(
                                    "ConversationSummary",
                                    "createReport fail: ${state.message}"
                                )
                            }
                        }
                    }
                }
            }
        }

        binding.nextBtn.setOnClickListener {
            val tid = viewModel.getLastEndedThreadId()
            if (tid == null) {
                    Toast.makeText(requireContext(), "대화 정보를 찾을 수 없어요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val fragment = TaskGuideFragment().apply {
                arguments = Bundle().apply {
                    putLong("threadId", tid)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
