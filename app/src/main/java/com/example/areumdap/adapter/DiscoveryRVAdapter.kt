package com.example.areumdap.adapter

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.example.areumdap.data.model.InsightDto
import com.example.areumdap.databinding.ItemFoundCardBinding
import androidx.appcompat.R as AppCompatR



class DiscoveryRVAdapter : ListAdapter<InsightDto, DiscoveryRVAdapter.VH>(DIFF) {
    inner class VH(private val binding: ItemFoundCardBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: InsightDto){
            val highlightColor = MaterialColors.getColor(binding.root, AppCompatR.attr.colorPrimary)
            binding.fountCardContentTv.text = highlightBacktickText(item.content, highlightColor)
        }
    }

    private fun highlightBacktickText(raw: String, color: Int): CharSequence {
        val result = SpannableStringBuilder()
        val regex = Regex("`([^`]+)`")
        var last = 0

        regex.findAll(raw).forEach { match ->
            val start = match.range.first
            val end = match.range.last + 1
            val inner = match.groupValues[1]

            if (start > last) {
                result.append(raw.substring(last, start))
            }

            val spanStart = result.length
            result.append(inner) // backtick 문자는 노출하지 않음
            result.setSpan(
                ForegroundColorSpan(color),
                spanStart,
                result.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            last = end
        }

        if (last < raw.length) {
            result.append(raw.substring(last))
        }

        return if (result.isEmpty()) raw else result
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType:Int): VH{
        val binding = ItemFoundCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder:VH, position:Int){
        holder.bind(getItem(position))
    }

    companion object {
        private val DIFF = object: DiffUtil.ItemCallback<InsightDto>(){
            override fun areItemsTheSame(o: InsightDto, n: InsightDto) = o.insightId == n.insightId
            override fun areContentsTheSame(o: InsightDto, n: InsightDto) = o == n

        }

    }

}

