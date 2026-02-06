package com.example.areumdap.UI.Chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.areumdap.R
import com.example.areumdap.UI.Chat.data.ChatViewModel
import com.example.areumdap.UI.Chat.data.SummaryUiState
import com.example.areumdap.databinding.FragmentCoversationSummaryBinding
import kotlinx.coroutines.launch

class ConversationSummaryFragment : Fragment() {
    private var _binding: FragmentCoversationSummaryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCoversationSummaryBinding.inflate(inflater,container,false)

        val threadId = viewModel.getLastEndedThreadId()
        val accessToken = ""
        if (threadId == null) {
            Toast.makeText(requireContext(), "대화 정보를 찾을 수 없어요.", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.fetchSummary(accessToken, threadId)
        }

        viewLifecycleOwner.lifecycleScope.launch{
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.summaryState.collect{state ->
                    when(state){
                        is SummaryUiState.Idle -> Unit
                        is SummaryUiState.Loading -> {
                            // 로딩뷰 있으면 보여주기
                            // binding.progressBar.visibility = View.VISIBLE
                        }
                        is SummaryUiState.Success ->{
                            val s = state.data.summaryContent

                            binding.sumTitleTv.text = s.title
                            binding.sumBodyTv.text = s.summary
                        }
                        is SummaryUiState.Error ->{
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        binding.nextBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, TaskGuideFragment())
                .addToBackStack(null)
                .commit()

        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
