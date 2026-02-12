package com.example.areumdap.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.areumdap.UI.Task.QuestionFragment
import com.example.areumdap.UI.Task.TaskFragment
import com.example.areumdap.UI.Archive.ArchiveFragment

class ArchiveVPAdapter (fragment: ArchiveFragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> QuestionFragment()
            else -> TaskFragment()
        }
    }
}