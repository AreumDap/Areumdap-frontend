package com.example.areumdap.Task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.graphics.Canvas
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.fragment.app.viewModels
import com.example.areumdap.Network.RetrofitClient
import com.example.areumdap.R
import com.example.areumdap.RVAdapter.TaskRVAdapter
import com.example.areumdap.databinding.FragmentTaskBinding

class TaskFragment : Fragment() {
    private var _binding: FragmentTaskBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(RetrofitClient.taskService)
    }

    private lateinit var taskRVAdapter: TaskRVAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinner()
        setupRecyclerView()
        setupObservers()

        // 초기 데이터 로드 (CAREER 태그로)
        viewModel.fetchCompletedMissions(tag = "CAREER")
    }

    private fun setupSpinner() {
        val categories = listOf("전체", "진로", "관계", "자기성찰", "감정", "성장", "기타")

        // 태그 매핑
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
        binding.taskSp.adapter = spinnerAdapter

        // 스피너 선택 리스너
        binding.taskSp.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = categories[position]
                val tag = tagMap[selectedCategory]

                // 선택된 태그로 데이터 다시 로드
                viewModel.fetchCompletedMissions(tag = tag)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
            }
        }
    }

    private fun setupRecyclerView() {
        // 초기에는 빈 리스트로 어댑터 생성
        taskRVAdapter = TaskRVAdapter(arrayListOf())

        taskRVAdapter.itemClickListener = { missionItem ->
            val dataTaskFragment = DataTaskFragment()
            val bundle = Bundle()
            bundle.putInt("missionId", missionItem.missionId)
            dataTaskFragment.arguments = bundle

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, dataTaskFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.taskListRv.apply {
            adapter = taskRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        setupSwipeToDelete()
    }

    private fun setupObservers() {
        // 완료된 과제 목록 관찰
        viewModel.completedMissions.observe(viewLifecycleOwner) { missionList ->
            missionList?.let {
                Log.d("FRAGMENT_OBSERVER", "데이터 전달받음: ${it.size}개")
                taskRVAdapter.updateData(it)
            }
        }

        // 로딩 상태 관찰
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
        }

        // 에러 메시지 관찰
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSwipeToDelete() {
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
                    taskRVAdapter.notifyItemChanged(pos)
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
                    val cardView = viewHolder.itemView.findViewById<View>(R.id.task_card_view)

                    val currentX = cardView.translationX
                    var newX = if (isCurrentlyActive) dX else currentX

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
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.taskListRv)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
