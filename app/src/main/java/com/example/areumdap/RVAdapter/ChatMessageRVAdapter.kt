package com.example.areumdap.RVAdapter

import android.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.areumdap.databinding.ItemChatAiBinding
import com.example.areumdap.databinding.ItemChatMeBinding
import com.example.areumdap.domain.model.ChatMessage
import com.example.areumdap.domain.model.Sender

class ChatMessageRVAdapter :
    ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DIFF) {

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).sender == Sender.ME) TYPE_ME else TYPE_AI
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == TYPE_ME) {
            val binding = ItemChatMeBinding.inflate(inflater, parent, false)
            MeVH(binding)
        } else {
            val binding = ItemChatAiBinding.inflate(inflater, parent, false)
            AiVH(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = getItem(position)

        val prev = if(position >0) getItem(position -1) else null
        val showProfile = prev?.sender != msg.sender

        when (holder) {
            is MeVH -> holder.bind(msg)
            is AiVH -> holder.bind(msg, showProfile)
        }
    }

    class MeVH(private val binding: ItemChatMeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: ChatMessage) {
            binding.chatTv.text = msg.text
        }
    }

    class AiVH(private val binding: ItemChatAiBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: ChatMessage, showProfile:Boolean) {
            binding.chatTv.text = msg.text
            binding.aiProfileIv.visibility = if(!showProfile) View.VISIBLE else View.INVISIBLE


        }
    }

    companion object {
        private const val TYPE_ME = 1
        private const val TYPE_AI = 0

        private val DIFF = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(old: ChatMessage, new: ChatMessage) = old.id == new.id
            override fun areContentsTheSame(old: ChatMessage, new: ChatMessage) = old == new
        }
    }
}
