package com.example.areumdap.UI.record

import android.os.Bundle
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
        val sessionId = requireArguments().getInt(ARG_SESSION_ID, -1)
        val threadId = sessionId.toLong()

        val binding = FragmentChatDetailBinding.bind(view)
        val adapter = ChatDetailRVAdapter()
        binding.chatRv.adapter = adapter
        binding.chatRv.layoutManager = LinearLayoutManager(requireContext())

        if (threadId != -1L) {
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
                .replace(R.id.main_frm, ReportFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        (activity as? MainActivity)?.setToolbar(false)
        super.onDestroyView()
    }

    companion object {
        private const val ARG_SESSION_ID = "sessionId"

        fun newInstance(sessionId: Int) = ChatDetailFragment().apply {
            arguments = Bundle().apply { putInt(ARG_SESSION_ID, sessionId) }
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
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        // 서버 시간이 KST라면 이거 켜는 게 안전
        sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        sdf.parse(value)?.time ?: System.currentTimeMillis()
    } catch (_: Exception) {
        System.currentTimeMillis()
    }
}
