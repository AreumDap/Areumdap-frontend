package com.example.areumdap.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.areumdap.data.model.MissionItem
import com.example.areumdap.databinding.ItemCharacterTaskBinding

class CharacterTaskRVAdapter(private val missionList: ArrayList<MissionItem>):
    RecyclerView.Adapter<CharacterTaskRVAdapter.ViewHolder>(){

    var itemClickListener: ((MissionItem) -> Unit)? = null
    // 경험치가 가득 찼는지 여부
    var isEvolutionReady: Boolean = false
    // 진화가 필요할 때 호출할 콜백
    var onEvolutionRequiredListener: (() -> Unit)? = null

    inner class ViewHolder(val binding: ItemCharacterTaskBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val binding = ItemCharacterTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = missionList[position]

        with(holder.binding){
            tvTaskTitle.text = item.title
            
            // D-day 데이터 반영
            val dDay = item.dDay ?: 0
            if (dDay == 0) {
                tvDdayValue.text = "오늘"
                tvDdayLabel.visibility = android.view.View.GONE
            } else {
                tvDdayValue.text = dDay.toString()
                tvDdayLabel.visibility = android.view.View.VISIBLE
            }

            taskCompleteBtn.setOnClickListener {
                if (isEvolutionReady) {
                    onEvolutionRequiredListener?.invoke()
                } else {
                    itemClickListener?.invoke(item)
                }
            }

            // 태그별 색상 적용
            applyTagStyle(holder, item.tag)
        }
    }

    private fun applyTagStyle(holder: ViewHolder, tag: String) {
        val context = holder.binding.root.context
        val binding = holder.binding

        val colorRes = when (tag) {
            "CAREER" -> com.example.areumdap.R.color.career2
            "RELATIONSHIP", "RELATION" -> com.example.areumdap.R.color.relationship2
            "REFLECTION", "SELF_REFLECTION" -> com.example.areumdap.R.color.reflection2
            "EMOTION" -> com.example.areumdap.R.color.emotion2
            "GROWTH" -> com.example.areumdap.R.color.growth2
            "ETC", "OTHER", "OTHERS", "ELSE" -> com.example.areumdap.R.color.etc2
            else -> com.example.areumdap.R.color.etc2
        }

        val color = androidx.core.content.ContextCompat.getColor(context, colorRes)

        // 1. 시계 아이콘 색상 변경
        binding.icClockIv.setColorFilter(color)

        // 2. D-day 값 및 라벨 텍스트 색상 변경
        binding.tvDdayValue.setTextColor(color)
        binding.tvDdayLabel.setTextColor(color)

        // 3. 버튼 배경색 변경
        binding.taskCompleteBtn.backgroundTintList = android.content.res.ColorStateList.valueOf(color)
    }

    override fun getItemCount(): Int = missionList.size

    fun updateData(newData: List<MissionItem>) {
        missionList.clear()
        missionList.addAll(newData)
        notifyDataSetChanged()
    }

    fun getCurrentList(): ArrayList<MissionItem> {
        return missionList
    }
}
