package com.example.areumdap.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.areumdap.databinding.ItemRecommendQuestionBinding
import com.example.areumdap.data.model.RecommendQuestion

class RecommendQuestionRVAdapter(
    private val onClick: (RecommendQuestion) -> Unit
) : ListAdapter<RecommendQuestion, RecommendQuestionRVAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(
        private val binding: ItemRecommendQuestionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RecommendQuestion) = with(binding) {
            questionTv.text = item.text
            categoryTv.text = item.tag.label
            categoryIv.setImageResource(item.tag.iconRes)

            val color = ContextCompat.getColor(root.context, item.tag.colorRes)
            categoryTv.setTextColor(color)

            root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(ItemRecommendQuestionBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<RecommendQuestion>() {
            override fun areItemsTheSame(oldItem: RecommendQuestion, newItem: RecommendQuestion) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: RecommendQuestion, newItem: RecommendQuestion) =
                oldItem == newItem
        }
    }
}
