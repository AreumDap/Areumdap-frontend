package com.example.areumdap.RVAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.areumdap.databinding.ItemCharacterHistoryBinding

class CharacterHistoryRVAdapter(private val items: List<Int>) :
    RecyclerView.Adapter<CharacterHistoryRVAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemCharacterHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(imgRes: Int) {
            binding.itemCharacterIv.setImageResource(imgRes)
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