package com.example.areumdap.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.areumdap.data.model.QuestionItem
import com.example.areumdap.UI.auth.PopUpDialogFragment
import com.example.areumdap.databinding.ItemQuestionTaskBinding

class QuestionRVAdapter (private val questionList: ArrayList<QuestionItem>):
    RecyclerView.Adapter<QuestionRVAdapter.ViewHolder>(){

    var itemDeleteListener: ((Long) -> Unit)? = null
    var itemClickListener: ((QuestionItem) -> Unit)? = null

    inner class ViewHolder(val binding: ItemQuestionTaskBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val binding = ItemQuestionTaskBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = questionList[position]

        // 질문 내용 바인딩 (아래쪽 텍스트)
        holder.binding.itemQuestionContentTv.text = item.content
        holder.binding.taskCardView.translationX = 0f

        // 태그 텍스트 및 스타일 적용 (위쪽 텍스트)
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
    fun removeItem(position: Int){
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
            "RELATIONSHIP", "RELATION" -> {
                textColorRes = com.example.areumdap.R.color.relationship2
                iconRes = com.example.areumdap.R.drawable.ic_relationship
            }
            "REFLECTION", "SELF_REFLECTION" -> {
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
            "ETC", "OTHER", "OTHERS", "ELSE" -> {
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

        // 태그 한글명 변환 및 적용
        val tagName = when (tag) {
            "CAREER" -> "진로"
            "RELATIONSHIP", "RELATION" -> "관계"
            "REFLECTION", "SELF_REFLECTION" -> "자기성찰"
            "EMOTION" -> "감정"
            "GROWTH" -> "성장"
            "ETC", "OTHER", "OTHERS", "ELSE" -> "기타"
            else -> "기타"
        }
        binding.itemQuestionTaskTv.text = tagName
    }
}