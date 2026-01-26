package com.example.areumdap.RVAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.areumdap.databinding.ItemArchiveTaskBinding

class TaskRVAdapter(private val questionList: ArrayList<String>):
    RecyclerView.Adapter<TaskRVAdapter.ViewHolder>(){

    inner class ViewHolder(val binding: ItemArchiveTaskBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val binding = ItemArchiveTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) { holder.binding.itemArchiveTaskTv.text = questionList[position]

    }

    override fun getItemCount(): Int = questionList.size
}