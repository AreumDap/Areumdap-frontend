package com.example.areumdap.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.areumdap.UI.Task.EmptyTaskFragment
import com.example.areumdap.UI.Task.TaskListFragment

class TaskPageVPAdapter(fragment: Fragment, private val hasTask: Boolean): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 1

    override fun createFragment(position: Int): Fragment {
        return if(hasTask){
            TaskListFragment()
        } else {
            EmptyTaskFragment()
        }
    }
}