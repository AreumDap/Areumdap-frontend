package com.example.areumdap.UI.Home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.areumdap.Data.api.ChatbotApi
import com.example.areumdap.Data.repository.ChatbotRepository
import com.example.areumdap.Network.RetrofitClient
import com.example.areumdap.R
import com.example.areumdap.RVAdapter.RecommendQuestionRVAdapter
import com.example.areumdap.UI.Character.CharacterViewModel
import com.example.areumdap.UI.Character.CharacterViewModelFactory
import com.example.areumdap.UI.Chat.ChatFragment
import com.example.areumdap.UI.Home.data.RecommendQuestionViewModel
import com.example.areumdap.databinding.FragmentHomeBinding
import com.example.areumdap.domain.model.Category
import com.example.areumdap.domain.model.RecommendQuestion

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CharacterViewModel by viewModels {
        CharacterViewModelFactory(RetrofitClient.service)
    }

    private val recommendViewModel: RecommendQuestionViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val api = RetrofitClient.create(ChatbotApi::class.java)
                return RecommendQuestionViewModel(ChatbotRepository(api)) as T
            }
        }
    }

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

        setupCharacterObserver()
        setupRecommendObserver()

        viewModel.fetchMyCharacter()

        adapter = RecommendQuestionRVAdapter { item ->
            goToChat(item.text, prefillId = item.id)
        }
        binding.recommendQuestionRv.adapter = adapter
        recommendViewModel.fetch()

        binding.chatStartButton.setOnClickListener {
            goToChat()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun goToChat(prefill: String? = null,  prefillId: Long? = null) {
        val fragment = ChatFragment().apply {
            arguments = Bundle().apply {
                if (!prefill.isNullOrBlank()) putString("prefill_question", prefill)
                if (prefillId != null) putLong("prefill_question_id", prefillId)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.main_frm, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setupCharacterObserver() {
    viewModel.characterLevel.observe(viewLifecycleOwner) { data ->
        data?.let {
            // level 혹은 currentLevel 사용 (GET /me 에서는 level)
            binding.homeCharacterLevelTv.text = " ${it.level ?: it.currentLevel ?: 0}"

            // 캐릭터 이미지 로드
            Glide.with(this)
                .load(it.imageUrl)
                .error(R.drawable.ic_character)
                .into(binding.characterIv)

            // 이미지 미리 불러오기 (다른 화면 이동 시 즉시 표시 위함)
            Glide.with(this)
                .load(it.imageUrl)
                .preload()
        }
    }
}

    private fun setupRecommendObserver() {
        recommendViewModel.questions.observe(viewLifecycleOwner) { apiQuestions ->
            val domainQuestions = apiQuestions.map { apiQuestion ->
                RecommendQuestion(
                    id = apiQuestion.userQuestionId,
                    text = apiQuestion.content,
                    category = mapCategory(apiQuestion.tag)
                )
            }
            adapter.submitList(domainQuestions)
        }
    }

    private fun mapCategory(tag: String?): Category {
        return when (tag?.trim()?.uppercase()) {
            "REFLECTION" -> Category.REFLECTION
            "RELATION", "RELATIONSHIP" -> Category.RELATIONSHIP
            "CAREER" -> Category.CAREER
            "EMOTION" -> Category.EMOTION
            "ELSE", "ETC" -> Category.ETC
            else -> Category.ETC
        }
    }
}
