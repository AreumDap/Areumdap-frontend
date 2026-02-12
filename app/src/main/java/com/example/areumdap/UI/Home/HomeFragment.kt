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
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.areumdap.data.api.ChatbotApiService
import com.example.areumdap.data.repository.ChatbotRepository
import com.example.areumdap.data.source.RetrofitClient
import com.example.areumdap.R
import com.example.areumdap.adapter.RecommendQuestionRVAdapter
import com.example.areumdap.UI.Character.CharacterViewModel
import com.example.areumdap.data.repository.CharacterViewModelFactory
import com.example.areumdap.UI.Chat.ChatFragment
import com.example.areumdap.UI.Chat.ChatViewModel
import com.example.areumdap.UI.Chat.RecommendQuestionViewModel
import com.example.areumdap.databinding.FragmentHomeBinding
import com.example.areumdap.UI.auth.Category
import com.example.areumdap.UI.auth.ToastDialogFragment
import com.example.areumdap.data.model.RecommendQuestion
import com.example.areumdap.data.repository.UserRepository
import com.example.areumdap.data.source.TokenManager
import kotlinx.coroutines.launch

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
            chatViewModel.startChat(
                content = item.text,
                userQuestionId = item.id
            )
            goToChat(item.text, prefillId = item.id, prefillTag = item.tag.name)
        }
        binding.recommendQuestionRv.adapter = adapter

        binding.titleTv.setOnClickListener {
            recommendViewModel.fetchAssignedQuestionsOnClick()
        }
        // 홈 진입 시: 배정 -> 조회 순으로 자동 로드
        recommendViewModel.fetchAssignedQuestionsOnHome()

        binding.chatStartButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                if (TokenManager.getUserNickname().isNullOrBlank()) {
                    runCatching { UserRepository.getProfile() }
                }
                chatViewModel.startChat(content = "", userQuestionId = null)
                goToChat("", null, null)
            }
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
            binding.recommendEmptyTv.visibility =
                if (domainQuestions.isEmpty()) View.VISIBLE else View.GONE
        }

        recommendViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            val loading = isLoading == true
            binding.recommendLoading.visibility = if (loading) View.VISIBLE else View.GONE
            binding.titleTv.isEnabled = !loading
            if (loading) {
                binding.recommendEmptyTv.visibility = View.GONE
            }
        }

        recommendViewModel.error.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                showCustomToast(message, isSuccess = false)
                binding.recommendEmptyTv.text = message
                binding.recommendEmptyTv.visibility = View.VISIBLE
            }
        }
    }

    private fun showCustomToast(message: String, isSuccess: Boolean = true) {
        if (!isAdded) return
        val iconRes = if (isSuccess) R.drawable.ic_success else R.drawable.ic_error
        val toast = ToastDialogFragment(message, iconRes)
        toast.show(requireActivity().supportFragmentManager, "CustomToast")
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
