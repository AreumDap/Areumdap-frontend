package com.example.areumdap.RVAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.areumdap.UI.record.data.InsightDto
import com.example.areumdap.databinding.ItemFoundCardBinding



class DiscoveryRVAdapter : ListAdapter<InsightDto, DiscoveryRVAdapter.VH>(DIFF) {
    inner class VH(private val binding: ItemFoundCardBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: InsightDto){
            binding.fountCardContentTv.text = item.content
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType:Int): VH{
        val binding = ItemFoundCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder:VH, position:Int){
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF = object: DiffUtil.ItemCallback<InsightDto>(){
            override fun areItemsTheSame(o: InsightDto, n: InsightDto) = o.insightId == n.insightId
            override fun areContentsTheSame(o: InsightDto, n: InsightDto) = o == n

        }

    }

}


