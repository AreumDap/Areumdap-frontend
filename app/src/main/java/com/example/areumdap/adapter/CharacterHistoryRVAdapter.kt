package com.example.areumdap.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.areumdap.data.model.HistoryItem
import com.example.areumdap.databinding.ItemCharacterHistoryBinding

class CharacterHistoryRVAdapter(private var items: List<HistoryItem>) :
    RecyclerView.Adapter<CharacterHistoryRVAdapter.ViewHolder>() {
        fun updateData(newItems: List<HistoryItem>?){
            if (newItems == null) return

            this.items = newItems
            notifyDataSetChanged()
    }
    inner class ViewHolder(private val binding: ItemCharacterHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HistoryItem) {
            val context = binding.root.context
            
            if (item.imageUrl.isNullOrEmpty()) {
                // 이미지가 없을 때
                binding.itemLayout.setBackgroundResource(com.example.areumdap.R.drawable.bg_character_history_empty)
                binding.itemCharacterIv.visibility = android.view.View.GONE
            } else {
                // 이미지가 있을 때
                binding.itemLayout.setBackgroundResource(com.example.areumdap.R.drawable.bg_character_history)
                binding.itemCharacterIv.visibility = android.view.View.VISIBLE
                
                Glide.with(context)
                    .load(item.imageUrl)
                    .error(com.example.areumdap.R.drawable.ic_character)
                    .into(binding.itemCharacterIv)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCharacterHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}