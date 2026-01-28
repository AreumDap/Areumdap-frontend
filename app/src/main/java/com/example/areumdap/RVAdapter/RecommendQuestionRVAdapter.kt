package com.example.areumdap.RVAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.layout.Layout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.areumdap.databinding.ItemRecommendQuestionBinding
import com.example.areumdap.domain.model.RecommendQuestion

class RecommendQuestionRVAdapter(
    private val onClick: (RecommendQuestion) -> Unit
) : ListAdapter<RecommendQuestion, RecommendQuestionRVAdapter.ViewHolder>(diffUtil) {
    companion object{
        private val diffUtil = object: DiffUtil.ItemCallback<RecommendQuestion>(){
            override fun areItemsTheSame(
                oldItem: RecommendQuestion,
                newItem: RecommendQuestion
            ): Boolean=oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: RecommendQuestion,
                newItem: RecommendQuestion
            ): Boolean = oldItem == newItem
        }
    }
    inner class ViewHolder(private val binding: ItemRecommendQuestionBinding) : RecyclerView.ViewHolder(binding.root){
            fun bind(item: RecommendQuestion){
                binding.questionTv.text = item.text
             binding.categoryTv.text = item.category.label
                binding.categoryIv.setImageResource(item.category.iconRes)
                val color = androidx.core.content.ContextCompat.getColor(
                    binding.root.context,
                    item.category.colorRes
                )
                binding.categoryTv.setTextColor(color)


                binding.root.setOnClickListener { onClick(item) }
            }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(ItemRecommendQuestionBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}