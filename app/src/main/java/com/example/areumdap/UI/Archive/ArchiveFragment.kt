package com.example.areumdap.UI.Archive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.areumdap.adapter.ArchiveVPAdapter
import com.example.areumdap.databinding.FragmentArchiveBinding
import com.google.android.material.tabs.TabLayoutMediator

class ArchiveFragment : Fragment(){
    private lateinit var binding : FragmentArchiveBinding

    private val information = arrayListOf("저장한 질문", "완료한 과제")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {

        binding = FragmentArchiveBinding.inflate(inflater, container, false)

        val archiveAdapter = ArchiveVPAdapter(this)
        binding.archiveVp.adapter = archiveAdapter

        TabLayoutMediator(binding.archiveTb, binding.archiveVp) { tab, position ->
            tab.text = information[position]
        }.attach()

        // 계절 테마 적용
        val prefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val season = prefs.getString("SEASON", "SPRING")
        val indicatorColor = when (season) {
            "SPRING" -> com.example.areumdap.R.color.pink2
            "SUMMER" -> com.example.areumdap.R.color.green2
            "FALL" -> com.example.areumdap.R.color.yellow2
            "WINTER" -> com.example.areumdap.R.color.blue2
            else -> com.example.areumdap.R.color.pink2
        }
        binding.archiveTb.setSelectedTabIndicatorColor(androidx.core.content.ContextCompat.getColor(requireContext(), indicatorColor))

        return binding.root
    }
}