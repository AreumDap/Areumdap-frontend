package com.example.areumdap.UI.auth

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.example.areumdap.R

enum class Category(
    val label:String,
    @DrawableRes val iconRes:Int,
    @ColorRes val colorRes: Int,
    @ColorRes val lineRes:Int
){
    REFLECTION("자기성찰", R.drawable.ic_reflection, R.color.reflection2, R.color.reflection1),
    RELATIONSHIP("관계", R.drawable.ic_relationship, R.color.relationship2, R.color.relationship1),
    CAREER("진로", R.drawable.ic_career, R.color.career2, R.color.career1),
    EMOTION("감정", R.drawable.ic_emotion, R.color.emotion2, R.color.emotion1),
    GROWTH("성장", R.drawable.ic_growth, R.color.growth2, R.color.growth1),
    ETC("기타", R.drawable.ic_etc, R.color.etc2, R.color.etc1);

    companion object{
        fun from(value:String?): Category{
            return when (value?.trim()){
                "자기성찰"->REFLECTION
                "관계"->RELATIONSHIP
                "진로" -> CAREER
                "감정" ->EMOTION
                "성장" -> GROWTH
                else -> ETC
            }
        }
        fun fromServerTag(tag: String?): Category {
            return when (tag?.trim()?.uppercase()) {
                "SELF_REFLECTION", "REFLECTION" -> REFLECTION
                "RELATION", "RELATIONSHIP" -> RELATIONSHIP
                "CAREER" -> CAREER
                "EMOTION" -> EMOTION
                "GROWTH" -> GROWTH
                else -> ETC
            }
        }
    }


}