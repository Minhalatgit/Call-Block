package com.blockcallnow.util.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.blockcallnow.R
import kotlinx.android.synthetic.main.dialog_call_block.*

class CallBlockDialog : DialogFragment() {

    private val optionEvent = MutableLiveData<Int>()
    val optionNavEvent: LiveData<Int> = optionEvent

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#D93db3c3")));
        return inflater.inflate(R.layout.dialog_call_block, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_contacts?.setOnClickListener {
            optionEvent.value = 1
            dismiss()
        }
        tv_input?.setOnClickListener {
            optionEvent.value = 2
            dismiss()
        }
        iv_close?.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
        }
    }
}