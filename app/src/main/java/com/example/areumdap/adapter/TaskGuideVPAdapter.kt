package com.example.areumdap.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.areumdap.UI.auth.Category
import com.example.areumdap.data.model.Mission
import com.example.areumdap.databinding.ItemTaskCardBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class TaskGuideVPAdapter : RecyclerView.Adapter<TaskGuideVPAdapter.VH>() {

    private fun formatDue(dueIso: String?): String {
        if (dueIso.isNullOrBlank()) return ""

        return try {
            val datePart = dueIso.take(10)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                isLenient = false
            }

            val dueDate: Date = sdf.parse(datePart) ?: return ""
            val todayDate: Date = sdf.parse(sdf.format(Date())) ?: return ""

            val diffMs = dueDate.time - todayDate.time
            val days = TimeUnit.MILLISECONDS.toDays(diffMs).toInt()

            when {
                days > 0 -> "D-$days"
                days == 0 -> "D-DAY"
                else -> "D+${-days}"
            }
        } catch (e: Exception) {
            ""
        }
    }

    private val items = mutableListOf<Mission>()

    fun submitList(list: List<Mission>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val binding: ItemTaskCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemTaskCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        with(holder.binding) {
            val ctx = root.context

            // 카테고리
            val cat = Category.fromServerTag(item.tag)
            sumCatTv.text = cat.label
            sumCatIv.setImageResource(cat.iconRes)

            val color = ContextCompat.getColor(ctx, cat.colorRes)
            sumCatTv.setTextColor(color)
            ImageViewCompat.setImageTintList(sumCatIv, ColorStateList.valueOf(color)) // ✅ 카테고리 아이콘도 틴트 주고 싶으면
            ImageViewCompat.setImageTintList(icClockIv, ColorStateList.valueOf(color))
            sumDuedayTv.setTextColor(color)

            val lineColor = ContextCompat.getColor(ctx, cat.lineRes)
            summaryCv.setStrokeColor(lineColor)

            // 본문
            sumTaskTitleTv.text = item.title
            sumTaskDescTv.text = item.content
            xpTv.text = "${item.reward} XP"

            // D-day
            val dueText = when {
                item.dDay != null -> "D-${item.dDay}"
                else -> formatDue(item.dueDate)
            }
            sumDuedayTv.text = dueText
            sumDuedayTv.visibility = if (dueText.isBlank()) View.GONE else View.VISIBLE


            //tip박스
            val tipText = (item.tip ?: "").trim()

            taskTipLl.visibility = if (tipText.isBlank()) View.GONE else View.VISIBLE
            if (tipText.isNotBlank()) {
                tipTv.text = tipText
            }
        }
    }

    fun getItem(position: Int): Mission? = items.getOrNull(position)
    fun getItems(): List<Mission> = items

    override fun getItemCount(): Int = items.size
}
