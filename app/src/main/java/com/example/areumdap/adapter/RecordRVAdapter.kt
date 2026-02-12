package com.example.areumdap.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.areumdap.databinding.ItemChatRecordBinding
import com.example.areumdap.data.model.RecordItem

class RecordRVAdapter(
    private val onItemClick: (RecordItem) -> Unit,
    private val onStarClick: (RecordItem, Boolean) -> Unit
) : ListAdapter<RecordItem, RecordRVAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(
        private val binding: ItemChatRecordBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RecordItem) = with(binding) {
            // 카테고리
            sumCatTv.text = item.category.label
            sumCatIv.setImageResource(item.category.iconRes)
            sumCatTv.setTextColor(ContextCompat.getColor(root.context, item.category.colorRes))

            // 내용
            recTitleTv.text = item.title
            recContentTv.text = ellipsize35(item.summary)
            dateTv.text = item.dateText

            btnFavorite.setImageResource(
                if (item.isStarred) com.example.areumdap.R.drawable.ic_favorite_on
                else com.example.areumdap.R.drawable.ic_star
            )


            // 카드 클릭
            root.setOnClickListener { onItemClick(item) }

            btnFavorite.setOnClickListener {
                val newState = !item.isStarred
                onStarClick(item, newState)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(ItemChatRecordBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<RecordItem>() {
            override fun areItemsTheSame(oldItem: RecordItem, newItem: RecordItem) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: RecordItem, newItem: RecordItem) =
                oldItem == newItem
        }
    }
    // 레포트 카드 35글자 이상 문장 ...으로 교체
    private fun ellipsize35(text:String):String{
        val t = text.trim()
        return if(t.length <35) t else t.take(35) +"..."
    }
}
