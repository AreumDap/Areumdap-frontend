package com.example.areumdap.UI

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.areumdap.R
import com.example.areumdap.databinding.FragmentSettingBinding
import java.util.Calendar
import android.content.Intent
import com.example.areumdap.Network.TokenManager

import com.example.areumdap.UI.auth.LoginActivity

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    // 임시 데이터 (실제로는 SharedPreferences나 ViewModel에서 관리)
    private var isNotificationEnabled = true
    private var notificationTime = "22:00"
    private var userName = "최지은"
    private var userBirthday = "2000.01.01"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        updateUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupClickListeners() {
        // 알림 스위치
        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            isNotificationEnabled = isChecked
            // TODO: SharedPreferences에 저장
        }

        // 푸시 알림 시간
        binding.layoutNotificationTime.setOnClickListener {
            showTimePickerDialog()
        }

        // 이름 변경
        binding.layoutName.setOnClickListener {
            showNicknameDialog()
        }

        // 생년월일 변경
        binding.layoutBirthday.setOnClickListener {
            showDatePickerDialog()
        }

        // 서비스 소개
        binding.layoutServiceIntro.setOnClickListener {
            // TODO: 서비스 소개 화면으로 이동
        }

        // 개인정보 처리방침
        binding.layoutPrivacyPolicy.setOnClickListener {
            // TODO: 개인정보 처리방침 화면으로 이동
        }

        // 이용약관
        binding.layoutTerms.setOnClickListener {
            // TODO: 이용약관 화면으로 이동
        }

        // 로그아웃
        binding.layoutLogout.setOnClickListener {
            showLogoutConfirmDialog()
        }

        // 계정탈퇴
        binding.layoutDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmDialog()
        }
    }

    private fun updateUI() {
        binding.switchNotification.isChecked = isNotificationEnabled
        binding.tvNotificationTime.text = notificationTime
        binding.tvName.text = userName
        binding.tvBirthday.text = userBirthday
    }

    // 닉네임 변경 다이얼로그
    private fun showNicknameDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_nickname)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val etNickname = dialog.findViewById<EditText>(R.id.et_nickname)
        val btnClear = dialog.findViewById<ImageButton>(R.id.btn_clear)
        val btnConfirm = dialog.findViewById<Button>(R.id.btn_confirm)

        etNickname.setText(userName)

        etNickname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                btnClear.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
        })

        btnClear.setOnClickListener {
            etNickname.text.clear()
        }

        btnConfirm.setOnClickListener {
            val newName = etNickname.text.toString().trim()
            if (newName.isNotEmpty()) {
                userName = newName
                binding.tvName.text = userName
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    // 생년월일 선택 다이얼로그
    private fun showDatePickerDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_date_picker)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val pickerYear = dialog.findViewById<NumberPicker>(R.id.picker_year)
        val pickerMonth = dialog.findViewById<NumberPicker>(R.id.picker_month)
        val pickerDay = dialog.findViewById<NumberPicker>(R.id.picker_day)
        val btnConfirm = dialog.findViewById<Button>(R.id.btn_confirm)

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        pickerYear.minValue = 1900
        pickerYear.maxValue = currentYear
        pickerYear.displayedValues = (1900..currentYear).map { "${it}년" }.toTypedArray()

        pickerMonth.minValue = 1
        pickerMonth.maxValue = 12
        pickerMonth.displayedValues = (1..12).map { "${it}월" }.toTypedArray()

        pickerDay.minValue = 1
        pickerDay.maxValue = 31
        pickerDay.displayedValues = (1..31).map { "${it}일" }.toTypedArray()

        val parts = userBirthday.split(".")
        if (parts.size == 3) {
            pickerYear.value = parts[0].toIntOrNull() ?: 2000
            pickerMonth.value = parts[1].toIntOrNull() ?: 1
            pickerDay.value = parts[2].toIntOrNull() ?: 1
        }

        btnConfirm.setOnClickListener {
            val year = pickerYear.value
            val month = pickerMonth.value
            val day = pickerDay.value
            userBirthday = String.format("%04d.%02d.%02d", year, month, day)
            binding.tvBirthday.text = userBirthday
            dialog.dismiss()
        }

        dialog.show()
    }

    // 시간 선택 다이얼로그
    private fun showTimePickerDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_time_picker)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val timePicker = dialog.findViewById<TimePicker>(R.id.time_picker)
        val btnConfirm = dialog.findViewById<Button>(R.id.btn_confirm)

        timePicker.setIs24HourView(true)

        val timeParts = notificationTime.split(":")
        if (timeParts.size == 2) {
            timePicker.hour = timeParts[0].toIntOrNull() ?: 22
            timePicker.minute = timeParts[1].toIntOrNull() ?: 0
        }

        btnConfirm.setOnClickListener {
            notificationTime = String.format("%02d:%02d", timePicker.hour, timePicker.minute)
            binding.tvNotificationTime.text = notificationTime
            dialog.dismiss()
        }

        dialog.show()
    }

    // 로그아웃 확인 다이얼로그
    // 로그아웃 확인 다이얼로그
    private fun showLogoutConfirmDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("로그아웃")
            .setMessage("정말 로그아웃 하시겠어요?")
            .setPositiveButton("로그아웃") { _, _ ->
                // 1. 저장된 로그인/온보딩 정보 삭제 (핵심!)
                val prefs = requireContext().getSharedPreferences("auth", android.content.Context.MODE_PRIVATE)
                prefs.edit().clear().apply() // 모든 저장 데이터 삭제

                // 2. 토큰 삭제
                TokenManager.clearAll()

                val cookieManager = android.webkit.CookieManager.getInstance()
                cookieManager.removeAllCookies(null)
                cookieManager.flush()

                // 3. 로그인 화면으로 이동
                val intent = Intent(requireContext(), LoginActivity::class.java) // LoginActivity import 필요
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 계정탈퇴 확인 다이얼로그
    private fun showDeleteAccountConfirmDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("계정탈퇴")
            .setMessage("정말 탈퇴하시겠어요?\n모든 데이터가 삭제되며 복구할 수 없습니다.")
            .setPositiveButton("탈퇴") { _, _ ->
                // TODO: 계정탈퇴 처리
            }
            .setNegativeButton("취소", null)
            .show()
    }
}