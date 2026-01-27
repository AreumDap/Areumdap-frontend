package com.example.areumdap.UI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.graphics.Canvas
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.areumdap.R
import com.example.areumdap.RVAdapter.TaskRVAdapter
import com.example.areumdap.databinding.FragmentTaskBinding

class TaskFragment : Fragment() {
    lateinit var binding: FragmentTaskBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        val categories = listOf("전체", "진로", "관계", "자기성찰", "감정", "성장", "기타")

        val spinnerAdapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.item_spinner_text, // 스피너 평소 모습
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

        binding.taskSp.adapter = spinnerAdapter

        val testData = arrayListOf("자기 성찰") // 아이템 하나만 나오도록 설정
        val taskRVAdapter = TaskRVAdapter(testData)

        taskRVAdapter.itemClickListener = { data ->
            val dataTaskFragment = DataTaskFragment()

            val bundle = Bundle()
            bundle.putString("selected_task", data)
            dataTaskFragment.arguments = bundle

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm,dataTaskFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.taskListRv.apply {
            adapter = taskRVAdapter
            // 가로 스크롤 설정 (XML에 가로로 되어 있으므로)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        val limit = (54 * resources.displayMetrics.density)

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                taskRVAdapter.notifyItemChanged(viewHolder.adapterPosition)
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val cardView = viewHolder.itemView.findViewById<View>(R.id.task_card_view)

                    val currentX = cardView.translationX
                    var newX = if (isCurrentlyActive) dX else currentX

                    if (newX < -limit) newX = -limit
                    if (newX > 0f) newX = 0f

                    cardView.translationX = newX

                    // 손을 뗐을 때 고정 또는 복구
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
}
