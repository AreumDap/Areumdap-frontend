package com.example.areumdap.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.areumdap.databinding.ItemChatAiBinding
import com.example.areumdap.databinding.ItemChatMeBinding
import com.example.areumdap.data.model.ChatMessage
import com.example.areumdap.data.model.Sender
import com.example.areumdap.data.model.Status

class ChatMessageRVAdapter(
    private val onAiLongClick : (View, ChatMessage) ->Unit
) : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DIFF) {

    private var lastProfileIndex: Int = -1

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
            AiVH(binding, onAiLongClick)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = getItem(position)

        val lastProfileIndex = findLastAiIndexPreferTyping(currentList)
        val showProfile =
            msg.sender == Sender.AI &&
            position == lastProfileIndex &&
            (msg.status == Status.SENT || msg.status == Status.TYPING)

        when (holder) {
            is MeVH -> holder.bind(msg)
            is AiVH -> holder.bind(msg, showProfile)
        }
    }

    override fun onCurrentListChanged(
        previousList: MutableList<ChatMessage>,
        currentList: MutableList<ChatMessage>
    ) {
        val newIndex = findLastAiIndexPreferTyping(currentList)
        if (lastProfileIndex != -1 && lastProfileIndex < itemCount) {
            notifyItemChanged(lastProfileIndex)
        }
        if (newIndex != -1 && newIndex < itemCount) {
            notifyItemChanged(newIndex)
        }
        lastProfileIndex = newIndex
    }

    private fun findLastAiIndexPreferTyping(list: List<ChatMessage>): Int {
        for (i in list.size - 1 downTo 0) {
            val msg = list[i]
            if (msg.sender == Sender.AI && msg.status == Status.TYPING) return i
        }
        for (i in list.size - 1 downTo 0) {
            val msg = list[i]
            if (msg.sender == Sender.AI && msg.status == Status.SENT) return i
        }
        return -1
    }

    class MeVH(private val binding: ItemChatMeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: ChatMessage) {
            binding.chatTv.text = msg.text
        }
    }

    class AiVH(private val binding: ItemChatAiBinding, private val onLongClick: (View, ChatMessage) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: ChatMessage, showProfile:Boolean) {
            binding.chatTv.text = msg.text
            binding.aiProfileIv.visibility = if(showProfile) View.VISIBLE else View.INVISIBLE

            binding.chatTv.setOnLongClickListener {
                onLongClick(it, msg)
                true
            }
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

