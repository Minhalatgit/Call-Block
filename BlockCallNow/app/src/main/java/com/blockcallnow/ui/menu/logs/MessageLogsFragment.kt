package com.blockcallnow.ui.menu.logs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blockcallnow.databinding.FragmentCallLogsBinding
import com.blockcallnow.databinding.FragmentMessageLogsBinding
import com.blockcallnow.ui.base.BaseFragment
import com.blockcallnow.util.LogUtil

class MessageLogsFragment : BaseFragment() {

    private lateinit var binding: FragmentCallLogsBinding
    private lateinit var messageLogs: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCallLogsBinding.inflate(inflater, container, false)

        messageLogs = binding.callLogs
        messageLogs.layoutManager = LinearLayoutManager(activity)

        val viewModel = ViewModelProvider(this).get(LogsViewModel::class.java)

        viewModel.getMessageLogs(myApp.db).observe(viewLifecycleOwner, Observer {
            LogUtil.e("CallLogsFragment", it.toString())
            messageLogs.adapter = LogAdapter(it)
        })

        return binding.root
    }
}