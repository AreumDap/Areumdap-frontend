package com.example.areumdap.UI

import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.areumdap.R
import com.example.areumdap.RVAdapter.QuestionRVAdapter
import com.example.areumdap.databinding.FragmentQuestionBinding

class QuestionFragment : Fragment() {
    lateinit var binding: FragmentQuestionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        val categories = listOf("전체", "진로", "관계", "자기성찰", "감정", "성장", "기타")

        val spinnerAdapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.item_spinner_text,
            R.id.tv_spinner_item,
            categories
        ) {
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_spinner_dropdown, parent, false)
                val tv = v.findViewById<TextView>(R.id.tv_dropdown_item)
                tv.text = getItem(position)
                return v
            }
        }

        binding.questionSp.adapter = spinnerAdapter

        val testData = arrayListOf("자기 성찰") // 아이템 하나만 나오도록 설정
        val questionRVAdapter = QuestionRVAdapter(testData)

        binding.questionListRv.apply {
            adapter = questionRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
        // 카드 이동거리
        val limit = (54 * resources.displayMetrics.density)

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            private var isClamped = false

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                questionRVAdapter.notifyItemChanged(viewHolder.adapterPosition)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    recyclerView.parent.requestDisallowInterceptTouchEvent(true)

                    val cardView = viewHolder.itemView.findViewById<View>(R.id.task_card_view)

                    val currentTranslationX = cardView.translationX
                    var newX = if (isCurrentlyActive) dX else currentTranslationX

                    if (newX < -limit) newX = -limit
                    if (newX > 0f) newX = 0f

                    cardView.translationX = newX

                    if (!isCurrentlyActive) {
                        if (cardView.translationX <= -limit / 2) {
                            cardView.animate().translationX(-limit).setDuration(100).start()
                        } else {
                            cardView.animate().translationX(0f).setDuration(100).start()
                        }
                    }

                    // ViewPager 터치 방지
                    recyclerView.parent.requestDisallowInterceptTouchEvent(true)
                }
            }
            // 자동으로 안 넘어가게 함
            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = 2f
            // 빠르게 밀어도 안 날아가게 함
            override fun getSwipeEscapeVelocity(defaultValue: Float): Float = defaultValue * 10
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.questionListRv)

    }
}