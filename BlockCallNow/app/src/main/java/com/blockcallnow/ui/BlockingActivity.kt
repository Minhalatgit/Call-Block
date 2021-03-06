package com.blockcallnow.ui

import android.os.Bundle
import android.widget.CompoundButton
import com.blockcallnow.R
import com.blockcallnow.data.preference.BlockCallsPref
import com.blockcallnow.databinding.ActivityBlockingBinding
import com.blockcallnow.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_blocking.view.*

class BlockingActivity : BaseActivity() {

    private val binding: ActivityBlockingBinding by binding(R.layout.activity_blocking)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            toolbar.iv_back?.setOnClickListener { onBackPressed() }
            //sw.isChecked = BlockCallsPref.getCallListEnable(mContext)
            cbPrivateNumbers.isChecked = BlockCallsPref.getPvtNumOption(mContext)
            cbUnknownNumber.isChecked = BlockCallsPref.getUnknownNum(mContext)
            cbSpam.isChecked = BlockCallsPref.getSpamOption(mContext)
            cbInternational.isChecked = BlockCallsPref.getInternationalOption(mContext)
            cbBlockAllExceptContacts.isChecked =
                BlockCallsPref.getBlockAllExceptContactsOption(mContext)
            cbBlockAll.isChecked = BlockCallsPref.getBlockAllOption(mContext)
            cbVoip.isChecked = BlockCallsPref.getBlockAllVoipConnected(mContext)

            //Messages
            cbMsgUnknownNumber.isChecked = BlockCallsPref.getMsgUnknownNumber(mContext)
            cbMsgNonNumericNumber.isChecked = BlockCallsPref.getMsgNonNumericNUmber(mContext)

            //Call listener
            sw.setOnCheckedChangeListener(listener)
            cbPrivateNumbers.setOnCheckedChangeListener(listener)
            cbUnknownNumber.setOnCheckedChangeListener(listener)
            cbSpam.setOnCheckedChangeListener(listener)
            cbInternational.setOnCheckedChangeListener(listener)
            cbBlockAllExceptContacts.setOnCheckedChangeListener(listener)
            cbBlockAll.setOnCheckedChangeListener(listener)
            cbVoip.setOnCheckedChangeListener(listener)

            //Messages listener
            cbMsgNonNumericNumber.setOnCheckedChangeListener(listener)
            cbMsgUnknownNumber.setOnCheckedChangeListener(listener)
        }
    }

    val listener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        when (buttonView.id) {
            R.id.sw -> {
//                BlockCallsPref.setCallListEnable(mContext, isChecked)
            }
            R.id.cb_private_numbers -> {
                BlockCallsPref.setPvtNumOption(mContext, isChecked)
            }
            R.id.cb_unknown_number -> {
                BlockCallsPref.setUnknownNum(mContext, isChecked)
            }
            R.id.cb_spam -> {
                BlockCallsPref.setSpamOption(mContext, isChecked)
            }
            R.id.cb_international -> {
                BlockCallsPref.setInternationalOption(mContext, isChecked)
            }
            R.id.cb_block_all_except_contacts -> {
                BlockCallsPref.setBlockAllExceptContactsOption(mContext, isChecked)
            }
            R.id.cb_block_all -> {
                BlockCallsPref.setBlockAllOption(mContext, isChecked)
            }
            R.id.cb_voip -> {
                BlockCallsPref.setBlockAllVoipConnected(mContext, isChecked)
            }
            R.id.cb_msg_unknown_number -> {
                BlockCallsPref.setMsgUnknownNumber(mContext, isChecked)
            }
            R.id.cb_msg_non_numeric_number -> {
                BlockCallsPref.setMsgNonNumericNUmber(mContext, isChecked)
            }
        }
    }
}