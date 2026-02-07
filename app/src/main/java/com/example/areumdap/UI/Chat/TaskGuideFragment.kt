package com.example.areumdap.UI.Chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.areumdap.Data.api.MissionApiService
import com.example.areumdap.Data.repository.MissionRepository
import com.example.areumdap.Network.RetrofitClient
import com.example.areumdap.R
import com.example.areumdap.UI.Character.CharacterFragment
import com.example.areumdap.UI.Chat.data.Mission
import com.example.areumdap.UI.Chat.data.MissionViewModel
import com.example.areumdap.UI.Home.HomeFragment
import com.example.areumdap.UI.Home.data.MissionViewModelFactory
import com.example.areumdap.VPAdapter.TaskGuideVPAdapter
import com.example.areumdap.VPAdapter.TaskPageVPAdapter
import com.example.areumdap.databinding.FragmentTaskGuideBinding


class TaskGuideFragment: Fragment() {
    private var _binding: FragmentTaskGuideBinding? = null
    private val binding get() = _binding!!

    private val missionViewModel : MissionViewModel by viewModels{
        val repo = MissionRepository(RetrofitClient.missionApi)
        MissionViewModelFactory(repo)
    }
    private lateinit var pagerAdapter : TaskGuideVPAdapter
    private var currentMissions: List<Mission> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTaskGuideBinding.inflate(inflater, container, false)
        pagerAdapter = TaskGuideVPAdapter()
        binding.vpTasks.adapter = pagerAdapter
        // observe
        missionViewModel.missions.observe(viewLifecycleOwner) { missions ->
            Log.d("TaskGuideFragment", "missions size=${missions.size}")
            pagerAdapter.submitList(missions)
            val firstTip = missions.firstOrNull()?.tip.orEmpty()
            binding.tipTv.text = firstTip
        }
        missionViewModel.loading.observe(viewLifecycleOwner) { /* 로딩 처리 */ }
        missionViewModel.error.observe(viewLifecycleOwner) { msg ->
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        // api 호출
        val threadId = requireArguments().getLong("threadId", -1L)
        if (threadId == -1L) {
            Log.w("TaskGuideFragment", "threadId is missing")
            Toast.makeText(requireContext(), "threadId 없음", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("TaskGuideFragment", "createMissions threadId=$threadId")
            missionViewModel.createMissions(threadId)
        }

        binding.btnTaskPage.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, CharacterFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.btnHome.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, HomeFragment())
                .addToBackStack(null)
                .commit()
        }
        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
