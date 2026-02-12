package com.example.areumdap.UI.Chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.areumdap.UI.auth.LoadingDialogFragment
import com.example.areumdap.UI.auth.MainActivity
import com.example.areumdap.adapter.TaskGuideVPAdapter
import com.example.areumdap.data.repository.MissionRepository
import com.example.areumdap.data.source.RetrofitClient
import com.example.areumdap.databinding.FragmentTaskGuideBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.math.abs

class TaskGuideFragment : Fragment() {

    private var _binding: FragmentTaskGuideBinding? = null
    private val binding get() = _binding!!

    private var indicatorMediator: TabLayoutMediator? = null
    private var pageCallback: ViewPager2.OnPageChangeCallback? = null

    private val missionViewModel: MissionViewModel by viewModels {
        val repo = MissionRepository(RetrofitClient.missionApi)
        MissionViewModelFactory(repo)
    }

    private lateinit var pagerAdapter: TaskGuideVPAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskGuideBinding.inflate(inflater, container, false)

        pagerAdapter = TaskGuideVPAdapter()
        binding.vpTasks.adapter = pagerAdapter
        setupPagerPreview()

        missionViewModel.missions.observe(viewLifecycleOwner) { missions ->
            pagerAdapter.submitList(missions)
            if (missions.isNotEmpty()) {
                binding.vpTasks.setCurrentItem(0, false)
            }
            setupIndicator(missions.size)
        }

        missionViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                LoadingDialogFragment()
                    .show(parentFragmentManager, "LoadingDialog_TaskGuide")
            } else {
                (parentFragmentManager.findFragmentByTag("LoadingDialog_TaskGuide") as? LoadingDialogFragment)
                    ?.dismissAllowingStateLoss()
            }
        }

        missionViewModel.error.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        val threadId = requireArguments().getLong("threadId", -1L)
        if (threadId == -1L) {
            Toast.makeText(requireContext(), "threadId 없음", Toast.LENGTH_SHORT).show()
        } else {
            missionViewModel.createMissions(threadId)
        }

        binding.btnTaskPage.setOnClickListener {
            (requireActivity() as MainActivity).goToCharacterFragment()
        }

        binding.btnHome.setOnClickListener {
            (requireActivity() as MainActivity).goToHome()
        }

        return binding.root
    }

    private fun setupPagerPreview() {
        val density = resources.displayMetrics.density
        val sidePadding = (40f * density).toInt()
        val pageMargin = (12f * density).toInt()

        val pageTransformer = CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(pageMargin))
            addTransformer { page, position ->
                val scale = 0.92f + (1 - abs(position)) * 0.08f
                page.scaleY = scale
            }
        }

        binding.vpTasks.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 3
            setPadding(sidePadding, 0, sidePadding, 0)
            setPageTransformer(pageTransformer)
        }

        (binding.vpTasks.getChildAt(0) as? RecyclerView)?.apply {
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            clipToPadding = false
            clipChildren = false
        }
    }
    private fun setupIndicator(count: Int) {
        // 기존 연결/콜백 정리
        indicatorMediator?.detach()
        indicatorMediator = null
        pageCallback?.let { binding.vpTasks.unregisterOnPageChangeCallback(it) }
        pageCallback = null

        // 0~1개면 점 굳이 안 보여도 되면 숨김 처리
        if (count <= 1) {
            binding.pageIndicator.visibility = View.GONE
            return
        }
        binding.pageIndicator.visibility = View.VISIBLE

        indicatorMediator = TabLayoutMediator(binding.pageIndicator, binding.vpTasks) { tab, _ ->
            tab.setCustomView(com.example.areumdap.R.layout.item_indicator_dot)
        }.also { it.attach() }

        fun updateDots(selectedPos: Int) {
            for (i in 0 until binding.pageIndicator.tabCount) {
                val dot = binding.pageIndicator.getTabAt(i)
                    ?.customView
                    ?.findViewById<ImageView>(/* id = */ com.example.areumdap.R.id.dot)
                    ?: continue

                dot.setImageResource(
                    if (i == selectedPos) com.example.areumdap.R.drawable.indicator_dot_selected
                    else com.example.areumdap.R.drawable.indicator_dot_unselected
                )
            }
        }

        updateDots(binding.vpTasks.currentItem)

        pageCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
            }
        }.also { binding.vpTasks.registerOnPageChangeCallback(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


