package com.blockcallnow.ui.menu.logs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blockcallnow.databinding.FragmentCallLogsBinding
import com.blockcallnow.ui.base.BaseFragment
import com.blockcallnow.util.LogUtil

class CallLogsFragment : BaseFragment() {

    private lateinit var binding: FragmentCallLogsBinding
    private lateinit var callLogs: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCallLogsBinding.inflate(inflater, container, false)

        callLogs = binding.callLogs
        callLogs.layoutManager = LinearLayoutManager(activity)

        val viewModel = ViewModelProvider(this).get(LogsViewModel::class.java)

        viewModel.getCallLogs(myApp.db).observe(viewLifecycleOwner, Observer {
            if (it.isEmpty()) {
                binding.noLogs.visibility = View.VISIBLE
            } else {
                binding.noLogs.visibility = View.GONE
            }
            callLogs.adapter = LogAdapter(it)
        })

        return binding.root
    }
}