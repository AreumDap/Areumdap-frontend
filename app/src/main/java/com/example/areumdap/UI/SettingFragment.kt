package com.example.areumdap.UI

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.areumdap.R
import com.example.areumdap.databinding.FragmentSettingBinding
import android.content.Context
import android.content.Intent
import com.example.areumdap.Network.TokenManager
import com.example.areumdap.Network.AuthRepository
import com.example.areumdap.Network.UserRepository
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.areumdap.UI.auth.LoginActivity

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    // 설정 데이터
    private var isNotificationEnabled = true
    private var notificationTime = "22:00"
    private var userNickname = "사용자"  // userName -> userNickname으로 변경
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
        setupNameInlineEdit()
        loadUserData()  // 서버에서 데이터 불러오기 (완료 후 updateUI 호출됨)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * 서버에서 사용자 데이터 불러오기
     */
    private fun loadUserData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = UserRepository.getProfile()

            result.onSuccess { profile ->
                if (!isAdded) return@onSuccess  // Fragment가 붙어있지 않으면 종료

                // 서버에서 가져온 데이터로 설정 - nickname 우선 사용
                userNickname = profile.nickname ?: profile.name ?: "사용자"
                userBirthday = profile.birth?.replace("-", ".") ?: "2000.01.01"
                isNotificationEnabled = profile.notificationEnabled
                notificationTime = profile.pushNotificationTime ?: "22:00"

                // UI 업데이트
                updateUI()

                // 로컬에도 저장
                saveSettingsLocally()
            }.onFailure { error ->
                if (!isAdded) return@onFailure  // Fragment가 붙어있지 않으면 종료

                // 실패시 로컬 데이터 사용
                loadLocalData()
                Toast.makeText(requireContext(), "프로필을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 로컬에서 데이터 불러오기 (서버 실패시 fallback)
     */
    private fun loadLocalData() {
        if (!isAdded) return  // Fragment가 붙어있지 않으면 종료

        // TokenManager에서 닉네임 우선 가져오기 (없으면 이름 사용)
        userNickname = TokenManager.getUserNickname() ?: TokenManager.getUserName() ?: "사용자"

        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        isNotificationEnabled = prefs.getBoolean("notification_enabled", true)
        notificationTime = prefs.getString("notification_time", "22:00") ?: "22:00"
        userBirthday = prefs.getString("user_birthday", "2000.01.01") ?: "2000.01.01"

        updateUI()
    }

    /**
     * 설정 값 로컬에 저장
     */
    private fun saveSettingsLocally() {
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("notification_enabled", isNotificationEnabled)
            .putString("notification_time", notificationTime)
            .putString("user_birthday", userBirthday)
            .apply()
    }

    private fun setupClickListeners() {
        // 알림 스위치 - 토글 연동
        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            isNotificationEnabled = isChecked
            updateNotificationTimeState()

            // 서버에 알림 설정 저장
            updateNotificationToServer()
        }

        // 푸시 알림 시간
        binding.layoutNotificationTime.setOnClickListener {
            if (isNotificationEnabled) {
                showTimePickerDialog()
            }
        }

        // 닉네임 변경 (인라인 편집)
        binding.layoutName.setOnClickListener {
            enableNameEdit()
        }

        // 생년월일 변경 (캘린더)
        binding.layoutBirthday.setOnClickListener {
            showCalendarDatePicker()
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

    // ============ 서버 연동 함수들 ============

    /**
     * 알림 설정 서버에 저장
     */
    private fun updateNotificationToServer() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = UserRepository.updateNotification(isNotificationEnabled, notificationTime)

            result.onSuccess {
                saveSettingsLocally()
            }.onFailure { error ->
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 닉네임 서버에 저장
     */
    private fun updateNicknameToServer(newNickname: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = UserRepository.updateNickname(newNickname)

            result.onSuccess {
                userNickname = newNickname
                binding.tvName.text = userNickname
                Toast.makeText(requireContext(), "닉네임이 변경되었습니다.", Toast.LENGTH_SHORT).show()
            }.onFailure { error ->
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                // 실패시 원래 닉네임으로 복원
                binding.tvName.text = userNickname
            }
        }
    }

    /**
     * 생년월일 서버에 저장
     */
    private fun updateBirthToServer(birth: String) {
        // API 형식: "yyyy-MM-dd"
        val apiFormatBirth = birth.replace(".", "-")

        viewLifecycleOwner.lifecycleScope.launch {
            val result = UserRepository.updateBirth(apiFormatBirth)

            result.onSuccess {
                userBirthday = birth
                binding.tvBirthday.text = userBirthday
                saveSettingsLocally()
                Toast.makeText(requireContext(), "생년월일이 변경되었습니다.", Toast.LENGTH_SHORT).show()
            }.onFailure { error ->
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateNotificationTimeState() {
        binding.tvNotificationTime.isEnabled = isNotificationEnabled
        binding.layoutNotificationTime.alpha = if (isNotificationEnabled) 1.0f else 0.5f
    }

    // ============ 인라인 편집 기능 ============
    private fun setupNameInlineEdit() {
        binding.etName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveNameAndDisableEdit()
                true
            } else {
                false
            }
        }

        binding.etName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                saveNameAndDisableEdit()
            }
        }
    }

    private fun enableNameEdit() {
        binding.tvName.visibility = View.GONE
        binding.etName.visibility = View.VISIBLE
        binding.etName.setText(userNickname)
        binding.etName.requestFocus()
        binding.etName.setSelection(binding.etName.text.length)

        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etName, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun saveNameAndDisableEdit() {
        val newNickname = binding.etName.text.toString().trim()

        // EditText 숨기고 TextView 보이기
        binding.etName.visibility = View.GONE
        binding.tvName.visibility = View.VISIBLE

        // 키보드 숨기기
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etName.windowToken, 0)

        // 닉네임이 변경되었으면 서버에 저장
        if (newNickname.isNotEmpty() && newNickname != userNickname) {
            updateNicknameToServer(newNickname)
        }
    }

    // ============ 기능 3: 생년월일 캘린더 DatePicker ============
    private fun showCalendarDatePicker() {
        val parts = userBirthday.split(".")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: 2000
        val month = (parts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
        val day = parts.getOrNull(2)?.toIntOrNull() ?: 1

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.CustomDatePickerDialog,
            { _, selectedYear, selectedMonth, selectedDay ->
                val newBirthday = String.format("%04d.%02d.%02d", selectedYear, selectedMonth + 1, selectedDay)

                // 서버에 저장
                updateBirthToServer(newBirthday)
            },
            year,
            month,
            day
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    // ============ 기능 4: TimePicker 휠 형식 ============
    private fun showTimePickerDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_time_picker)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val pickerHour = dialog.findViewById<NumberPicker>(R.id.picker_hour)
        val pickerMinute = dialog.findViewById<NumberPicker>(R.id.picker_minute)
        val pickerSecond = dialog.findViewById<NumberPicker>(R.id.picker_second)
        val pickerAmPm = dialog.findViewById<NumberPicker>(R.id.picker_ampm)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)
        val btnConfirm = dialog.findViewById<Button>(R.id.btn_confirm)

        // 시간 파싱
        val timeParts = notificationTime.split(":")
        var hour24 = timeParts.getOrNull(0)?.toIntOrNull() ?: 22
        val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

        // 12시간제로 변환
        val isPM = hour24 >= 12
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }

        // 시 설정 (1-12)
        pickerHour.minValue = 1
        pickerHour.maxValue = 12
        pickerHour.value = hour12
        pickerHour.wrapSelectorWheel = true
        pickerHour.displayedValues = (1..12).map { String.format("%02d", it) }.toTypedArray()

        // 분 설정 (0-59)
        pickerMinute.minValue = 0
        pickerMinute.maxValue = 59
        pickerMinute.value = minute
        pickerMinute.wrapSelectorWheel = true
        pickerMinute.displayedValues = (0..59).map { String.format("%02d", it) }.toTypedArray()

        // 초 설정 (0-59)
        pickerSecond.minValue = 0
        pickerSecond.maxValue = 59
        pickerSecond.value = 0
        pickerSecond.wrapSelectorWheel = true
        pickerSecond.displayedValues = (0..59).map { String.format("%02d", it) }.toTypedArray()

        // AM/PM 설정
        pickerAmPm.minValue = 0
        pickerAmPm.maxValue = 1
        pickerAmPm.displayedValues = arrayOf("AM", "PM")
        pickerAmPm.value = if (isPM) 1 else 0
        pickerAmPm.wrapSelectorWheel = true

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            val selectedHour12 = pickerHour.value
            val selectedMinute = pickerMinute.value
            val selectedAmPm = pickerAmPm.value

            // 24시간제로 변환
            val selectedHour24 = when {
                selectedAmPm == 0 && selectedHour12 == 12 -> 0
                selectedAmPm == 1 && selectedHour12 == 12 -> 12
                selectedAmPm == 1 -> selectedHour12 + 12
                else -> selectedHour12
            }

            notificationTime = String.format("%02d:%02d", selectedHour24, selectedMinute)
            binding.tvNotificationTime.text = notificationTime

            // 서버에 저장
            updateNotificationToServer()

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateUI() {
        binding.switchNotification.isChecked = isNotificationEnabled
        binding.tvNotificationTime.text = notificationTime
        binding.tvName.text = userNickname  // userName -> userNickname
        binding.tvBirthday.text = userBirthday

        updateNotificationTimeState()
    }

    // 로그아웃 확인 다이얼로그
    private fun showLogoutConfirmDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("로그아웃")
            .setMessage("정말 로그아웃 하시겠어요?")
            .setPositiveButton("로그아웃") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        AuthRepository.logout()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    val prefs = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
                    prefs.edit().clear().apply()

                    TokenManager.clearAll()

                    val cookieManager = android.webkit.CookieManager.getInstance()
                    cookieManager.removeAllCookies(null)
                    cookieManager.flush()

                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
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
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val result = AuthRepository.withdraw()
                        result.onSuccess {
                            Toast.makeText(requireContext(), "회원탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()

                            val prefs = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
                            prefs.edit().clear().apply()
                            TokenManager.clearAll()

                            val cookieManager = android.webkit.CookieManager.getInstance()
                            cookieManager.removeAllCookies(null)
                            cookieManager.flush()

                            val intent = Intent(requireContext(), LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            requireActivity().finish()
                        }.onFailure { error ->
                            Toast.makeText(requireContext(), error.message ?: "탈퇴 실패", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }
}