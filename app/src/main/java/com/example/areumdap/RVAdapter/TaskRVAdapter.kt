package com.example.areumdap.RVAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.areumdap.databinding.ItemArchiveTaskBinding

class TaskRVAdapter(private val questionList: ArrayList<String>):
    RecyclerView.Adapter<TaskRVAdapter.ViewHolder>(){

        var itemClickListener: ((String) -> Unit)? = null
    inner class ViewHolder(val binding: ItemArchiveTaskBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val binding = ItemArchiveTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.itemArchiveTaskTv.text = questionList[position]

        holder.binding.taskCardView.setOnClickListener {
            val currentX = it.translationX

            if (currentX < 0f) {
                it.animate().translationX(0f).setDuration(200).start()
            } else {
                // 정상 클릭 처리
                itemClickListener?.invoke(questionList[position])
            }
        }

        holder.binding.taskTrashIv.setOnClickListener {
            removeItem(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int = questionList.size

    fun removeItem(position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            questionList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, questionList.size)
        }
    }
}