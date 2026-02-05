package com.example.areumdap.UI

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.areumdap.databinding.ActivityDialogBinding

class PopUpDialogFragment : DialogFragment() {
    private lateinit var binding : ActivityDialogBinding

    private var callback: MyDialogCallback? = null

    interface MyDialogCallback{
        fun onConfirm()
    }

    fun setCallback(callback: MyDialogCallback){
        this.callback = callback
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = ActivityDialogBinding.inflate(inflater,container,false)

        binding.tvTitle.text = arguments?.getString("title") ?: "제목 없음"
// ✅ subtitle이 비어있으면 TextView 숨기기
        val subtitle = arguments?.getString("subtitle") ?: ""
        if (subtitle.isEmpty()) {
            binding.tvDesc.visibility = View.GONE
        } else {
            binding.tvDesc.visibility = View.VISIBLE
            binding.tvDesc.text = subtitle
        }
        binding.btnContinue.text = arguments?.getString("btnleft") ?: "이전으로"
        binding.btnRight.text = arguments?.getString("btnright") ?: "확인"

        binding.btnContinue.setOnClickListener {
            dismiss()
        }

        binding.btnRight.setOnClickListener {
            callback?.onConfirm()
            dismiss()
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // 다이얼로그의 너비와 높이를 화면에 맞게 재설정
            val params = attributes
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            attributes = params
        }    }

    companion object {
        fun newInstance(title: String, subtitle: String, leftBtn: String, rightBtn: String): PopUpDialogFragment {
            return PopUpDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("title", title)
                    putString("subtitle", subtitle)
                    putString("btnleft", leftBtn)
                    putString("btnright", rightBtn)
                }
            }
        }
    }
}