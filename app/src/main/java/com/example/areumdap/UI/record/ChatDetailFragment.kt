package com.example.areumdap.UI.record

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.areumdap.Data.DummyChatData
import com.example.areumdap.R
import com.example.areumdap.RVAdapter.ChatDetailRVAdapter
import com.example.areumdap.UI.MainActivity
import com.example.areumdap.databinding.FragmentChatDetailBinding

class ChatDetailFragment : Fragment(R.layout.fragment_chat_detail) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val sessionId = requireArguments().getInt(ARG_SESSION_ID, -1)

        // RV 연결
        val binding = FragmentChatDetailBinding.bind(view)
        val adapter = ChatDetailRVAdapter()
        binding.chatRv.adapter = adapter  // 너 RV id로 바꿔

        // 더미 메시지 세팅
        binding.chatRv.layoutManager = LinearLayoutManager(requireContext())
        adapter.submitList(DummyChatData.getMessages(sessionId))
        (activity as? MainActivity)?.setToolbar(
            visible = true,
            title = "나에게 가장 중요한 가치",
            showBackButton = true,
            subText = "",
            onBackClick = { parentFragmentManager.popBackStack()}
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