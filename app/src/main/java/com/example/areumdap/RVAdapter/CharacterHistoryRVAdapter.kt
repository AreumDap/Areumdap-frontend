package com.example.areumdap.RVAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.areumdap.UI.Character.Data.HistoryItem
import com.example.areumdap.databinding.ItemCharacterHistoryBinding

class CharacterHistoryRVAdapter(private var items: List<HistoryItem>) :
    RecyclerView.Adapter<CharacterHistoryRVAdapter.ViewHolder>() {
        fun updateData(newItems: List<HistoryItem>?){
            if (newItems == null) return // null이 들어오면 함수 종료

            this.items = newItems
            notifyDataSetChanged()
    }
    inner class ViewHolder(private val binding: ItemCharacterHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HistoryItem) {
           /*Glide.with(binding.root.context)
                .load(item.imageUrl) // 서버에서 온 이미지 URL
                .into(binding.itemCharacterIv)*/
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