package com.example.areumdap.RVAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.areumdap.Task.QuestionItem
import com.example.areumdap.UI.PopUpDialogFragment
import com.example.areumdap.databinding.ItemQuestionTaskBinding

class QuestionRVAdapter (private val questionList: ArrayList<QuestionItem>):
RecyclerView.Adapter<QuestionRVAdapter.ViewHolder>(){

    var itemDeleteListener: ((Int) -> Unit)? = null
    var itemClickListener: ((QuestionItem) -> Unit)? = null

    inner class ViewHolder(val binding: ItemQuestionTaskBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val binding = ItemQuestionTaskBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = questionList[position]
        // 테스트용
        holder.binding.itemQuestionTaskTv.text = item.content
        holder.binding.taskCardView.translationX = 0f

        // 태그별 스타일 적용 (tag 필드가 QuestionItem에 있다고 가정)
        // QuestionItem에 tag 필드가 없으면 추가해야 함. TaskData.kt 확인 필요.
        // 일단 TaskRVAdapter와 동일한 로직 적용 시도.
        applyTagStyle(holder, item.tag)

        holder.binding.taskCardView.setOnClickListener {
            if(it.translationX < 0f){
                it.animate().translationX(0f).setDuration(200).start()
            } else{
                // 닫혀있을 때 클릭 처리
                itemClickListener?.invoke(item)
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
                        itemDeleteListener?.invoke(item.userQuestionId)
                    }
                }
            })

            dialog.show(activity.supportFragmentManager, "PopUpDialog")
        }
    }

    override fun getItemCount(): Int = questionList.size
    //아이템 삭제 대신 리스너 호출로 변경됨
    fun removeItem(position: Int){
        // viewModel에서 삭제 후 updateData가 호출되므로 여기서는 아무것도 안함
    }

    fun updateData(newData: List<QuestionItem>) {
        questionList.clear()
        questionList.addAll(newData)
        notifyDataSetChanged()
    }
    private fun applyTagStyle(holder: ViewHolder, tag: String) {
        val context = holder.binding.root.context
        val binding = holder.binding

        var textColorRes = com.example.areumdap.R.color.black
        var iconRes = com.example.areumdap.R.drawable.ic_reflection // 기본 아이콘

        when (tag) {
            "CAREER" -> {
                textColorRes = com.example.areumdap.R.color.career2
                iconRes = com.example.areumdap.R.drawable.ic_career
            }
            "RELATIONSHIP" -> {
                textColorRes = com.example.areumdap.R.color.relationship2
                iconRes = com.example.areumdap.R.drawable.ic_relationship
            }
            "SELF_REFLECTION" -> {
                textColorRes = com.example.areumdap.R.color.reflection2
                iconRes = com.example.areumdap.R.drawable.ic_reflection
            }
            "EMOTION" -> {
                textColorRes = com.example.areumdap.R.color.emotion2
                iconRes = com.example.areumdap.R.drawable.ic_emotion
            }
            "GROWTH" -> {
                textColorRes = com.example.areumdap.R.color.growth2
                iconRes = com.example.areumdap.R.drawable.ic_growth
            }
            "ETC" -> {
                textColorRes = com.example.areumdap.R.color.etc2
                iconRes = com.example.areumdap.R.drawable.ic_etc
            }
            else -> {
                textColorRes = com.example.areumdap.R.color.etc2
                iconRes = com.example.areumdap.R.drawable.ic_etc
            }
        }

        // 배경색 적용 코드 제거됨

        // 텍스트 색상 적용
        val textColor = androidx.core.content.ContextCompat.getColor(context, textColorRes)
        binding.itemQuestionTaskTv.setTextColor(textColor)
        
        // 아이콘 적용
        binding.itemQuestionTaskIv.setImageResource(iconRes)
    }
}