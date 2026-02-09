package com.example.areumdap.UI.Task

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.areumdap.data.source.RetrofitClient
import com.example.areumdap.R
import com.example.areumdap.databinding.FragmentDataTaskBinding
import kotlinx.coroutines.launch
import android.widget.Toast

class DataTaskFragment: DialogFragment() {
    private var _binding: FragmentDataTaskBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 다이알로그 스타일 설정
        setStyle(STYLE_NORMAL, R.style.CustomDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDataTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var currentReward: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val missionId = arguments?.getInt("missionId") ?: return
        currentReward = arguments?.getInt("reward") ?: 0
        val title = arguments?.getString("title")
        val tag = arguments?.getString("tag")
        val showTip = arguments?.getBoolean("showTip", true) ?: true
        val isCompleted = arguments?.getBoolean("isCompleted", false) ?: false
        val isTransparent = arguments?.getBoolean("isTransparent", false) ?: false

        // 배경색 설정 (투명 여부에 따라)
        if (!isTransparent) {
            binding.root.setBackgroundResource(R.color.background1)
        }

        // 전달받은 데이터 즉시 표시
        if (currentReward > 0) {
            binding.dataTaskCard.xpTv.text = "${currentReward} XP"
        }
        title?.let { binding.dataTaskCard.sumTaskTitleTv.text = it }
        tag?.let { applyTagStyle(it) }

        // 완료 여부에 따른 버튼 텍스트 및 클릭 리스너 설정
        if (isCompleted) {
            binding.taskCompleteBtn.text = "이전으로"
            binding.taskCompleteBtn.setOnClickListener {
                if (showsDialog) dismiss() else parentFragmentManager.popBackStack()
            }
        } else {
            binding.taskCompleteBtn.text = "완료하기"
            binding.taskCompleteBtn.setOnClickListener {
                completeMission(missionId)
            }
        }

        // Tip 및 D-day 가시성 초기 설정
        if (showTip) {
            binding.taskTipLl.visibility = View.VISIBLE
            binding.dataTaskCard.icClockIv.visibility = View.VISIBLE
            binding.dataTaskCard.sumDuedayTv.visibility = View.VISIBLE
        } else {
            binding.taskTipLl.visibility = View.GONE
            binding.dataTaskCard.icClockIv.visibility = View.INVISIBLE
            binding.dataTaskCard.sumDuedayTv.visibility = View.INVISIBLE

            val layoutParams = binding.taskCompleteBtn.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = (80 * resources.displayMetrics.density).toInt()
            binding.taskCompleteBtn.layoutParams = layoutParams
        }

        // API 미션 상세 정보 가져오기
        fetchMissionDetail(missionId, showTip)
    }

