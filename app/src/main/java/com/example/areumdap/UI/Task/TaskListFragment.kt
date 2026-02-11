package com.example.areumdap.UI.Task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.areumdap.data.source.RetrofitClient
import com.example.areumdap.R
import com.example.areumdap.adapter.CharacterTaskRVAdapter
import com.example.areumdap.UI.Character.CharacterViewModel
import com.example.areumdap.UI.Character.CharacterXpFragment
import com.example.areumdap.UI.auth.PopUpDialogFragment
import com.example.areumdap.data.repository.CharacterViewModelFactory
import com.example.areumdap.databinding.FragmentTaskListBinding

class TaskListFragment: Fragment() {
    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CharacterViewModel by activityViewModels {
        CharacterViewModelFactory(RetrofitClient.service)
    }

    private lateinit var taskAdapter: CharacterTaskRVAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        
        // 팝업에서 미션 완료/이미 완료 등의 신호를 받으면 데이터 새로고침
        childFragmentManager.setFragmentResultListener("missionCompleted", viewLifecycleOwner) { requestKey, bundle ->
            
            val missionId = bundle.getInt("missionId", -1)
            val reward = bundle.getInt("reward", 0)

            // 1. 로컬 리스트에서 즉시 제거 (Optimistic UI)
            if (missionId != -1) {
                // 완료된 미션 ID를 뷰모델 블랙리스트에 추가
                viewModel.addCompletedMission(missionId)

                // 현재 어댑터에 있는 데이터 복사
                val currentList = taskAdapter.getCurrentList().toMutableList()
                // 완료된 미션 제거
                val updatedList = currentList.filter { it.missionId != missionId }
                
                // 어댑터 갱신 (즉시 사라짐 효과)
                taskAdapter.updateData(updatedList)
            }

            // 2. XP 반영
            if (reward > 0) {
                viewModel.addXp(reward)
                 Toast.makeText(context, "+${reward} XP", Toast.LENGTH_SHORT).show()
            } else {
                 // Reward is 0 or less
            }

            // 3. 서버 데이터 갱신 (지연 호출 - 데이터 정합성 맞춤)
            view?.postDelayed({
                viewModel.fetchMyCharacter()
            }, 1000)
        }
    }

    private fun setupRecyclerView() {
        taskAdapter = CharacterTaskRVAdapter(arrayListOf())
        
        taskAdapter.itemClickListener = { missionItem ->
            val dataTaskFragment = DataTaskFragment()
            val bundle = Bundle()
            bundle.putInt("missionId", missionItem.missionId)
            bundle.putInt("reward", missionItem.reward)
            bundle.putString("title", missionItem.title)
            bundle.putString("tag", missionItem.tag)
            bundle.putBoolean("showTip", true)
            bundle.putBoolean("isCompleted", false)
            bundle.putBoolean("isTransparent", true)
            dataTaskFragment.arguments = bundle
            
            dataTaskFragment.show(childFragmentManager, "DataTaskDialog")
        }

        // 진화 안내 다이얼로그 연동
        taskAdapter.onEvolutionRequiredListener = {
            val dialog = PopUpDialogFragment.newInstance(
                title = "목표 XP는 모두 달성했어요!\n과제를 진행하려면\n캐릭터를 먼저 성장시켜주세요.",
                subtitle = "",
                leftBtn = "돌아가기",
                rightBtn = "성장시키기"
            )

            dialog.setCallback(object : PopUpDialogFragment.MyDialogCallback {
                override fun onConfirm() {
                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.main_frm, CharacterXpFragment())
                    transaction.addToBackStack(null)
                    transaction.commit()
                }
            })

            dialog.show(childFragmentManager, "EvolutionDialog")
        }

        binding.taskListRv.apply {
            adapter = taskAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun setupObservers() {
        // 캐릭터 레벨 데이터 관찰
        viewModel.characterLevel.observe(viewLifecycleOwner) {
            filterAndRefreshList()
        }

        // 선택된 태그 관찰
        viewModel.selectedTag.observe(viewLifecycleOwner) {
            filterAndRefreshList()
        }
    }

    private fun filterAndRefreshList() {
        val levelData = viewModel.characterLevel.value ?: return
        val currentTag = viewModel.selectedTag.value

        //  활성 과제 필터링
        var filteredMissions = levelData.missions?.filter { mission ->
            val status = mission.status?.uppercase() ?: ""
            status != "COMPLETED" && status != "DONE" && mission.isCompleted != true && (mission.dDay ?: 0) >= 0 && !viewModel.isMissionCompleted(mission.missionId)
        } ?: emptyList()

        // 태그 필터링
        if (currentTag != "전체") {
            filteredMissions = filteredMissions.filter { it.tag == currentTag }
        }

        // 진화 준비 상태 확인
        val isEvolutionReady = levelData.currentXp >= levelData.maxXp && levelData.maxXp > 0
        taskAdapter.isEvolutionReady = isEvolutionReady

        taskAdapter.updateData(filteredMissions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}