package com.example.areumdap.UI.record

import android.os.Bundle
import android.widget.Toast
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.areumdap.data.api.ChatReportApiService
import com.example.areumdap.data.repository.ChatReportRepository
import com.example.areumdap.data.repository.ChatReportRepositoryImpl
import com.example.areumdap.data.source.RetrofitClient
import com.example.areumdap.R
import com.example.areumdap.adapter.ChatDetailRVAdapter
import com.example.areumdap.UI.auth.MainActivity
import com.example.areumdap.databinding.FragmentChatDetailBinding
import com.example.areumdap.data.model.ChatMessage
import com.example.areumdap.data.model.Sender
import com.example.areumdap.data.model.HistoryDto
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ChatDetailFragment : Fragment(R.layout.fragment_chat_detail) {
    private var navigatingToReport = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val threadId = requireArguments().getLong(ARG_THREAD_ID, -1L)
        if (threadId == -1L) return

        val binding = FragmentChatDetailBinding.bind(view)

        val adapter = ChatDetailRVAdapter()
        binding.chatRv.adapter = adapter
        binding.chatRv.layoutManager = LinearLayoutManager(requireContext())

        // repo 연결 (토큰/헤더 처리는 너희 쪽에서 따로 한다고 했으니 여기선 그대로)
        val api = RetrofitClient.create(ChatReportApiService::class.java)
        val repo: ChatReportRepository = ChatReportRepositoryImpl(api)

        var reportId: Long? = null

        viewLifecycleOwner.lifecycleScope.launch {
            repo.getThreadHistories(threadId)
                .onSuccess { data ->
                    reportId = data.reportId
                    val messages = data.histories.flatMap { it.toChatMessages() }
                    adapter.submitList(messages)
//                    if (messages.isNotEmpty()) {
//                        binding.chatRv.scrollToPosition(messages.size - 1)
//                    }
                }
                .onFailure { e ->
                    e.printStackTrace()
                }
        }

        setupToolbar()

        binding.icReportIv.setOnClickListener {
            val rid = reportId
            if (rid == null || rid <= 0L) {
                Toast.makeText(requireContext(), "레포트 정보를 찾을 수 없어요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            navigatingToReport = true
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, ReportFragment().apply{
                    arguments = Bundle().apply{
                        putLong("reportId", rid)
                    }
                })
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        if (!navigatingToReport) {
            (activity as? MainActivity)?.setToolbar(false)
        }
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        setupToolbar()
    }

    private fun setupToolbar() {
        val titleFromArgs = arguments?.getString(ARG_THREAD_TITLE).orEmpty()
        val title = if (titleFromArgs.isBlank()) "대화 기록" else titleFromArgs
        (activity as? MainActivity)?.setToolbar(
            visible = true,
            title = title,
            showBackButton = true,
            subText = "",
            onBackClick = { parentFragmentManager.popBackStack() }
        )
    }

    companion object {
        private const val ARG_THREAD_ID = "threadId"
        private const val ARG_THREAD_TITLE = "threadTitle"
        fun newInstance(threadId: Long, title:String?=null) = ChatDetailFragment().apply {
            arguments = Bundle().apply { putLong(ARG_THREAD_ID, threadId)
            putString(ARG_THREAD_TITLE, title)}
        }
    }
}

private fun HistoryDto.toChatMessages(): List<ChatMessage> {
    val sender = when (senderType.uppercase()) {
        "USER", "ME" -> Sender.ME
        else -> Sender.AI
    }
    val timeMillis = parseToMillis(createdAt)

    if (sender == Sender.AI) {
        val parts = splitToBubbles(content)
        if (parts.isNotEmpty()) {
            return parts.mapIndexed { index, part ->
                ChatMessage(
                    id = "${id}_$index",
                    sender = sender,
                    text = part,
                    time = timeMillis + index,
                    chatHistoryId = id
                )
            }
        }
    }

    return listOf(
        ChatMessage(
            id = id.toString(),
            sender = sender,
            text = content,
            time = timeMillis,
            chatHistoryId = id
        )
    )
}


// 구분자에 따라 버블 나누기
private fun splitToBubbles(text: String): List<String> {
    val regex = Regex("(?<=[.!?])\\s+")
    return text.trim()
        .split(regex)
        .map { it.trim() }
        .filter { it.isNotBlank() }
}

private fun parseToMillis(value: String): Long {
    // createdAt 예: "2026-01-23T01:52:52"
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss"
    )

    for (p in patterns) {
        try {
            val sdf = SimpleDateFormat(p, Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            val t = sdf.parse(value)?.time
            if (t != null) return t
        } catch (e: Exception) {
        }
    }
    return System.currentTimeMillis()
}
