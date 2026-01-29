package com.example.areumdap.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.areumdap.RVAdapter.RecordRVAdapter
import com.example.areumdap.databinding.FragmentRecordBinding
import com.example.areumdap.domain.model.Category
import com.example.areumdap.domain.model.RecordItem

class RecordFragment : Fragment() {

    private var _binding : FragmentRecordBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RecordRVAdapter
    private var items : List<RecordItem> = emptyList()

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
            onItemClick = {item ->
                // 상세 화면 이동
            }
        )
        binding.recCardRv.adapter = adapter
        items=createDummyRecords()
        adapter.submitList(items)
    }

    private fun createDummyRecords(): List<RecordItem> {
        return listOf(
            RecordItem(
                id = 1,
                category = Category.REFLECTION,
                title = "나에게 가장 중요한 가치",
                summary = "대화요약대화요약대화요약대화요약…",
                dateText = "2025. 10. 29",
                isStarred = true
            ),
            RecordItem(
                id = 2,
                category = Category.CAREER,
                title = "요즘 가장 고민되는 건?",
                summary = "대화요약대화요약대화요약대화요약…",
                dateText = "2025. 10. 29",
                isStarred = false
            ),
            RecordItem(
                id = 3,
                category = Category.RELATIONSHIP,
                title = "관계에서 내가 원하는 것",
                summary = "대화요약대화요약대화요약대화요약…",
                dateText = "2025. 10. 29",
                isStarred = false
            ),
            RecordItem(
                id = 4,
                category = Category.EMOTION,
                title = "오늘 가장 기억에 남는 순간",
                summary = "대화요약대화요약대화요약대화요약…",
                dateText = "2025. 10. 28",
                isStarred = true
            )
        )

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}