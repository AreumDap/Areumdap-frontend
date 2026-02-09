package com.example.areumdap.UI.auth

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.webkit.CookieManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.areumdap.data.repository.AuthRepository
import com.example.areumdap.data.source.TokenManager
import com.example.areumdap.data.repository.UserRepository
import com.example.areumdap.R
import com.example.areumdap.databinding.FragmentSettingBinding
import kotlinx.coroutines.launch
import java.util.Calendar

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    // 설정 데이터
    private var isNotificationEnabled = true
    private var notificationTime = "22:00"
    private var userNickname = "사용자"
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
        loadUserData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * 다이얼로그를 화면 하단(Bottom Sheet) 스타일로 설정하는 공통 함수
     */
    private fun configureBottomDialog(dialog: Dialog) {
        dialog.window?.apply {
            // 배경 투명하게 (XML 배경의 둥근 모서리 적용을 위해)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // 너비는 꽉 채우고, 높이는 내용물에 맞춤
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            // 위치를 화면 하단으로 고정
            setGravity(Gravity.BOTTOM)

            // 다이얼로그 주변 기본 여백 제거 (꽉 채우기 위해 중요)
            decorView.setPadding(0, 0, 0, 0)
        }
    }

    /**
     * 서버에서 사용자 데이터 불러오기
     */
    private fun loadUserData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = UserRepository.getProfile()

            result.onSuccess { profile ->
                if (!isAdded) return@onSuccess

                userNickname = profile.nickname ?: profile.name ?: "사용자"
                userBirthday = profile.birth?.replace("-", ".") ?: "2000.01.01"
                isNotificationEnabled = profile.notificationEnabled
                notificationTime = profile.pushNotificationTime ?: "22:00"

                updateUI()
                saveSettingsLocally()
            }.onFailure { error ->
                if (!isAdded) return@onFailure

                loadLocalData()
                Toast.makeText(requireContext(), "프로필을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 로컬에서 데이터 불러오기
     */
    private fun loadLocalData() {
        if (!isAdded) return

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
        if (!isAdded) return

        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("notification_enabled", isNotificationEnabled)
            .putString("notification_time", notificationTime)
            .putString("user_birthday", userBirthday)
            .apply()
    }

    private fun setupClickListeners() {
        // 알림 스위치
        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            isNotificationEnabled = isChecked
            updateNotificationTimeState()
            updateNotificationToServer()
        }

        // 푸시 알림 시간 (휠 다이얼로그)
        binding.layoutNotificationTime.setOnClickListener {
            if (isNotificationEnabled) {
                showTimePickerDialog()
            }
        }

        // 이름 변경 (다이얼로그)
        binding.layoutName.setOnClickListener {
            showNicknameDialog()
        }

        // 생년월일 변경 (휠 다이얼로그)
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

    // ============ 서버 연동 ============

    private fun updateNotificationToServer() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = UserRepository.updateNotification(isNotificationEnabled, notificationTime)

            result.onSuccess {
                saveSettingsLocally()
            }.onFailure { error ->
                if (isAdded) {
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateNicknameToServer(newNickname: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = UserRepository.updateNickname(newNickname)

            result.onSuccess {
                userNickname = newNickname
                binding.tvName.text = userNickname
                Toast.makeText(requireContext(), "닉네임이 변경되었습니다.", Toast.LENGTH_SHORT).show()
            }.onFailure { error ->
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateBirthToServer(birth: String) {
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

    // ============ UI 업데이트 ============

    private fun updateNotificationTimeState() {
        binding.layoutNotificationTime.isEnabled = isNotificationEnabled
        binding.layoutNotificationTime.alpha = if (isNotificationEnabled) 1.0f else 0.5f
    }

    private fun updateUI() {
        binding.switchNotification.isChecked = isNotificationEnabled
        binding.tvNotificationTime.text = notificationTime
        binding.tvName.text = userNickname
        binding.tvBirthday.text = userBirthday

        updateNotificationTimeState()
    }

    // ============ 닉네임 다이얼로그 ============

    private fun showNicknameDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_nickname)

        // 하단 고정 및 스타일 적용
        configureBottomDialog(dialog)

        // 키보드 올라올 때 다이얼로그도 같이 올라오게 설정
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        val etNickname = dialog.findViewById<EditText>(R.id.et_nickname)
        val btnClear = dialog.findViewById<ImageButton>(R.id.btn_clear)
        val btnConfirm = dialog.findViewById<Button>(R.id.btn_confirm)

        etNickname.setText(userNickname)
        etNickname.setSelection(etNickname.text.length)

        // X 버튼 표시/숨김
        btnClear.visibility = if (etNickname.text.isNotEmpty()) View.VISIBLE else View.GONE

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
            val newNickname = etNickname.text.toString().trim()
            if (newNickname.isNotEmpty()) {
                if (newNickname != userNickname) {
                    updateNicknameToServer(newNickname)
                }
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    // ============ 생년월일 다이얼로그 (휠 형식) ============

    private fun showDatePickerDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_date_picker)

        // 하단 고정 및 스타일 적용
        configureBottomDialog(dialog)

        val pickerYear = dialog.findViewById<NumberPicker>(R.id.picker_year)
        val pickerMonth = dialog.findViewById<NumberPicker>(R.id.picker_month)
        val pickerDay = dialog.findViewById<NumberPicker>(R.id.picker_day)
        val btnConfirm = dialog.findViewById<Button>(R.id.btn_confirm)

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        // 년도 설정
        pickerYear.minValue = 1900
        pickerYear.maxValue = currentYear
        pickerYear.wrapSelectorWheel = false

        // 월 설정
        pickerMonth.minValue = 1
        pickerMonth.maxValue = 12
        pickerMonth.displayedValues = (1..12).map { String.format("%02d", it) }.toTypedArray()

        // 일 설정
        pickerDay.minValue = 1
        pickerDay.maxValue = 31
        pickerDay.displayedValues = (1..31).map { String.format("%02d", it) }.toTypedArray()

        // 현재 값 설정
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
            val newBirthday = String.format("%04d.%02d.%02d", year, month, day)

            updateBirthToServer(newBirthday)
            dialog.dismiss()
        }

        dialog.show()
    }

    // ============ 시간 다이얼로그 (시:분만, 24시간제) ============

    private fun showTimePickerDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_time_picker)

        // 하단 고정 및 스타일 적용
        configureBottomDialog(dialog)

        val pickerHour = dialog.findViewById<NumberPicker>(R.id.picker_hour)
        val pickerMinute = dialog.findViewById<NumberPicker>(R.id.picker_minute)
        val btnConfirm = dialog.findViewById<Button>(R.id.btn_confirm)

        // 시간 파싱
        val timeParts = notificationTime.split(":")
        val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 22
        val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

        // 시 설정 (0-23)
        pickerHour.minValue = 0
        pickerHour.maxValue = 23
        pickerHour.value = hour
        pickerHour.displayedValues = (0..23).map { String.format("%02d", it) }.toTypedArray()

        // 분 설정 (0-59)
        pickerMinute.minValue = 0
        pickerMinute.maxValue = 59
        pickerMinute.value = minute
        pickerMinute.displayedValues = (0..59).map { String.format("%02d", it) }.toTypedArray()

        btnConfirm.setOnClickListener {
            val selectedHour = pickerHour.value
            val selectedMinute = pickerMinute.value

            notificationTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            binding.tvNotificationTime.text = notificationTime

            updateNotificationToServer()
            dialog.dismiss()
        }

        dialog.show()
    }

    // ============ 로그아웃 다이얼로그 (기본 알럿은 중앙 유지) ============

    private fun showLogoutConfirmDialog() {
        AlertDialog.Builder(requireContext())
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

                    val cookieManager = CookieManager.getInstance()
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

    // ============ 계정탈퇴 다이얼로그 (기본 알럿은 중앙 유지) ============

    private fun showDeleteAccountConfirmDialog() {
        AlertDialog.Builder(requireContext())
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

                            val cookieManager = CookieManager.getInstance()
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