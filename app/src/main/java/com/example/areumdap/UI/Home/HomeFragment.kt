package com.example.areumdap.UI.Home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.areumdap.R
import com.example.areumdap.RVAdapter.RecommendQuestionRVAdapter
import com.example.areumdap.UI.Chat.ChatFragment
import com.example.areumdap.databinding.FragmentHomeBinding
import com.example.areumdap.domain.model.Category
import com.example.areumdap.domain.model.RecommendQuestion

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RecommendQuestionRVAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RecommendQuestionRVAdapter { item ->
            goToChat(item.text)
        }
        binding.recommendQuestionRv.adapter = adapter
        adapter.submitList(
            listOf(
                RecommendQuestion(1, "오늘 가장 기억에 남는 순간은?", Category.REFLECTION),
                RecommendQuestion(2, "요즘 가장 고민되는 건?", Category.EMOTION)
            )
        )

        // 대화 바로 시작하기 버튼 클릭 시 ChatFragment로 이동
        binding.chatStartButton.setOnClickListener {
            goToChat()

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun goToChat(prefill:String?=null){
        val fragment = ChatFragment().apply{
            arguments = Bundle().apply{
                putString("prefill_question" , prefill)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.main_frm, fragment)
            .addToBackStack(null)
            .commit()
    }
}
