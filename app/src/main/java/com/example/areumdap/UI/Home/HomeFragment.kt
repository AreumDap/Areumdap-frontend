package com.example.areumdap.UI.Home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.areumdap.Data.api.ChatbotApiService
import com.example.areumdap.Data.repository.ChatbotRepository
import com.example.areumdap.Network.RetrofitClient
import com.example.areumdap.R
import com.example.areumdap.RVAdapter.RecommendQuestionRVAdapter
import com.example.areumdap.UI.Character.CharacterViewModel
import com.example.areumdap.UI.Character.CharacterViewModelFactory
import com.example.areumdap.UI.Chat.ChatFragment
import com.example.areumdap.UI.Chat.data.ChatViewModel
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
                val api = RetrofitClient.create(ChatbotApiService::class.java)
                return RecommendQuestionViewModel(ChatbotRepository(api)) as T
            }
        }
    }

    private val chatViewModel: ChatViewModel by activityViewModels()

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
            goToChat(item.text, prefillId = item.id, prefillTag = item.tag.name)
        }
        binding.recommendQuestionRv.adapter = adapter
        recommendViewModel.fetch()

        binding.chatStartButton.setOnClickListener {
            chatViewModel.startChat(content = "", userQuestionId = null)
            goToChat("", null, null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun goToChat(prefill: String? = null, prefillId: Long? = null, prefillTag: String? = null) {
        val fragment = ChatFragment().apply {
            arguments = Bundle().apply {
                if (!prefill.isNullOrBlank()) putString("prefill_question", prefill)
                if (prefillId != null) putLong("prefill_question_id", prefillId)
                if (!prefillTag.isNullOrBlank()) putString("prefill_tag", prefillTag)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.main_frm, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setupCharacterObserver() {
        viewModel.characterLevel.observe(viewLifecycleOwner) { data ->
            data ?: return@observe

            binding.homeCharacterLevelTv.text = " ${data.level ?: data.currentLevel ?: 0}"

            Glide.with(this)
                .load(data.imageUrl)
                .placeholder(R.drawable.ic_character)
                .error(R.drawable.ic_character)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding.characterIv)

            Glide.with(this)
                .load(data.imageUrl)
                .preload()
        }
    }

    private fun setupRecommendObserver() {
        recommendViewModel.questions.observe(viewLifecycleOwner) { apiQuestions ->
            val safeItems = apiQuestions ?: emptyList()
            val domainQuestions = safeItems.map { apiQuestion ->
                RecommendQuestion(
                    id = apiQuestion.userQuestionId,
                    text = apiQuestion.content,
                    tag = mapCategory(apiQuestion.tag)
                )
            }
            adapter.submitList(domainQuestions)
        }
    }

    private fun mapCategory(tag: String?): Category {
        val trimmed = tag?.trim()
        val upper = trimmed?.uppercase()
        return when (upper) {
            "REFLECTION", "SELF_REFLECTION" -> Category.REFLECTION
            "RELATION", "RELATIONSHIP" -> Category.RELATIONSHIP
            "CAREER" -> Category.CAREER
            "EMOTION" -> Category.EMOTION
            "ELSE", "ETC" -> Category.ETC
            else -> when (trimmed) {
                "자기성찰" -> Category.REFLECTION
                "관계" -> Category.RELATIONSHIP
                "진로" -> Category.CAREER
                "감정" -> Category.EMOTION
                "기타" -> Category.ETC
                else -> Category.ETC
            }
        }
    }
}
