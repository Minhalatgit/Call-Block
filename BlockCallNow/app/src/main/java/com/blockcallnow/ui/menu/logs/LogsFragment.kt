package com.blockcallnow.ui.menu.logs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blockcallnow.R
import com.blockcallnow.databinding.FragmentLogsBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class LogsFragment : Fragment() {

    private lateinit var binding: FragmentLogsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLogsBinding.inflate(inflater, container, false)

        val list = arrayListOf("Calls", "Messages")
        binding.pager.adapter = LogsViewPagerAdapter(list, requireActivity())

        TabLayoutMediator(
            binding.tab,
            binding.pager,
            TabLayoutMediator.TabConfigurationStrategy { tab: TabLayout.Tab, position: Int ->
                tab.text = list[position]
            }).attach()

        return binding.root
    }

}