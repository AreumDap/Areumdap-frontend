package com.example.areumdap.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.areumdap.R
import com.example.areumdap.RVAdapter.QuestionRVAdapter
import com.example.areumdap.databinding.FragmentQuestionBinding

class QuestionFragment : Fragment() {
    lateinit var binding: FragmentQuestionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        // CharacterFragment.kt 내 onViewCreated
        val categories = listOf("전체", "진로", "관계", "자기성찰", "감정", "성장", "기타")

        val spinnerAdapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.item_spinner_text, // 스피너 평소 모습
            R.id.tv_spinner_item,
            categories
        ) {
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_spinner_dropdown, parent, false)
                val tv = v.findViewById<TextView>(R.id.tv_dropdown_item)
                tv.text = getItem(position)
                return v
            }
        }

        binding.questionSp.adapter = spinnerAdapter

        val testData = arrayListOf("자기 성찰") // 아이템 하나만 나오도록 설정
        val questionRVAdapter = QuestionRVAdapter(testData)

        binding.questionListRv.apply {
            adapter = questionRVAdapter
            // 가로 스크롤 설정 (XML에 가로로 되어 있으므로)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }
}