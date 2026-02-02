package com.example.areumdap.UI.record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.areumdap.RVAdapter.DiscoveryItem
import com.example.areumdap.RVAdapter.DiscoveryRVAdapter
import com.example.areumdap.databinding.FragmentRecordBinding
import com.example.areumdap.databinding.FragmentReportBinding

class ReportFragment: Fragment(){
    private var _binding : FragmentReportBinding?=null
    private val binding get() = _binding!!

    private val discoveryRVAdapter = DiscoveryRVAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReportBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.foundCardRv.apply{
            layoutManager = LinearLayoutManager(requireContext())
            adapter = discoveryRVAdapter
        }

        val rawTexts : List<String> = listOf(
            "지은님은 이것보다 저것을 추구하는 사람이에요.\n그것보단 이러이러한 걸 더 좋아해요.",
            "최대글자 확인 최대글자 확인 최대글자 확인 ...",
            "지은님은 이것보다 저것을 추구하는 사람이에요.\n그것보단 이러이러한 걸 더 좋아해요."
        )

        val fixed = rawTexts
            .filter{it.isNotBlank()}
            .take(5)
            .let{ list ->
                if(list.size >=2) list else(list+"")
            }

        discoveryRVAdapter.submitList(
            fixed.mapIndexed{idx, t-> DiscoveryItem(id = idx.toLong(), text = t)}
        )
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}