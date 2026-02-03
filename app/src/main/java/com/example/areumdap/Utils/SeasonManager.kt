package com.example.areumdap.Utils

import android.content.Context
import android.content.SharedPreferences
import com.example.areumdap.R

/**
 * 계절 테마 관리 클래스
 * - 선택한 계절 저장/불러오기
 * - 계절별 색상 반환
 */
object SeasonManager {

    private const val PREF_NAME = "season_pref"
    private const val KEY_SEASON = "selected_season"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 계절 저장
     * @param season "봄", "여름", "가을", "겨울"
     */
    fun saveSeason(context: Context, season: String) {
        getPrefs(context).edit()
            .putString(KEY_SEASON, season)
            .apply()
    }

    /**
     * 저장된 계절 불러오기
     * @return 저장된 계절 또는 null
     */
    fun getSeason(context: Context): String? {
        return getPrefs(context).getString(KEY_SEASON, null)
    }

    /**
     * 계절이 선택되었는지 확인
     */
    fun isSeasonSelected(context: Context): Boolean {
        return getSeason(context) != null
    }

    /**
     * 계절에 해당하는 색상 리소스 ID 반환
     * @return ColorRes ID
     */
    fun getSeasonColorRes(context: Context): Int {
        return when (getSeason(context)) {
            "봄" -> R.color.pink2
            "여름" -> R.color.green2
            "가을" -> R.color.yellow2
            "겨울" -> R.color.blue2
            else -> R.color.pink2  // 기본값
        }
    }

    /**
     * 계절에 해당하는 실제 색상 값 반환
     * @return Color Int 값
     */
    fun getSeasonColor(context: Context): Int {
        val colorRes = getSeasonColorRes(context)
        return context.getColor(colorRes)
    }

    /**
     * 계절 문자열로 색상 리소스 ID 반환 (저장 전에 사용)
     */
    fun getColorResBySeason(season: String): Int {
        return when (season) {
            "봄" -> R.color.pink2
            "여름" -> R.color.green2
            "가을" -> R.color.yellow2
            "겨울" -> R.color.blue2
            else -> R.color.pink2
        }
    }
}