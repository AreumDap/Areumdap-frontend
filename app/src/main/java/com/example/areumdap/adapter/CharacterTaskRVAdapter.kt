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
        }
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
