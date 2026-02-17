package com.example.areumdap.UI.record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.areumdap.data.api.ChatReportApiService
import com.example.areumdap.data.repository.ChatReportRepository
import com.example.areumdap.data.repository.ChatReportRepositoryImpl
import com.example.areumdap.data.source.RetrofitClient
import com.example.areumdap.adapter.RecordRVAdapter
import com.example.areumdap.databinding.FragmentRecordBinding
import com.example.areumdap.UI.auth.Category
import com.example.areumdap.data.model.RecordItem
import com.example.areumdap.R
import com.example.areumdap.data.model.UserChatThread
import kotlinx.coroutines.launch

class RecordFragment : Fragment() {

    private var _binding : FragmentRecordBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RecordRVAdapter

    private lateinit var viewModel : ChatThreadViewModel
    private var allThreads: List<UserChatThread> = emptyList()
    private var currentFilter: String = "최신순"
    private val filterOptions = listOf(
        "최신순",
        "오래된순",
        "즐겨찾기",
        "진로",
        "관계",
        "자기성찰",
        "감정",
        "성장",
        "기타"
    )

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
        (activity as? com.example.areumdap.UI.auth.MainActivity)?.setToolbar(false)

        adapter = RecordRVAdapter(
            onItemClick = { item ->
                // 상세 화면 이동
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, ChatDetailFragment.newInstance(item.id, item.title))
                    .addToBackStack(null)
                    .commit()
            },
            onStarClick = { item,_ ->
                viewModel.toggleFavorite(item.id)
            }
        )
        binding.recordSp.post {
            val spinnerWidth = binding.recordSp.width
            val density = resources.displayMetrics.density
            val dropdownWidthPx = (100 * density).toInt()
            val offset = spinnerWidth - dropdownWidthPx
            binding.recordSp.dropDownHorizontalOffset = offset
        }

        binding.recCardRv.apply{
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@RecordFragment.adapter
        }
        //ViewModel (Repo/Impl 연결)
        val api = RetrofitClient.create(ChatReportApiService::class.java)
        val repo: ChatReportRepository = ChatReportRepositoryImpl(api)
        viewModel = ChatThreadViewModel(repo)


        val spinnerAdapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.item_spinner_text,
            R.id.tv_spinner_item,
            filterOptions
        ) {
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.item_spinner_dropdown, parent, false)
                val tv = v.findViewById<android.widget.TextView>(R.id.tv_dropdown_item)
                tv.text = getItem(position)
                return v
            }
        }
        binding.recordSp.adapter = spinnerAdapter
        binding.recordSp.setSelection(filterOptions.indexOf(currentFilter))

        binding.recordSp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                currentFilter = filterOptions[position]
                adapter.submitList(applyFilter(allThreads, currentFilter).map{it.toRecordItem()})
            }

            override fun onNothingSelected(parent: AdapterView<*>?)  = Unit
        }

        viewModel.loadThreads()

        viewLifecycleOwner.lifecycleScope.launch{
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                launch{
                    viewModel.threads.collect { threads ->
                        allThreads = threads
                        adapter.submitList(applyFilter(allThreads, currentFilter).map { it.toRecordItem() })

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

    override fun onResume() {
        super.onResume()
        (activity as? com.example.areumdap.UI.auth.MainActivity)?.setToolbar(false)
    }

    override fun onStart() {
        super.onStart()
        (activity as? com.example.areumdap.UI.auth.MainActivity)?.setToolbar(false)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

private fun applyFilter(
    threads: List<UserChatThread>,
    filter: String
): List<UserChatThread> {
    return when (filter) {
        "최신순" -> threads.sortedByDescending { it.createdAt }
        "오래된순" -> threads.sortedBy { it.createdAt }
        "즐겨찾기" -> threads.filter { it.favorite }.sortedByDescending { it.createdAt }
        "진로" -> threads.filter { it.tag.equals("CAREER", ignoreCase = true) || it.tag == "진로" }
        "관계" -> threads.filter { it.tag.equals("RELATIONSHIP", ignoreCase = true) || it.tag.equals("RELATION", ignoreCase = true) || it.tag == "관계" }
        "자기성찰" -> threads.filter { it.tag.equals("SELF_REFLECTION", ignoreCase = true) || it.tag.equals("REFLECTION", ignoreCase = true) || it.tag == "자기성찰" }
        "감정" -> threads.filter { it.tag.equals("EMOTION", ignoreCase = true) || it.tag == "감정" }
        "성장" -> threads.filter { it.tag.equals("GROWTH", ignoreCase = true) || it.tag == "성장" }
        "기타" -> threads.filter { it.tag.equals("ETC", ignoreCase = true) || it.tag == "기타" }
        else -> threads
    }
}
private fun UserChatThread.toRecordItem(): RecordItem {
    val parsed = summary.parseSummaryJson()
    return RecordItem(
        id = threadId,
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

private fun String.toCategory(): Category = Category.fromServerTag(this)

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



