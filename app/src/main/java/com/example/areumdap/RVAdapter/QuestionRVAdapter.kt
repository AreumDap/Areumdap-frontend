package com.example.areumdap.RVAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.areumdap.Task.QuestionItem
import com.example.areumdap.UI.PopUpDialogFragment
import com.example.areumdap.databinding.ItemQuestionTaskBinding

class QuestionRVAdapter (private val questionList: ArrayList<QuestionItem>):
RecyclerView.Adapter<QuestionRVAdapter.ViewHolder>(){

    inner class ViewHolder(val binding: ItemQuestionTaskBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val binding = ItemQuestionTaskBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = questionList[position]
        // 테스트용
        holder.binding.itemQuestionTaskTv.text = item.content

        holder.binding.taskCardView.setOnClickListener {
            if(it.translationX < 0f){
                it.animate().translationX(0f).setDuration(200).start()
            } else{
                // 닫혀있을 때 클릭 철
            }
        }

        holder.binding.taskTrashIv.setOnClickListener {
            val activity = holder.binding.root.context as androidx.fragment.app.FragmentActivity

            val dialog = PopUpDialogFragment.newInstance(
                title = "저장한 질문을 정말로 삭제하겠어요?\n삭제한 리스트는 되돌릴 수 없어요.",
                subtitle = "",
                leftBtn = "이전으로",
                rightBtn = "삭제하기"
            )

            dialog.setCallback(object : PopUpDialogFragment.MyDialogCallback{
                override fun onConfirm(){
                    val position = holder.adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        removeItem(position)
                    }
                }
            })

            dialog.show(activity.supportFragmentManager, "PopUpDialog")
        }
    }

    override fun getItemCount(): Int = questionList.size
    //아이템 삭제
    fun removeItem(position: Int){
        questionList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, questionList.size)
    }

    fun updateData(newData: List<QuestionItem>) {
        questionList.clear()
        questionList.addAll(newData)
        notifyDataSetChanged()
    }
}