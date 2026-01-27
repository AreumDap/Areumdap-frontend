package com.example.areumdap.RVAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.areumdap.databinding.ItemQuestionTaskBinding

class QuestionRVAdapter (private val questionList: ArrayList<String>):
RecyclerView.Adapter<QuestionRVAdapter.ViewHolder>(){

    inner class ViewHolder(val binding: ItemQuestionTaskBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val binding = ItemQuestionTaskBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 테스트용
        holder.binding.itemQuestionTaskTv.text = questionList[position]

        holder.binding.taskCardView.setOnClickListener {
            if(it.translationX < 0f){
                it.animate().translationX(0f).setDuration(200).start()
            } else{
                // 닫혀있을 때 클릭 철
            }
        }

        holder.binding.taskTrashIv.setOnClickListener {
           removeItem(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int = questionList.size
    //아이템 삭제
    fun removeItem(position: Int){
        questionList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, questionList.size)
    }

}