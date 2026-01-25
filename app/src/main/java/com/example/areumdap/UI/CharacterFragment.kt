package com.example.areumdap.UI

import android.os.Bundle
import android.view.LayoutInflater
import com.example.areumdap.R
import android.widget.TextView
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.areumdap.VPAdapter.TaskPageVPAdapter
import com.example.areumdap.databinding.FragmentCharacterBinding

class CharacterFragment : Fragment() {
    private var _binding: FragmentCharacterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCharacterBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        val hasTask = false

        val taskAdapter = TaskPageVPAdapter(this, hasTask)
        binding.characterVp.adapter = taskAdapter

        // 사용자가 페이지를 넘기지 못하게 하는것
        binding.characterVp.isUserInputEnabled = false

        // 카테고리 목록 정의
        val categories = listOf("전체", "진로","관계","자기성찰","감정","성장","기타")

        val spinnerAdapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.item_spinner_text, // 스피너 겉모양
            R.id.tv_spinner_item,      // 스피너 겉모양 안의 TextView ID
            categories
        ) {
            // 드롭다운(펼쳐졌을 때) 화면 설정
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                // 부모의 것을 쓰지 않고 직접 레이아웃을 가져옵니다 (튕김 방지)
                val inflater = LayoutInflater.from(context)
                val view = convertView ?: inflater.inflate(R.layout.item_spinner_dropdown, parent, false)

                // item_spinner_dropdown.xml 내부의 TextView ID를 정확히 찾아 연결합니다
                val tv = view.findViewById<TextView>(R.id.tv_dropdown_item)
                tv.text = getItem(position)

                return view
            }
        }
        binding.taskFilterSp.adapter = spinnerAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}