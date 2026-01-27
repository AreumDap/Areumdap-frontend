package com.example.areumdap.UI.Archive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.areumdap.VPAdapter.ArchiveVPAdapter
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

        return binding.root
    }
}