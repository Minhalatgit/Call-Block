package com.call.blockcallnow.ui.menu.logs

import android.os.Bundle
import android.util.Log
import android.view.*
import com.call.blockcallnow.R
import com.call.blockcallnow.databinding.FragmentLogsBinding
import com.call.blockcallnow.ui.base.BaseFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class LogsFragment : BaseFragment() {

    private lateinit var binding: FragmentLogsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        Log.d("LogsFragment", "onCreate: ")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLogsBinding.inflate(inflater, container, false)
        Log.d("LogsFragment", "onCreateView: ")

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.d("LogsFragment", "onCreateOptionsMenu: ")
        inflater.inflate(R.menu.logs_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear -> {
                myApp.db.contactDao().deleteAllLogs()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}