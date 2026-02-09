package com.example.areumdap.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.areumdap.data.model.MissionItem
import com.example.areumdap.UI.auth.PopUpDialogFragment
import com.example.areumdap.databinding.ItemArchiveTaskBinding

class TaskRVAdapter(private val questionList: ArrayList<MissionItem>):
    RecyclerView.Adapter<TaskRVAdapter.ViewHolder>(){

    var itemClickListener: ((MissionItem) -> Unit)? = null
    var itemDeleteListener: ((Int) -> Unit)? = null

    inner class ViewHolder(val binding: ItemArchiveTaskBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val binding = ItemArchiveTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = questionList[position]

        with(holder.binding){
            // 뷰홀더 재사용 시 스와이프 상태 초기화
            taskCardView.translationX = 0f

            // 질문 제목 반영
            itemArchiveTaskTv.text = item.title

            // 날짜 및 요일 반영 (예: 2026.02.02 (월))
            itemArchiveDateTv.text = formatDateWithDay(item.completedAt)

            // 경험치 반영 (reward 필드 사용)
            itemArchiveXpTv.text = item.reward.toString()

            // 태그별 스타일 적용
            applyTagStyle(holder, item.tag)

            // 카드 클릭 리스너
            taskCardView.setOnClickListener {
                val currentX = it.translationX
                if (currentX < 0f) {
                    it.animate().translationX(0f).setDuration(200).start()
                } else {
                    itemClickListener?.invoke(item)
                }
            }

            taskTrashIv.setOnClickListener {
                val activity = holder.binding.root.context as androidx.fragment.app.FragmentActivity

                val dialog = PopUpDialogFragment.newInstance(
                    title = "완료한 과제를 정말로 삭제하겠어요?\n삭제한 리스트는 되돌릴 수 없어요.",
                    subtitle = "",
                    leftBtn = "이전으로",
                    rightBtn = "삭제하기"
                )

                dialog.setCallback(object : PopUpDialogFragment.MyDialogCallback{
                    override fun onConfirm(){
                        itemDeleteListener?.invoke(item.missionId)
                    }
                })

                dialog.show(activity.supportFragmentManager, "PopUpDialog")
            }
        }
    }

    override fun getItemCount(): Int = questionList.size

    fun updateData(newData: List<MissionItem>) {
        questionList.clear()
        questionList.addAll(newData)
        notifyDataSetChanged()
    }

    private fun formatDateWithDay(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return ""
        return try {
            // 서버에서 오는 형식 (밀리초 .SSS와 Z 포함)
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
            inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")

            val date = inputFormat.parse(dateString)

            // 출력할 형식: 2026.02.02 (월)
            val outputFormat = java.text.SimpleDateFormat("yyyy.MM.dd (E)", java.util.Locale.KOREA)

            date?.let {
                outputFormat.format(it)
            } ?: dateString

        } catch (e: Exception) {
            dateString.split("T").getOrNull(0)?.replace("-", ".") ?: ""
        }
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


        // 텍스트 색상 적용
        val textColor = androidx.core.content.ContextCompat.getColor(context, textColorRes)
        binding.itemArchiveTaskTv.setTextColor(textColor)

        // 아이콘 및 체크 표시 색상 적용
        binding.itemArchiveTaskIv.setImageResource(iconRes)
        binding.itemArchiveCheckIv.setColorFilter(textColor)
    }
}