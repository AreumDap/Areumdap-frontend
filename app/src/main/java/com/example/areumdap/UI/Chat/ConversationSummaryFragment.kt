package com.example.areumdap.UI.Chat

import android.os.Bundle
import android.text.TextPaint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.areumdap.R
import com.example.areumdap.UI.auth.Category
import com.example.areumdap.UI.auth.LoadingDialogFragment
import com.example.areumdap.databinding.FragmentCoversationSummaryBinding
import kotlinx.coroutines.launch

class ConversationSummaryFragment : Fragment() {
    private var _binding: FragmentCoversationSummaryBinding? = null
    private val binding get() = _binding!!
    private var reportRequested = false

    private val viewModel: ChatViewModel by activityViewModels()

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
        _binding = FragmentCoversationSummaryBinding.inflate(inflater,container,false)
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
        val accessToken = ""
        if (threadId == null) {
            Toast.makeText(requireContext(), "대화 정보를 찾을 수 없어요.", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.fetchSummary(accessToken, threadId)

        }

        viewLifecycleOwner.lifecycleScope.launch{
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.summaryState.collect{state ->
                    if (!reportRequested && state is SummaryUiState.Success) {
                        reportRequested = true
                        viewModel.createReport()
                    }
                    when(state){
                        is SummaryUiState.Idle -> Unit
                        is SummaryUiState.Loading -> {
                            val dialog = LoadingDialogFragment()
                            dialog.show(parentFragmentManager, "LoadingDialog")
                        }
                        is SummaryUiState.Success ->{
                            (parentFragmentManager.findFragmentByTag("LoadingDialog") as? LoadingDialogFragment)?.dismiss()
                            val data = state.data
                            val s = state.data.summaryContent


                            binding.sumTitleTv.doOnLayout {
                                val width = binding.sumTitleTv.width - binding.sumTitleTv.paddingLeft - binding.sumTitleTv.paddingRight
                                binding.sumTitleTv.text = wrapByWord(s.title, binding.sumTitleTv.paint, width.toFloat())
                            }

                            binding.sumBodyTv.doOnLayout {
                                val width = binding.sumBodyTv.width - binding.sumBodyTv.paddingLeft - binding.sumBodyTv.paddingRight
                                binding.sumBodyTv.text = wrapByWord(s.summary, binding.sumBodyTv.paint, width.toFloat())
                            }
                            val cat = Category.fromServerTag(data.tag)
                            binding.sumCatIv.setImageResource(cat.iconRes)
                            binding.sumCatTv.text = cat.label
                            binding.sumCatTv.setTextColor(ContextCompat.getColor(requireContext(), cat.colorRes))
                        }
                        is SummaryUiState.Error ->{
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }

                    }
                }
            }
        }
        binding.nextBtn.setOnClickListener {
            val tid = threadId
            if(tid == null){
                Toast.makeText(requireContext(), "대화 정보를 찾을 수 없어요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val fragment = TaskGuideFragment().apply{
                arguments = Bundle().apply{
                    putLong("threadId", tid)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, fragment)
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
