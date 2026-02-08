package com.example.areumdap.UI.record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.areumdap.Data.api.ChatReportApiService
import com.example.areumdap.Data.repository.ChatReportRepository
import com.example.areumdap.Data.repository.ChatReportRepositoryImpl
import com.example.areumdap.Network.RetrofitClient
import com.example.areumdap.RVAdapter.RecordRVAdapter
import com.example.areumdap.databinding.FragmentRecordBinding
import com.example.areumdap.domain.model.Category
import com.example.areumdap.domain.model.RecordItem
import com.example.areumdap.R
import com.example.areumdap.UI.record.data.ChatThreadViewModel
import com.example.areumdap.UI.record.data.UserChatThread
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RecordFragment : Fragment() {

    private var _binding : FragmentRecordBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RecordRVAdapter

    private lateinit var viewModel : ChatThreadViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecordBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RecordRVAdapter(
            onItemClick = { item ->
                // 상세 화면 이동
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, ChatDetailFragment.newInstance(item.id))
                    .addToBackStack(null)
                    .commit()
            }
        )
        binding.recCardRv.apply{
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@RecordFragment.adapter
        }
        //ViewModel 만들기(Repo/Impl 연결)
        val api = RetrofitClient.create(ChatReportApiService::class.java)
        val repo: ChatReportRepository = ChatReportRepositoryImpl(api)
        viewModel = ChatThreadViewModel(repo)
        viewModel.loadThreads()

        viewLifecycleOwner.lifecycleScope.launch{
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                launch{
                    viewModel.threads.collect { threads ->
                        adapter.submitList(threads.map{it.toRecordItem()})

                    }
                }

                launch{
                    viewModel.error.collect { msg ->
                        if(!msg.isNullOrBlank()){
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()                        }
                    }
                }
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
private fun UserChatThread.toRecordItem(): RecordItem {
    val parsed = summary.parseSummaryJson()
    return RecordItem(
        id = threadId.toInt(),
        category = tag.toCategory(),
        title = parsed.title.ifBlank { content.trim() },
        summary = parsed.summary,
        dateText = createdAt.toKoreanDate(),
        isStarred = favorite
    )
}

private fun String?.toSafeSummary(): String {
    val value = this?.trim().orEmpty()
    return if (value.equals("null", ignoreCase = true)) "" else value
}

private data class ParsedSummary(val title: String, val summary: String)

private fun String?.parseSummaryJson(): ParsedSummary {
    val raw = this.toSafeSummary()
    if (raw.isBlank()) return ParsedSummary(title = "", summary = "")

    return try {
        val obj = org.json.JSONObject(raw)
        val title = obj.optString("title", "").trim()
        val summary = obj.optString("summary", "").trim()
        ParsedSummary(title = title, summary = summary)
    } catch (_: Exception) {
        ParsedSummary(title = "", summary = raw)
    }
}

private fun String.toCategory(): Category = when (this) {
    "SELF_REFLECTION" -> Category.REFLECTION
    "CAREER" -> Category.CAREER
    "RELATIONSHIP" -> Category.RELATIONSHIP
    "EMOTION" -> Category.EMOTION
    else -> Category.REFLECTION
}

private fun String.toKoreanDate(): String {
    // 예: 2026-01-26T01:44:27.394324 -> 2026. 01. 26
    return try {
        val date = this.take(10) // "yyyy-MM-dd"
        val y = date.substring(0, 4)
        val m = date.substring(5, 7)
        val d = date.substring(8, 10)
        "$y. $m. $d"
    } catch (e: Exception) {
        this
    }
}


