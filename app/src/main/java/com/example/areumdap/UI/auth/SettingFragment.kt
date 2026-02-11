package com.example.areumdap.UI.auth

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.areumdap.R
import com.example.areumdap.data.repository.AuthRepository
import com.example.areumdap.data.repository.UserRepository
import com.example.areumdap.data.source.TokenManager
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

        // 1. 로컬 데이터 먼저 로드
        loadLocalData()

        // 2. 리스너 설정
        setupClickListeners()

        // 3. 서버 데이터 로드
        loadUserData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ============ 커스텀 토스트 함수 (수정됨) ============
    /**
     * ToastDialogFragment를 액티비티 위에 띄우는 헬퍼 함수
     * DataTaskFragment와 동일하게 requireActivity().supportFragmentManager 사용
     */
    private fun showCustomToast(message: String, iconRes: Int = R.drawable.ic_success) {
        // 프래그먼트가 붙어있지 않으면 실행 중단
        if (!isAdded || activity == null) return

        val toast = ToastDialogFragment(message, iconRes)
        // parentFragmentManager 대신 액티비티의 매니저를 사용하여 최상단에 표시
        toast.show(requireActivity().supportFragmentManager, "CustomToast")
    }

    // ============ 초기화 및 설정 ============

    private fun configureBottomDialog(dialog: Dialog) {
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.BOTTOM)
            decorView.setPadding(0, 0, 0, 0)
        }
    }

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
                // 필요 시 에러 토스트 활성화 (아이콘은 ic_error 등으로 변경 필요)
                // showCustomToast("프로필 로드 실패", R.drawable.ic_error)
            }
        }
    }

    private fun loadLocalData() {
        if (!isAdded) return

        userNickname = TokenManager.getUserNickname() ?: TokenManager.getUserName() ?: "사용자"

        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        isNotificationEnabled = prefs.getBoolean("notification_enabled", true)
        notificationTime = prefs.getString("notification_time", "22:00") ?: "22:00"
        userBirthday = prefs.getString("user_birthday", "2000.01.01") ?: "2000.01.01"

        updateUI()
    }

    private fun saveSettingsLocally() {
        if (!isAdded) return

        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("notification_enabled", isNotificationEnabled)
            .putString("notification_time", notificationTime)
            .putString("user_birthday", userBirthday)
            .apply()
    }

    // ============ UI 업데이트 ============

    private fun updateUI() {
        if (_binding == null) return

        binding.switchNotification.setOnCheckedChangeListener(null)

        binding.switchNotification.isChecked = isNotificationEnabled
        binding.tvNotificationTime.text = notificationTime
        binding.tvName.text = userNickname
        binding.tvBirthday.text = userBirthday

        updateNotificationTimeState()

        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            isNotificationEnabled = isChecked
            updateNotificationTimeState()
            updateNotificationToServer()
        }
    }

    private fun updateNotificationTimeState() {
        val alpha = if (isNotificationEnabled) 1.0f else 0.5f
        binding.layoutNotificationTime.alpha = alpha
        binding.layoutNotificationTime.isClickable = isNotificationEnabled
    }

    // ============ 클릭 리스너 설정 ============

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun setupClickListeners() {
        binding.layoutNotificationTime.setOnClickListener {
            if (isNotificationEnabled) showTimePickerDialog()
        }

        binding.layoutName.setOnClickListener { showNicknameDialog() }
        binding.layoutBirthday.setOnClickListener { showDatePickerDialog() }

        binding.layoutServiceIntro.setOnClickListener { openUrl("https://areumdap.notion.site/1f08d65f6acf8009952ed1e1a76b0b3a") }
        binding.layoutPrivacyPolicy.setOnClickListener { openUrl("https://areumdap.notion.site/1f08d65f6acf807c942ef5bf97c753c2") }
        binding.layoutTerms.setOnClickListener { openUrl("https://areumdap.notion.site/1f08d65f6acf8085b0e5c00b9d6d8cd2") }

        binding.layoutLogout.setOnClickListener { showLogoutConfirmDialog() }
        binding.layoutDeleteAccount.setOnClickListener { showDeleteAccountConfirmDialog() }
    }

    // ============ 서버 연동 함수들 ============

    private fun updateNotificationToServer() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = UserRepository.updateNotification(isNotificationEnabled, notificationTime)
            result.onSuccess {
                saveSettingsLocally()
            }.onFailure { error ->
                if (isAdded) {
                    showCustomToast(error.message ?: "알림 설정 저장 실패", R.drawable.ic_error) // ic_error 확인 필요
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
                showCustomToast("닉네임이 변경되었습니다.")
            }.onFailure { error ->
                showCustomToast(error.message ?: "닉네임 변경 실패", R.drawable.ic_error)
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
                showCustomToast("생년월일이 변경되었습니다.")
            }.onFailure { error ->
                showCustomToast(error.message ?: "생년월일 변경 실패", R.drawable.ic_error)
            }
        }
    }

    // ============ 다이얼로그들 (닉네임, 날짜, 시간) ============

    private fun showNicknameDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_nickname)

        configureBottomDialog(dialog)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        val etNickname = dialog.findViewById<EditText>(R.id.et_nickname)
        val btnClear = dialog.findViewById<ImageButton>(R.id.btn_clear)
        val btnConfirm = dialog.findViewById<Button>(R.id.btn_confirm)

        etNickname.setText(userNickname)
        etNickname.setSelection(etNickname.text.length)

        val (colorActive, colorInactive) = getCurrentSeasonColors()

        fun updateButtonState() {
            val input = etNickname.text.toString().trim()
            val isChanged = input.isNotEmpty() && input != userNickname

            if (isChanged) {
                btnConfirm.backgroundTintList = ColorStateList.valueOf(colorActive)
                btnConfirm.isEnabled = true
                btnConfirm.setTextColor(Color.WHITE)
            } else {
                btnConfirm.backgroundTintList = ColorStateList.valueOf(colorInactive)
                btnConfirm.isEnabled = false
                btnConfirm.setTextColor(Color.WHITE)
            }
        }

        updateButtonState()
        btnClear.visibility = if (etNickname.text.isNotEmpty()) View.VISIBLE else View.GONE

        etNickname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                btnClear.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                updateButtonState()
            }
        })

        btnClear.setOnClickListener { etNickname.text.clear() }
        btnConfirm.setOnClickListener {
            val newNickname = etNickname.text.toString().trim()
            if (newNickname.isNotEmpty() && newNickname != userNickname) {
                updateNicknameToServer(newNickname)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun getCurrentSeasonColors(): Pair<Int, Int> {
        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val season = prefs.getString("SEASON", "SPRING")?.uppercase() ?: "SPRING"
        val (resId1, resId2) = when (season) {
            "SPRING" -> Pair(R.color.pink2, R.color.pink1)
            "SUMMER" -> Pair(R.color.green2, R.color.green1)
            "FALL" -> Pair(R.color.yellow2, R.color.yellow1)
            "WINTER" -> Pair(R.color.blue2, R.color.blue1)
            else -> Pair(R.color.pink2, R.color.pink1)
        }
        return Pair(ContextCompat.getColor(requireContext(), resId1), ContextCompat.getColor(requireContext(), resId2))
    }

    private fun showDatePickerDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_date_picker)
        configureBottomDialog(dialog)

        val pickerYear = dialog.findViewById<NumberPicker>(R.id.picker_year)
        val pickerMonth = dialog.findViewById<NumberPicker>(R.id.picker_month)
        val pickerDay = dialog.findViewById<NumberPicker>(R.id.picker_day)
        val btnConfirm = dialog.findViewById<Button>(R.id.btn_confirm)

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        pickerYear.minValue = 1900
        pickerYear.maxValue = currentYear
        pickerYear.wrapSelectorWheel = false
        pickerMonth.minValue = 1
        pickerMonth.maxValue = 12
        pickerDay.minValue = 1
        pickerDay.maxValue = 31

        val parts = userBirthday.split(".")
        if (parts.size == 3) {
            pickerYear.value = parts[0].toIntOrNull() ?: 2000
            pickerMonth.value = parts[1].toIntOrNull() ?: 1
            pickerDay.value = parts[2].toIntOrNull() ?: 1
        }

        btnConfirm.setOnClickListener {
            val newBirthday = String.format("%04d.%02d.%02d", pickerYear.value, pickerMonth.value, pickerDay.value)
            updateBirthToServer(newBirthday)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showTimePickerDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_time_picker)
        configureBottomDialog(dialog)

        val pickerHour = dialog.findViewById<NumberPicker>(R.id.picker_hour)
        val pickerMinute = dialog.findViewById<NumberPicker>(R.id.picker_minute)
        val btnConfirm = dialog.findViewById<Button>(R.id.btn_confirm)

        val timeParts = notificationTime.split(":")
        pickerHour.minValue = 0
        pickerHour.maxValue = 23
        pickerHour.value = timeParts.getOrNull(0)?.toIntOrNull() ?: 22
        pickerMinute.minValue = 0
        pickerMinute.maxValue = 59
        pickerMinute.value = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

        btnConfirm.setOnClickListener {
            notificationTime = String.format("%02d:%02d", pickerHour.value, pickerMinute.value)
            binding.tvNotificationTime.text = notificationTime
            updateNotificationToServer()
            dialog.dismiss()
        }
        dialog.show()
    }

    // ============ 로그아웃 / 탈퇴 다이얼로그 ============

    private fun showLogoutConfirmDialog() {
        val dialog = PopUpDialogFragment.newInstance(
            title = "서비스를 로그아웃하시겠어요?",
            subtitle = "지금까지의 기록은 모두 저장 돼요.",
            leftBtn = "뒤로 가기",
            rightBtn = "종료하기"
        )
        dialog.setCallback(object : PopUpDialogFragment.MyDialogCallback {
            override fun onConfirm() { performLogout() }
        })
        dialog.show(parentFragmentManager, "LogoutDialog")
    }

    private fun performLogout() {
        viewLifecycleOwner.lifecycleScope.launch {
            try { AuthRepository.logout() } catch (e: Exception) { e.printStackTrace() }
            clearLocalDataAndGoToLogin()
        }
    }

    private fun showDeleteAccountConfirmDialog() {
        val dialog = PopUpDialogFragment.newInstance(
            title = "서비스를 정말로 탈퇴하시겠어요?",
            subtitle = "탈퇴한 계정은 다시 복구할 수 없어요.",
            leftBtn = "뒤로 가기",
            rightBtn = "종료하기"
        )
        dialog.setCallback(object : PopUpDialogFragment.MyDialogCallback {
            override fun onConfirm() { performDeleteAccount() }
        })
        dialog.show(parentFragmentManager, "DeleteAccountDialog")
    }

    private fun performDeleteAccount() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = AuthRepository.withdraw()
                result.onSuccess {
                    showCustomToast("회원탈퇴가 완료되었습니다.")
                    // 약간의 딜레이 후 이동하면 토스트가 더 잘 보일 수 있음
                    // handler.postDelayed({ clearLocalDataAndGoToLogin() }, 1000)
                    clearLocalDataAndGoToLogin()
                }.onFailure { error ->
                    showCustomToast(error.message ?: "탈퇴 실패", R.drawable.ic_error)
                }
            } catch (e: Exception) {
                showCustomToast("오류 발생: ${e.message}", R.drawable.ic_error)
            }
        }
    }

    private fun clearLocalDataAndGoToLogin() {
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