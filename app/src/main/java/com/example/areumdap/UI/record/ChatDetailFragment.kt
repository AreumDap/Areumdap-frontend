package com.example.areumdap.UI.record

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.areumdap.Data.api.ChatReportApiService
import com.example.areumdap.Data.repository.ChatReportRepository
import com.example.areumdap.Data.repository.ChatReportRepositoryImpl
import com.example.areumdap.Network.RetrofitClient
import com.example.areumdap.R
import com.example.areumdap.RVAdapter.ChatDetailRVAdapter
import com.example.areumdap.UI.MainActivity
import com.example.areumdap.databinding.FragmentChatDetailBinding
import com.example.areumdap.domain.model.ChatMessage
import com.example.areumdap.domain.model.Sender
import com.example.areumdap.UI.record.data.HistoryDto
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ChatDetailFragment : Fragment(R.layout.fragment_chat_detail) {

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

        viewLifecycleOwner.lifecycleScope.launch {
            repo.getThreadHistories(threadId)
                .onSuccess { data ->
                    val messages = data.histories.map { it.toChatMessage() }
                    adapter.submitList(messages)
                    if (messages.isNotEmpty()) {
                        binding.chatRv.scrollToPosition(messages.size - 1)
                    }
                }
                .onFailure { e ->
                    e.printStackTrace()
                }
        }

        (activity as? MainActivity)?.setToolbar(
            visible = true,
            title = "대화 기록",
            showBackButton = true,
            subText = "",
            onBackClick = { parentFragmentManager.popBackStack() }
        )

        binding.icReportIv.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, ReportFragment().apply{
                    arguments = Bundle().apply{
                        putLong("threadId", threadId)
                    }
                })
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        (activity as? MainActivity)?.setToolbar(false)
        super.onDestroyView()
    }

    companion object {
        private const val ARG_THREAD_ID = "threadId"
        fun newInstance(threadId: Long) = ChatDetailFragment().apply {
            arguments = Bundle().apply { putLong(ARG_THREAD_ID, threadId) }
        }
    }
}

private fun HistoryDto.toChatMessage(): ChatMessage {
    val sender = when (senderType.uppercase()) {
        "USER", "ME" -> Sender.ME
        else -> Sender.AI
    }
    val timeMillis = parseToMillis(createdAt)
    return ChatMessage(
        id = id.toString(),
        sender = sender,
        text = content,
        time = timeMillis
    )
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
            Log.d("parseToMillis", "fail pattern=$p value=$value", e)
        }
    }
    return System.currentTimeMillis()
}
