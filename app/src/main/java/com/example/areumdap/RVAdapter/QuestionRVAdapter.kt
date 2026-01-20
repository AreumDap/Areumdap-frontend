package com.example.areumdap.RVAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.areumdap.databinding.ItemQuestionTaskBinding

class QuestionRVAdapter (private val questionList: ArrayList<String>):
RecyclerView.Adapter<QuestionRVAdapter.ViewHolder>(){

    inner class ViewHolder(val binding: ItemQuestionTaskBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val binding = ItemQuestionTaskBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 테스트용
        holder.binding.itemQuestionTaskTv.text = questionList[position]
    }

    override fun getItemCount(): Int = questionList.size
}