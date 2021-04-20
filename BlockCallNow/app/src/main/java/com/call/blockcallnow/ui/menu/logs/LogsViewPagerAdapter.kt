package com.call.blockcallnow.ui.menu.logs

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class LogsViewPagerAdapter(private val titles: ArrayList<String>, val fragment: FragmentActivity) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return titles.size
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CallLogsFragment()
            1 -> MessageLogsFragment()
            else -> MessageLogsFragment()
        }
    }
}