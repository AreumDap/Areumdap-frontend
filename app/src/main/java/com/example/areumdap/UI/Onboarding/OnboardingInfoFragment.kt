package com.example.areumdap.UI.Onboarding

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.util.Log
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

        // 캐릭터 생성 상태 관찰
        observeCharacterCreation()

        // infoTextStep 관찰해서 텍스트 업데이트
        viewModel.infoTextStep.observe(viewLifecycleOwner) { step ->
            Log.d("CharacterAPI", "infoTextStep 변경: $step")
            when (step) {
                0 -> showText1() // "아름이가 태어났어요"
                1 -> showText2() // "서로를 알아가기 위해..."
                2 -> showText3() // "좋아요! 앞으로 ..."
                3 -> showText4() // "아름이는 oo님이..."
                4 -> createCharacter() // 캐릭터 생성
            }
        }
    }

    private fun showText1() {
        binding.tvTitle.text = Html.fromHtml(
            "앞선 당신의 이야기를 통해<br>당신을 닮은 <b>아름이</b>가 태어났어요!",
            Html.FROM_HTML_MODE_LEGACY
        )
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
        val keywords = viewModel.selectedKeywords.value ?: mutableListOf()
        val nickname = viewModel.nickname.value

        Log.d("CharacterAPI", "계절: $season")
        Log.d("CharacterAPI", "키워드: $keywords")

        if (season.isNullOrEmpty()) {
            Log.e("CharacterAPI", "계절 정보 없음!")
            showError("계절 정보가 없습니다.")
            return
        }

        if (nickname.isNullOrEmpty()) {
            showError("닉네임을 입력해주세요.")
            return
        }

        Log.d("CharacterAPI", "API 호출 시작: season=$season, keywords=$keywords")

        val currentState = characterViewModel.uiState.value
        if (currentState is CharacterUiState.Loading || currentState is CharacterUiState.Success) {
            return
        }

        // 캐릭터 생성 API 호출
        characterViewModel.createCharacter(
            season = season,
            keywords = emptyList()
        )

        Log.d("CharacterAPI", "characterViewModel.createCharacter() 호출 완료")
    }

    // 캐릭터 생성 상태 관찰
    private fun observeCharacterCreation() {
        Log.d("CharacterAPI", "observeCharacterCreation() 시작")

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                characterViewModel.uiState.collect { state ->
                    Log.d("CharacterAPI", "uiState 변경: $state")

                    when (state) {
                        is CharacterUiState.Loading -> {
                            Log.d("CharacterAPI", "캐릭터 생성 중...")
                            showLoading()
                        }
                        is CharacterUiState.Success -> {
                            Log.d("CharacterAPI", "성공! ID: ${state.characterId}, URL: ${state.imageUrl}")
                            saveOnboardingAndNavigate(state.characterId, state.imageUrl)
                        }
                        is CharacterUiState.Error -> {
                            Log.e("CharacterAPI", "에러: ${state.message}")
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
        Log.d("CharacterAPI", "캐릭터 정보 저장: ID=$characterId, URL=$imageUrl")

        requireActivity().getSharedPreferences("character", Context.MODE_PRIVATE)
            .edit()
            .putInt("character_id", characterId)
            .putString("image_url", imageUrl ?: "")
            .apply()
    }

    // 온보딩 저장
    private fun saveOnboardingAndNavigate(characterId: Int, imageUrl: String?) {
        val nickname = viewModel.nickname.value ?: ""

        Log.d("CharacterAPI", "=== 온보딩 저장 시작 ===")
        Log.d("CharacterAPI", "닉네임: $nickname")
        Log.d("CharacterAPI", "캐릭터 ID: $characterId")

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Log.d("CharacterAPI", "API 호출 중...")
                val response = userApi.saveOnboarding(OnboardingRequest(nickname = nickname))

                Log.d("CharacterAPI", "응답 코드: ${response.code()}")
                Log.d("CharacterAPI", "응답 성공 여부: ${response.isSuccessful}")

                if (!response.isSuccessful) {
                    Log.e("CharacterAPI", "온보딩 저장 응답 실패 (코드: ${response.code()}), 메인으로 이동 계속 진행")
                }

                // 캐릭터는 이미 생성 완료되었으므로 온보딩 저장 성공/실패 무관하게 메인으로 이동
                saveCharacterInfo(characterId, imageUrl)
                characterViewModel.resetUiState()
                viewModel.infoTextStep.value = -1
                hideLoading()
                (activity as? OnboardingActivity)?.navigateToMain()
            } catch (e: Exception) {
                Log.e("CharacterAPI", "예외 발생", e)
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
