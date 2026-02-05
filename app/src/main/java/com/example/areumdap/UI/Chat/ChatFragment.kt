package com.example.areumdap.UI.Chat

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.util.Log
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.example.areumdap.R
import com.example.areumdap.RVAdapter.ChatMessageRVAdapter
import com.example.areumdap.UI.Chat.data.ChatViewModel
import com.example.areumdap.UI.MainActivity
import com.example.areumdap.UI.PopUpDialogFragment
import kotlinx.coroutines.launch


class ChatFragment : Fragment(R.layout.fragment_chat) {
    private val vm: ChatViewModel by viewModels()
    private lateinit var adapter: ChatMessageRVAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ChatMessageRVAdapter()
        val prefill = arguments?.getString("prefill_question")
        val prefillQuestionId = arguments?.getLong("prefill_question_id", -1L) ?: -1L
        if(!prefill.isNullOrBlank()){
            vm.seedPrefillQuestion(prefill)
            arguments?.remove("prefill_question")

        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            showExitDialog()
        }



        if (!prefill.isNullOrBlank() && prefillQuestionId != -1L){
            vm.startChat(prefill, prefillQuestionId)
        }

        val rv = view.findViewById<RecyclerView>(R.id.chat_rv)
        rv.adapter = adapter
        rv.itemAnimator = null

        val et = view.findViewById<EditText>(R.id.et_chat_input)
        val btn = view.findViewById<ImageView>(R.id.send_btn)

        btn.setOnClickListener {
            vm.send(et.text.toString())
            et.setText("")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.message.collect { list ->
                        adapter.submitList(list) {
                            if (adapter.itemCount > 0) rv.scrollToPosition(adapter.itemCount - 1)
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
            onBackClick = { showExitDialog()}
        )


    }

    override fun onDestroyView() {
        (activity as? MainActivity)?.setToolbar(false)
        super.onDestroyView()
    }

    private fun showExitDialog(){
        val dialog = PopUpDialogFragment.newInstance(
            title = "대화를 종료하시겠어요?\n지금 나가면 과제를 받을 수 없어요.",
            subtitle = "",
            leftBtn = "취소",
            rightBtn = "나가기"
        )

        dialog.setCallback(object : PopUpDialogFragment.MyDialogCallback{
            override fun onConfirm() {
                vm.stopChatOnExit()
                parentFragmentManager.popBackStack()
            }
        })

        dialog.show(parentFragmentManager,"exit_chat_dialog")
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
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, ConversationSummaryFragment())
                        .addToBackStack(null)
                        .commit()
                }
            }
        })

        dialog.show(parentFragmentManager, "chat_end_dialog")
    }



}
