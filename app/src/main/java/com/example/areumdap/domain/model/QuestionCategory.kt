package com.example.areumdap.domain.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.example.areumdap.R

enum class QuestionCategory(
    val label:String,
    @DrawableRes val iconRes:Int,
    @ColorRes val colorRes: Int
){
    REFLECTION("자기성찰", R.drawable.ic_reflection, R.color.reflection2),
    RELATIONSHIP("관계", R.drawable.ic_relationship, R.color.relationship2),
    CAREER("진로", R.drawable.ic_career, R.color.career2),
    EMOTION("감정", R.drawable.ic_emotion, R.color.emotion2),
    ETC("기타", R.drawable.ic_etc, R.color.etc2);

    companion object{
        fun from(value:String?): QuestionCategory{
            return when (value?.trim()){
                "자기성찰"->REFLECTION
                "관계"->RELATIONSHIP
                "진로" -> CAREER
                "감정" ->EMOTION
                else -> ETC
            }
        }
    }
}