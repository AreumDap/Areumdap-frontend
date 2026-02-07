package com.example.areumdap.VPAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.areumdap.UI.Chat.data.Mission
import com.example.areumdap.databinding.ItemTaskCardBinding
import com.example.areumdap.domain.model.Category
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
class TaskGuideVPAdapter : RecyclerView.Adapter<TaskGuideVPAdapter.VH>() {
    private fun formatDue(dueIso: String?): String {
        if (dueIso.isNullOrBlank()) return ""

        return try {
            // "2026-02-08T..." -> "2026-02-08"
            val datePart = dueIso.take(10)

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.isLenient = false

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
            val cat = Category.fromServerTag(item.tag)
            sumCatTv.text = cat.label
            sumCatIv.setImageResource(cat.iconRes)

            val color = ContextCompat.getColor(holder.binding.root.context, cat.colorRes)
            sumCatTv.setTextColor(color)
            sumTaskTitleTv.text = item.title
            sumTaskDescTv.text = item.content
            xpTv.text = "${item.reward} XP"

            val dueText = when {
                item.dDay != null -> "D-${item.dDay}"
                else -> formatDue(item.dueDate)
            }

            sumDuedayTv.text = dueText
            sumDuedayTv.visibility = if (dueText.isBlank()) View.GONE else View.VISIBLE
        }
    }

    override fun getItemCount(): Int = items.size
}