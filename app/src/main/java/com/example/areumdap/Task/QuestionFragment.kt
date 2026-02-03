package com.example.areumdap.Task

import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.areumdap.Network.RetrofitClient
import com.example.areumdap.R
import com.example.areumdap.RVAdapter.QuestionRVAdapter
import com.example.areumdap.databinding.FragmentQuestionBinding

class QuestionFragment : Fragment() {
    private var _binding: FragmentQuestionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(RetrofitClient.taskService)
    }

    private lateinit var questionRVAdapter: QuestionRVAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        setupSpinner()
        setupRecyclerView()
        setupObservers()

        // 초기 데이터 로드
        viewModel.fetchSavedQuestions(tag = null)
    }

    private fun setupObservers() {
        // 저장한 질문 목록 관찰
        viewModel.savedQuestions.observe(viewLifecycleOwner) { questions ->
            questions?.let{
                binding.questionTotalTv.text = "${it.size}"

                questionRVAdapter.updateData(it)
            }
        }

        // 에러 메시지 관찰
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        questionRVAdapter = QuestionRVAdapter(arrayListOf())

        binding.questionListRv.apply {
            adapter = questionRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        setupSwipe()
    }
    private fun setupSpinner() {
        val categories = listOf("전체", "진로", "관계", "자기성찰", "감정", "성장", "기타")

        val tagMap = mapOf(
            "전체" to null,
            "진로" to "CAREER",
            "관계" to "RELATIONSHIP",
            "자기성찰" to "SELF_REFLECTION",
            "감정" to "EMOTION",
            "성장" to "GROWTH",
            "기타" to "ETC"
        )

        val spinnerAdapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.item_spinner_text,
            R.id.tv_spinner_item,
            categories
        ) {
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.item_spinner_dropdown, parent, false)
                val tv = v.findViewById<TextView>(R.id.tv_dropdown_item)
                tv.text = getItem(position)
                return v
            }
        }

        binding.questionSp.adapter = spinnerAdapter

        // 스피너 선택 리스너
        binding.questionSp.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = categories[position]
                val tag = tagMap[selectedCategory]

                viewModel.fetchSavedQuestions(tag = tag)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupSwipe() {
        val limit = (54 * resources.displayMetrics.density)

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    questionRVAdapter.notifyItemChanged(pos)
                }
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
                }
            }

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = 2f
            override fun getSwipeEscapeVelocity(defaultValue: Float): Float = defaultValue * 10
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.questionListRv)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}