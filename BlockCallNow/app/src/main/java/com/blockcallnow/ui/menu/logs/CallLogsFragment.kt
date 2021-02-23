package com.blockcallnow.ui.menu.logs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blockcallnow.databinding.FragmentCallLogsBinding

class CallLogsFragment : Fragment() {

    private lateinit var binding: FragmentCallLogsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCallLogsBinding.inflate(inflater, container, false)
        return binding.root
    }
}