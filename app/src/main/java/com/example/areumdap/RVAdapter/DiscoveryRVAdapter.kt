package com.example.areumdap.RVAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.areumdap.databinding.ItemFoundCardBinding

data class DiscoveryItem(
    val id: Long,
    val text: String
)

class DiscoveryRVAdapter : ListAdapter<DiscoveryItem, DiscoveryRVAdapter.VH>(DIFF) {
    inner class VH(private val binding: ItemFoundCardBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DiscoveryItem){
            binding.fountCardContentTv.text = item.text
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
        private val DIFF = object: DiffUtil.ItemCallback<DiscoveryItem>(){
            override fun areItemsTheSame(o: DiscoveryItem, n: DiscoveryItem) = o.id == n.id
            override fun areContentsTheSame(o: DiscoveryItem, n: DiscoveryItem) = o == n

        }

    }

}


