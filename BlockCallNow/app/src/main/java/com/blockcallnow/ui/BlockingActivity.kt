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

        binding.toolbar.iv_back?.setOnClickListener { onBackPressed() }
//      binding.sw.isChecked = BlockCallsPref.getCallListEnable(mContext)
        binding.cbPrivateNumbers.isChecked = BlockCallsPref.getPvtNumOption(mContext)
        binding.cbUnknownNumber.isChecked = BlockCallsPref.getUnknownNum(mContext)
        binding.cbSpam.isChecked = BlockCallsPref.getSpamOption(mContext)
        binding.cbInternational.isChecked = BlockCallsPref.getInternationalOption(mContext)
        binding.cbBlockAllExceptContacts.isChecked =
            BlockCallsPref.getBlockAllExceptContactsOption(mContext)
        binding.cbBlockAll.isChecked = BlockCallsPref.getBlockAllOption(mContext)
        binding.cbVoip.isChecked = BlockCallsPref.getBlockAllVoipConnected(mContext)

        //Messages
        binding.cbMsgUnknownNumber.isChecked = BlockCallsPref.getMsgUnknownNumber(mContext)
        binding.cbMsgNonNumericNumber.isChecked = BlockCallsPref.getMsgNonNumericNUmber(mContext)

        binding.sw.setOnCheckedChangeListener(listener)
        binding.cbPrivateNumbers.setOnCheckedChangeListener(listener)
        binding.cbUnknownNumber.setOnCheckedChangeListener(listener)
        binding.cbSpam.setOnCheckedChangeListener(listener)
        binding.cbInternational.setOnCheckedChangeListener(listener)
        binding.cbBlockAllExceptContacts.setOnCheckedChangeListener(listener)
        binding.cbBlockAll.setOnCheckedChangeListener(listener)
        binding.cbVoip.setOnCheckedChangeListener(listener)
        //Messages
        binding.cbMsgNonNumericNumber.setOnCheckedChangeListener(listener)
        binding.cbMsgUnknownNumber.setOnCheckedChangeListener(listener)
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