    private fun completeMission(missionId: Int) {
        lifecycleScope.launch {
            try {
                val body = mapOf("missionId" to missionId)
                val response = RetrofitClient.taskService.postMissionComplete(body)
                
                if (response.isSuccessful) {
                    val result = response.body()
                    if (result?.isSuccess == true) {
                        if (isAdded) {
                             Toast.makeText(context, "과제가 완료되었습니다!", Toast.LENGTH_SHORT).show()
                        }
                        // Refresh trigger
                        // reward 전달하여 즉시 UI 반영
                        val resultBundle = Bundle().apply {
                            putInt("missionId", missionId)
                            if (currentReward > 0) {
                                putInt("reward", currentReward)
                            }
                        }
                        parentFragmentManager.setFragmentResult("missionCompleted", resultBundle)
                        
                        if (showsDialog) {
                            dismiss()
                        } else {
                            parentFragmentManager.popBackStack()
                        }
                    } else {
                        Log.e("DataTaskFragment", "Failed to complete mission: ${result?.message}")
                        if (isAdded) {
                            Toast.makeText(context, "완료 실패: ${result?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else if (response.code() == 409) {
                    // 이미 완료된 경우 (토스트 없이 조용히 닫기)
                    // Refresh trigger (to remove it from list)
                    // reward도 함께 전달하여 즉시 UI 반영
                    val resultBundle = Bundle().apply {
                        putInt("missionId", missionId)
                        if (currentReward > 0) {
                            putInt("reward", currentReward)
                        }
                    }
                    parentFragmentManager.setFragmentResult("missionCompleted", resultBundle)
                    
                     if (showsDialog) {
                        dismiss()
                    } else {
                        parentFragmentManager.popBackStack()
                    }
                } else {
                    Log.e("DataTaskFragment", "Failed to complete mission: ${response.code()}")
                    if (isAdded) {
                        Toast.makeText(context, "서버 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("DataTaskFragment", "Error completing mission", e)
                if (isAdded) {
                    Toast.makeText(context, "에러 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            // 배경 투명하게 처리
            setBackgroundDrawableResource(android.R.color.transparent)
            // 화면 전체를 팝업 창으로 설정 (이게 없으면 XML의 'parent' 기준이 틀어져서 마진이 다르게 보입니다)
            setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun fetchMissionDetail(missionId: Int, showTip: Boolean) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.taskService.getMissionDetail(missionId)
                if (response.isSuccessful) {
                    val missionDetail = response.body()?.data
                    if (missionDetail != null) {
                        // UI 업데이트
                        binding.dataTaskCard.sumTaskTitleTv.text = missionDetail.title
                        binding.dataTaskCard.sumTaskTitleTv.text = missionDetail.title
                        
                        // API에서 reward가 0이 아니면 업데이트
                        if (missionDetail.rewardXp > 0) {
                            currentReward = missionDetail.rewardXp // Update class property
                            binding.dataTaskCard.xpTv.text = "${missionDetail.rewardXp} XP"
                            Log.d("DEBUG_XP", "Updated currentReward from API: $currentReward")
                        }
                        
                        // 태그에 따른 스타일 적용
                        applyTagStyle(missionDetail.tag)
                        
                        // Tip 및 D-day 가시성 처리 (초기 설정은 onViewCreated에서 했지만, 내용 채우기 위해 유지)
                        if (showTip) {
                             // 가이드 텍스트 설정
                            missionDetail.guide?.let { guideText ->
                                binding.tipTv.text = guideText
                            }

                            missionDetail.dDay?.let {
                                if (it == 0) {
                                    binding.dataTaskCard.sumDuedayTv.text = "오늘"
                                } else {
                                    binding.dataTaskCard.sumDuedayTv.text = "${it}일"
                                }
                            }
                        }
                    }
                } else {
                    // Failed to load mission
                }
            } catch (e: Exception) {
                // Error loading mission
            }
        }
    }

    private fun applyTagStyle(tag: String) {
        val (color1Res, color2Res, iconRes, catName) = when (tag) {
            "CAREER" -> Quadruple(R.color.career1, R.color.career2, R.drawable.ic_career, "진로")
            "RELATIONSHIP", "RELATION" -> Quadruple(R.color.relationship1, R.color.relationship2, R.drawable.ic_relationship, "관계")
            "REFLECTION", "SELF_REFLECTION" -> Quadruple(R.color.reflection1, R.color.reflection2, R.drawable.ic_reflection, "자기성찰")
            "EMOTION" -> Quadruple(R.color.emotion1, R.color.emotion2, R.drawable.ic_emotion, "감정")
            "GROWTH" -> Quadruple(R.color.growth1, R.color.growth2, R.drawable.ic_growth, "성장")
            "ETC", "OTHER", "OTHERS", "ELSE" -> Quadruple(R.color.etc1, R.color.etc2, R.drawable.ic_etc, "기타")
            else -> Quadruple(R.color.etc1, R.color.etc2, R.drawable.ic_etc, "기타")
        }

        val context = requireContext()
        val color1 = ContextCompat.getColor(context, color1Res)
        val color2 = ContextCompat.getColor(context, color2Res)

        // 1. 버튼 배경색 변경 (color2)
        binding.taskCompleteBtn.backgroundTintList = ColorStateList.valueOf(color2)

        // 2. 포함된 카드 뷰 스타일 변경
        with(binding.dataTaskCard) {
            // 카드 테두리 및 그림자 (color1)
            summaryCv.strokeColor = color1
            summaryCv.setOutlineAmbientShadowColor(color1)
            summaryCv.setOutlineSpotShadowColor(color1)

            // 카테고리 텍스트 및 아이콘 (color2)
            sumCatTv.text = catName
            sumCatTv.setTextColor(color2)
            sumCatIv.setImageResource(iconRes)
            
            // 듀데이 텍스트 (color2) - item_task_card.xml 에 있음
            sumDuedayTv.setTextColor(color2)
        }
    }

    data class Quadruple<out A, out B, out C, out D>(val first: A, val second: B, val third: C, val fourth: D)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}