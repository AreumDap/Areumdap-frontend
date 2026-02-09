package com.example.areumdap.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.areumdap.data.model.MissionDto
import com.example.areumdap.databinding.ItemReportTaskBinding
import com.example.areumdap.UI.auth.Category

class ReportTaskRVAdapter(
    private val onClick : ((MissionDto) -> Unit)? = null
) : ListAdapter<MissionDto, ReportTaskRVAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(
        private val binding : ItemReportTaskBinding
    ) : RecyclerView.ViewHolder(binding.root){
        fun bind(item: MissionDto) = with(binding){
            val category = Category.fromServerTag(item.tag)
            repTaskCatIv.setImageResource(category.iconRes)
            repTaskTitleTv.text = item.title
            repTaskContentTv.text = item.content

            root.setOnClickListener { onClick?.invoke(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReportTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object{
        private val DIFF = object : DiffUtil.ItemCallback<MissionDto>(){
            override fun areItemsTheSame(oldItem: MissionDto, newItem: MissionDto): Boolean =
                oldItem.missionId == newItem.missionId

            override fun areContentsTheSame(oldItem: MissionDto, newItem: MissionDto): Boolean =
                oldItem == newItem
        }
    }

}