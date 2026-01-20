package com.example.areumdap.VPAdapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.areumdap.UI.ArchiveFragment
import com.example.areumdap.UI.QuestionFragment
import com.example.areumdap.UI.TaskFragment

class ArchiveVPAdapter (fragment: ArchiveFragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> QuestionFragment()
            else -> TaskFragment()
        }
    }
}