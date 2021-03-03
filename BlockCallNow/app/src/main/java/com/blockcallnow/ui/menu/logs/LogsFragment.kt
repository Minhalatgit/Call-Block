package com.blockcallnow.ui.menu.logs

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.blockcallnow.R
import com.blockcallnow.app.BlockCallApplication
import com.blockcallnow.databinding.FragmentLogsBinding
import com.blockcallnow.ui.base.BaseFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_logs.*

class LogsFragment : BaseFragment() {

    private lateinit var binding: FragmentLogsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
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