package com.example.areumdap.RVAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.areumdap.Task.MissionItem
import com.example.areumdap.UI.PopUpDialogFragment
import com.example.areumdap.databinding.ItemArchiveTaskBinding

class TaskRVAdapter(private val questionList: ArrayList<MissionItem>):
    RecyclerView.Adapter<TaskRVAdapter.ViewHolder>(){

    var itemClickListener: ((MissionItem) -> Unit)? = null
    inner class ViewHolder(val binding: ItemArchiveTaskBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val binding = ItemArchiveTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = questionList[position]

        with(holder.binding){
            // 질문 제목 반영
            itemArchiveTaskTv.text = item.title

            // 2. 날짜 및 요일 반영 (예: 2026.02.02 (월))
            itemArchiveDateTv.text = formatDateWithDay(item.completedAt)

            // 3. 경험치 반영 (reward 필드 사용)
            itemArchiveXpTv.text = item.reward.toString()

            // 카드 클릭 리스너 (스와이프 상태 확인 및 상세 페이지 이동)
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
                        removeItem(holder.adapterPosition)
                    }
                })

                dialog.show(activity.supportFragmentManager, "PopUpDialog")
            }
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

    fun updateData(newData: List<MissionItem>) {
        questionList.clear()
        questionList.addAll(newData)
        notifyDataSetChanged()
    }

    private fun formatDateWithDay(dateString: String): String {
        return try {
            // 1. 서버에서 오는 형식 (밀리초 .SSS와 Z 포함)
            // Z는 시간대를 나타내므로 따옴표 없이 사용합니다.
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
            // 서버 시간이 UTC 기준이므로 포맷터에게 이를 알려줍니다.
            inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")

            val date = inputFormat.parse(dateString)

            // 2. 출력할 형식: 2026.02.02 (월)
            // Locale.KOREA를 설정해야 '월', '화' 같은 한글 요일이 나옵니다.
            val outputFormat = java.text.SimpleDateFormat("yyyy.MM.dd (E)", java.util.Locale.KOREA)

            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            // 변환 실패 시 원본 문자열의 날짜 부분만이라도 출력
            dateString.split("T")[0].replace("-", ".")
        }
    }
}