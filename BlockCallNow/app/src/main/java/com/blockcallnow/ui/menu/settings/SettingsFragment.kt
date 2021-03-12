package com.blockcallnow.ui.menu.settings

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.blockcallnow.BuildConfig
import com.blockcallnow.R
import com.blockcallnow.ui.BlockingActivity
import com.blockcallnow.ui.base.BaseFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.layout_call_action.*

class SettingsFragment : BaseFragment() {

    var busyDivert = "*67*18553605717"

    //    var busyDivert = "*67*08553605717"
    var cancelBusyDivert = "67"
    var cancelAll = "#002#"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tv_activate_option?.setOnClickListener {
            CallActionSheet(object : CallAction {
                override fun onActivate() {
//                    showForwardDialog()
                    val finalNumber = busyDivert + Uri.encode("#")
                    val callIntent = Intent(Intent.ACTION_CALL)
                    callIntent.data = Uri.parse("tel:$finalNumber")
                    startActivity(callIntent)
                }

                override fun onDeactivate() {
                    cancelBusyDivert()
                }
            }).show(childFragmentManager, "call")
        }

        tv_blocking?.setOnClickListener {
            startActivity(Intent(mContext, BlockingActivity::class.java))
        }

        tv_support.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("test@example.com"))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Test subject")
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Test message")
            emailIntent.type = "message/rfc822"
            startActivity(Intent.createChooser(emailIntent, "Contact Support:"))
        }

        tv_rate.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
                    )
                )
            }
        }

        tv_affiliate.setOnClickListener {
            Toast.makeText(context, "Affiliate coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    interface CallAction {
        fun onActivate()
        fun onDeactivate()
    }

    class CallActionSheet(val listener: CallAction) : BottomSheetDialogFragment() {

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.layout_call_action, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            tv_activate?.setOnClickListener {
                listener.onActivate()
                dismiss()
            }
            tv_deactivate.setOnClickListener {
                listener.onDeactivate()
                dismiss()
            }
        }
    }

    fun cancelBusyDivert() {
        val finalNumber = Uri.encode("#") + cancelBusyDivert + Uri.encode("#")
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$finalNumber")
        startActivity(callIntent)
    }

    private fun showForwardDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle("Enter Number")

        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_PHONE
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val phoneNo = input.text.toString()
            if (phoneNo.isBlank()) {
                Toast.makeText(context, "Phone no can't be empty", Toast.LENGTH_SHORT)
                    .show()
            } else {
                //                    blockNumber(phoneNo)
                val finalNumber = busyDivert + phoneNo + Uri.encode("#")
                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.data = Uri.parse("tel:$finalNumber")
                startActivity(callIntent)
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }
}