package com.example.areumdap.UI.Task

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.areumdap.data.source.RetrofitClient
import com.example.areumdap.R
import com.example.areumdap.UI.auth.Category
import com.example.areumdap.UI.auth.ToastDialogFragment
import com.example.areumdap.databinding.FragmentDataTaskBinding
import kotlinx.coroutines.launch


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
            binding.taskCompleteBtn.text = "과제 완료"
            binding.taskCompleteBtn.setOnClickListener {
                completeMission(missionId)
            }
        }

        // Tip 및 D-day 가시성 초기 설정
        if (showTip) {
            binding.dataTaskCard.taskTipLl.visibility = View.VISIBLE
            binding.dataTaskCard.icClockIv.visibility = View.VISIBLE
            binding.dataTaskCard.sumDuedayTv.visibility = View.VISIBLE
        } else {
            binding.dataTaskCard.taskTipLl.visibility = View.GONE
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
                             showCustomToast("과제가 완료되었습니다!",R.drawable.ic_success)
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
                        // Mission completion failed
                        if (isAdded) {
                        }
                    }
                } else if (response.code() == 409) {
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
                    // Failed to complete mission
                    if (isAdded) {
                    }
                }
            } catch (e: Exception) {
                // Error completing mission
                if (isAdded) {
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            // 배경 투명하게 처리
            setBackgroundDrawableResource(android.R.color.transparent)
            // 화면 전체를 팝업 창으로 설정
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
                        binding.dataTaskCard.sumTaskDescTv.text = missionDetail.description ?: ""
                        
                        // API에서 reward가 0이 아니면 업데이트
                        if (missionDetail.rewardXp > 0) {
                            currentReward = missionDetail.rewardXp
                            binding.dataTaskCard.xpTv.text = "${missionDetail.rewardXp} XP"

                        }
                        
                        // 태그에 따른 스타일 적용
                        applyTagStyle(missionDetail.tag)
                        
                        // Tip 및 D-day 가시성 처리
                        if (showTip) {
                             // 가이드 텍스트 설정
                            missionDetail.guide?.let { guideText ->
                                binding.dataTaskCard.tipTv.text = guideText
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
        val ctx = requireContext()

        val cat = Category.fromServerTag(tag)

        val color = ContextCompat.getColor(ctx, cat.colorRes)
        val lineColor = ContextCompat.getColor(ctx, cat.lineRes)

        binding.taskCompleteBtn.backgroundTintList = ColorStateList.valueOf(color)

        with(binding.dataTaskCard) {
            // 카드 테두리
            summaryCv.setStrokeColor(lineColor)

            // 카테고리 텍스트/아이콘
            sumCatTv.text = cat.label
            sumCatTv.setTextColor(color)
            sumCatIv.setImageResource(cat.iconRes)

            // 아이콘 tint
            ImageViewCompat.setImageTintList(sumCatIv, ColorStateList.valueOf(color))
            ImageViewCompat.setImageTintList(icClockIv, ColorStateList.valueOf(color))

            // 듀데이 tint
            sumDuedayTv.setTextColor(color)
        }
    }

    data class Quadruple<out A, out B, out C, out D>(val first: A, val second: B, val third: C, val fourth: D)

    private fun showCustomToast(message: String, iconResId:Int) {
        val toast = ToastDialogFragment(message, iconResId)
        toast.show(requireActivity().supportFragmentManager, "custom_toast")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
