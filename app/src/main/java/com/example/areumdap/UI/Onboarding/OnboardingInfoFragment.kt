package com.example.areumdap.UI.Onboarding

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.areumdap.data.api.OnboardingRequest
import com.example.areumdap.data.source.RetrofitClient
import com.example.areumdap.data.api.UserApi
import com.bumptech.glide.Glide
import com.example.areumdap.R
import com.example.areumdap.UI.Character.CharacterUiState
import com.example.areumdap.databinding.FragmentOnboardingInfoBinding

import com.example.areumdap.UI.Character.CharacterViewModel
import com.example.areumdap.data.repository.CharacterViewModelFactory
import kotlinx.coroutines.launch
import kotlin.getValue
import kotlin.jvm.java


class OnboardingInfoFragment: Fragment() {

    private var _binding: FragmentOnboardingInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OnboardingViewModel by activityViewModels()

    // 캐릭터 생성 결과 저장
    private var createdCharacterId: Int? = null
    private var createdImageUrl: String? = null

    // 캐릭터 생성 ViewModel
    private val characterViewModel: CharacterViewModel by viewModels {
        CharacterViewModelFactory(RetrofitClient.service)
    }

    // 유저 온보딩 저장
    private val userApi: UserApi by lazy {
        RetrofitClient.create(UserApi::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 시작 화면 진입 시 하단 버튼을 즉시 활성화(pink1) 상태로
        viewModel.isKeywordSelected.value = true

        // FINAL Fragment 진입 시 이미 생성된 캐릭터 정보 복원
        createdCharacterId = viewModel.createdCharacterId
        createdImageUrl = viewModel.createdImageUrl
        if (!createdImageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(createdImageUrl)
                .error(R.drawable.img_character_egg)
                .into(binding.ivCharacter)
            binding.ivCharacter.visibility = View.VISIBLE
        }

        // 캐릭터 생성 상태 관찰
        observeCharacterCreation()

        // infoTextStep 관찰해서 텍스트 업데이트
        viewModel.infoTextStep.observe(viewLifecycleOwner) { step ->
            when (step) {
                0 -> showText1() // "아름이가 태어났어요"
                1 -> showText2() // "서로를 알아가기 위해..."
                2 -> showText3() // "좋아요! 앞으로 ..."
                3 -> showText4() // "아름이는 oo님이..."
                4 -> saveOnboardingAndNavigate(createdCharacterId, createdImageUrl) // 온보딩 저장 + 메인 이동
            }
        }
    }

    private fun showText1() {
        binding.tvTitle.text = Html.fromHtml(
            "앞선 당신의 이야기를 통해<br>당신을 닮은 <b>아름이</b>가 태어났어요!",
            Html.FROM_HTML_MODE_LEGACY
        )
        // 캐릭터 생성 API 호출 (INFO 화면 진입 시 최초 1회)
        createCharacter()
    }

    private fun showText2() {
        binding.tvTitle.text = Html.fromHtml(
            "서로를 알아가기 위해<br>당신을 뭐라고 부르면 좋을까요?",
            Html.FROM_HTML_MODE_LEGACY
        )
    }

    private fun showText3() {
        val nickname = viewModel.nickname.value
        binding.tvTitle.text = Html.fromHtml(
            "좋아요!<br>앞으로 <b>${nickname}</b>님과 <b>아름이</b>의<br>여정이 시작됩니다.",
            Html.FROM_HTML_MODE_LEGACY
        )
    }

    private fun showText4() {
        val nickname = viewModel.nickname.value
        binding.tvTitle.text = Html.fromHtml(
            "<b>아름이</b>는 <b>${nickname}</b>님이<br>나를 온전히 알게 되는 그날까지<br>함께합니다.",
            Html.FROM_HTML_MODE_LEGACY
        )
    }

    // 캐릭터 생성
    private fun createCharacter() {
        val season = viewModel.selectedSeason.value

        if (season.isNullOrEmpty()) {
            showError("계절 정보가 없습니다.")
            return
        }

        // 이미 캐릭터가 생성된 경우 재호출 방지
        if (viewModel.createdCharacterId != null) {
            return
        }

        val currentState = characterViewModel.uiState.value
        if (currentState is CharacterUiState.Loading || currentState is CharacterUiState.Success) {
            return
        }

        // 캐릭터 생성 API 호출
        characterViewModel.createCharacter(
            season = season,
            keywords = emptyList()
        )
    }

    // 캐릭터 생성 상태 관찰
    private fun observeCharacterCreation() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                characterViewModel.uiState.collect { state ->

                    when (state) {
                        is CharacterUiState.Loading -> {
                            showLoading()
                        }
                        is CharacterUiState.Success -> {
                            createdCharacterId = state.characterId
                            createdImageUrl = state.imageUrl
                            // ViewModel에 저장 (FINAL Fragment에서 복원용)
                            viewModel.createdCharacterId = state.characterId
                            viewModel.createdImageUrl = state.imageUrl
                            // 계절별 알 이미지 로드
                            if (!state.imageUrl.isNullOrEmpty()) {
                                Glide.with(this@OnboardingInfoFragment)
                                    .load(state.imageUrl)
                                    .error(R.drawable.img_character_egg)
                                    .into(binding.ivCharacter)
                                binding.ivCharacter.visibility = View.VISIBLE
                            }
                            hideLoading()
                            characterViewModel.resetUiState()
                        }
                        is CharacterUiState.Error -> {
                            hideLoading()
                            showError(state.message)
                            characterViewModel.resetUiState()
                        }
                        CharacterUiState.Idle -> {}
                    }
                }
            }
        }
    }

    // 캐릭터 정보 저장
    private fun saveCharacterInfo(characterId: Int, imageUrl: String?) {
        requireActivity().getSharedPreferences("character", Context.MODE_PRIVATE)
            .edit()
            .putInt("character_id", characterId)
            .putString("image_url", imageUrl ?: "")
            .apply()
    }

    // 온보딩 저장
    private fun saveOnboardingAndNavigate(characterId: Int?, imageUrl: String?) {
        if (characterId == null) {
            showError("캐릭터 생성이 완료되지 않았습니다.")
            return
        }

        val nickname = viewModel.nickname.value ?: ""

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = userApi.saveOnboarding(OnboardingRequest(nickname = nickname))

                if (!response.isSuccessful) {
                }
                saveCharacterInfo(characterId, imageUrl)
                characterViewModel.resetUiState()
                viewModel.infoTextStep.value = -1
                hideLoading()
                (activity as? OnboardingActivity)?.navigateToMain()
            } catch (e: Exception) {
                // 예외 발생해도 캐릭터는 이미 생성되었으므로 메인으로 이동
                saveCharacterInfo(characterId, imageUrl)
                characterViewModel.resetUiState()
                viewModel.infoTextStep.value = -1
                hideLoading()
                (activity as? OnboardingActivity)?.navigateToMain()
            }
        }
    }

    private fun showLoading() {
        // 버튼 비활성화
        (activity as? OnboardingActivity)?.findViewById<View>(R.id.btn_next)?.isEnabled = false
    }

    private fun hideLoading() {
        // 버튼 활성화
        (activity as? OnboardingActivity)?.findViewById<View>(R.id.btn_next)?.isEnabled = true
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}