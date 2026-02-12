package com.example.areumdap.UI.Chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupWindow
import androidx.activity.addCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.areumdap.R
import com.example.areumdap.UI.auth.MainActivity
import com.example.areumdap.UI.auth.PopUpDialogFragment
import com.example.areumdap.UI.auth.ToastDialogFragment
import com.example.areumdap.adapter.ChatMessageRVAdapter
import com.example.areumdap.data.model.ChatMessage
import com.example.areumdap.data.repository.UserRepository
import com.example.areumdap.data.source.TokenManager
import com.example.areumdap.databinding.FragmentChatBinding
import com.example.areumdap.databinding.ItemChatMenuBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private val vm: ChatViewModel by activityViewModels()
    private lateinit var adapter: ChatMessageRVAdapter

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private var menuPopup: PopupWindow? = null

    private fun showCustomToast(message: String, iconResId:Int) {
        val toast = ToastDialogFragment(message, iconResId)
        toast.show(requireActivity().supportFragmentManager, "custom_toast")
    }

    fun setInputRadius(editText: EditText, radiusDp: Float) {
        val radiusPx = radiusDp * editText.resources.displayMetrics.density

        val bg = editText.background
        val gd = (bg as? GradientDrawable)?.mutate() as? GradientDrawable
            ?: return

        gd.cornerRadius = radiusPx
        editText.background = gd
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChatBinding.bind(view)

        setBottomNavVisible(false)

        adapter = ChatMessageRVAdapter { anchor, msg ->
            showChatMenu(anchor, msg)
        }

        // prefill 처리
        val prefill = arguments?.getString("prefill_question")
        val prefillQuestionId = arguments?.getLong("prefill_question_id", -1L) ?: -1L
        val prefillTag = arguments?.getString("prefill_tag")

        if (!prefillTag.isNullOrBlank()) vm.setRecommendTag(prefillTag)

        if (!prefill.isNullOrBlank()) {
            if (prefillQuestionId != -1L) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val profileNick = runCatching { UserRepository.getProfile() }
                        .getOrNull()
                        ?.getOrNull()
                        ?.nickname
                    if (TokenManager.getUserNickname().isNullOrBlank()) {
                        if (!profileNick.isNullOrBlank()) {
                            TokenManager.saveNickname(profileNick)
                        }
                    }
                    vm.seedPrefillQuestion(prefill, profileNick)
                    vm.startChat(prefill, prefillQuestionId)
                    arguments?.remove("prefill_question")
                }
            } else {
                vm.seedQuestionOnly(prefill)
                arguments?.remove("prefill_question")
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            showExitDialog()
        }


        binding.chatRv.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.chatRv.adapter = adapter
        binding.chatRv.itemAnimator = null
        binding.etChatInput.addTextChangedListener {
            val r = if (binding.etChatInput.lineCount >= 3) 14f else 18f
            setInputRadius(binding.etChatInput, r)
        }

        binding.sendBtn.setOnClickListener {
            vm.send(binding.etChatInput.text.toString())
            binding.etChatInput.setText("")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.message.collect { list ->
                        adapter.submitList(list) {
                            if (adapter.itemCount > 0) {
                                binding.chatRv.post{
                                    binding?.chatRv?.scrollToPosition(list.lastIndex)
                                }

                            }
                        }
                    }
                }
                launch {
                    vm.endEvent.collect {
                        showChatEndDialog()
                    }
                }
            }
        }

        (activity as? MainActivity)?.setToolbar(
            visible = true,
            title = "대화",
            showBackButton = true,
            subText = "질문 길게 눌러 저장",
            onBackClick = { showExitDialog() }
        )
    }

    override fun onDestroyView() {
        setBottomNavVisible(true)
        menuPopup?.dismiss()
        menuPopup = null
        _binding = null
        (activity as? MainActivity)?.setToolbar(false)
        super.onDestroyView()
    }

    private fun showChatMenu(anchor: View, msg: ChatMessage) {
        menuPopup?.dismiss()

        val menuBinding = ItemChatMenuBinding.inflate(layoutInflater)

        val popup = PopupWindow(
            menuBinding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isOutsideTouchable = true
            elevation = 20f
            setOnDismissListener { menuPopup = null }
        }

        menuBinding.menuSave.setOnClickListener {
            popup.dismiss()
            val id = msg.chatHistoryId
            if (id == null) {
                android.util.Log.w("ChatFragment", "saveQuestion skipped: chatHistoryId is null")
                showCustomToast("저장할 수 없는 메시지예요.",R.drawable.ic_failure)
                return@setOnClickListener
            }
            vm.saveQuestion(id)
            showCustomToast("저장되었습니다",R.drawable.ic_success)
        }

        menuBinding.menuCopy.setOnClickListener {
            popup.dismiss()
            copyToClipboard(msg.text)
        }

        // 말풍선 위에 뜨게
        val menuWidth = (92 * resources.displayMetrics.density).toInt()
        val xOffset = anchor.width - menuWidth
        val yOffset = (-30 * resources.displayMetrics.density).toInt()
        popup.showAsDropDown(anchor, xOffset, yOffset)

        menuPopup = popup
    }

    private fun copyToClipboard(text: String) {
        val cm = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("chat", text))
    }

    private fun showExitDialog() {
        val dialog = PopUpDialogFragment.newInstance(
            title = "대화를 종료하시겠어요?\n지금 나가면 과제를 받을 수 없어요.",
            subtitle = "",
            leftBtn = "취소",
            rightBtn = "나가기"
        )

        dialog.setCallback(object : PopUpDialogFragment.MyDialogCallback {
            override fun onConfirm() {
                viewLifecycleOwner.lifecycleScope.launch {
                    android.util.Log.d("ChatExit", "confirm exit clicked")
                    vm.stopChatOnExit()
                    vm.resetChatSession()
                    parentFragmentManager.popBackStack()
                }
            }
        })

        dialog.show(parentFragmentManager, "exit_chat_dialog")
    }

    private fun showChatEndDialog() {
        val dialog = PopUpDialogFragment.newInstance(
            title = "대화가 종료되었어요!\n자기 성찰 과제를 받아볼 수 있어요.",
            subtitle = "과제를 완성하면 캐릭터를 성장시킬 수 있어요.",
            leftBtn = "대화 이어가기",
            rightBtn = "과제 확인하기"
        )

        dialog.setCallback(object : PopUpDialogFragment.MyDialogCallback {
            override fun onConfirm() {
                view?.post {
                    val endedThreadId = vm.getLastEndedThreadId() ?: vm.getThreadId()
                    vm.resetChatSession(endedThreadId)
                    val summaryFragment = ConversationSummaryFragment().apply {
                        arguments = Bundle().apply {
                            val tag = vm.getLastRecommendTag()
                            if (!tag.isNullOrBlank()) putString("recommend_tag", tag)
                        }
                    }
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, summaryFragment)
                        .addToBackStack(null)
                        .commit()
                }
            }

            override fun onCancel() {
                vm.resumeChatSession()
            }
        })

        dialog.show(parentFragmentManager, "chat_end_dialog")
    }

    private fun setBottomNavVisible(visible:Boolean){
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.main_bnv)
        bottomNav.visibility = if (visible) View.VISIBLE else View.GONE
    }


}
