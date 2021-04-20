package com.call.blockcallnow.ui.menu.home

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.call.blockcallnow.R
import com.call.blockcallnow.adapter.BlockListAdapter
import com.call.blockcallnow.data.event.BaseNavEvent
import com.call.blockcallnow.data.model.BlockNoListResponse
import com.call.blockcallnow.data.model.LoginResponse
import com.call.blockcallnow.data.preference.LoginPref
import com.call.blockcallnow.data.room.BlockContact
import com.call.blockcallnow.ui.base.BaseActivity
import com.call.blockcallnow.ui.base.BaseFragment
import com.call.blockcallnow.ui.main.BlockViewModel
import com.call.blockcallnow.util.Utils
import com.call.blockcallnow.util.toast
import kotlinx.android.synthetic.main.fragment_blocked_list.*

class BlockedListFragment : BaseFragment() {

    lateinit var blockViewModel: BlockViewModel
    var blockContact: BlockContact? = null
    private lateinit var adapter: BlockListAdapter

    private val RC_CONTACT: Int = 10001
    private val RC_CONTACT_CHANGE: Int = 1004

    private val TAG: String? = BlockedListFragment::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        blockViewModel = ViewModelProvider(this).get(BlockViewModel::class.java)
        blockViewModel.unblockNavEvent.observe(this, unblockObserver)
        blockViewModel.blockNoDBListNavEvent.observe(this, Observer {
            setData(it)
        })
        blockViewModel.blockNoListNavEvent.observe(this, blockNos)
        blockViewModel.loginNavEvent.observe(this, userObserver)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_blocked_list, container, false)
    }

    private val userObserver = Observer<BaseNavEvent<LoginResponse?>> {
        when (it) {
            is BaseNavEvent.StartLoading -> {
                dialog.show(mContext)
            }
            is BaseNavEvent.StopLoading -> {
                dialog.dialog.cancel()
            }
            is BaseNavEvent.Success -> {
                it.data?.data?.let { user ->
                    user.user?.let { detail ->
                        LoginPref.setLoginObject(mContext, detail)
                        (activity as BaseActivity).setUserDetail()
                        userDetail = LoginPref.getLoginObject(mContext)
                        if (userDetail?.is_expired == true) {

                            cl_plan_detail?.visibility = View.VISIBLE
                            tv_plan_msg.text =
                                getString(R.string.msg_your_plan_has_expired_please_upgrade_to_use_full_functionality)
                            tv_protection.text = getString(R.string.label_disabled)
                        } else if (userDetail?.paywhirl_plan_id == Utils.PLAN_TRIAL) {
                            cl_plan_detail?.visibility = View.VISIBLE
                            tv_plan_msg.text =
                                "You are on trial period. Please upgrade to unlock pro features."
                            tv_protection.text = "Enabled"
                        }
                        getData()
                    }
                }
            }

            is BaseNavEvent.Error -> {
                it.message?.let {
                    toast(it)
                }
            }
            is BaseNavEvent.ShowMessage -> {
                it.message?.let {
                    toast(it)
                }
            }
            is BaseNavEvent.JsonParseException -> {
                it.throwable?.message?.let { it1 -> toast(it1) }
            }
            is BaseNavEvent.NetWorkException -> {
                it.throwable?.message?.let { it1 -> toast(it1) }
            }
            is BaseNavEvent.UnKnownException -> {
                it.throwable?.message?.let { it1 -> toast(it1) }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == RC_CONTACT_CHANGE) {
            getData()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_name?.text = userDetail?.name?.capitalize()
        btn_upgrade?.setOnClickListener {
            Utils.openLink(
                mContext,
                userDetail?.renewal_url ?: "https://www.blockcallsnow.com/pricing/"
            )
        }
        rv_block_list?.isNestedScrollingEnabled = false

        adapter = BlockListAdapter()
        adapter.listener = unblockListener

        rv_block_list?.adapter = adapter
        getUserProfile()
    }

    private fun getUserProfile() {
        blockViewModel.getUserProfile(token)
    }

    fun getData() {
        val list = myApp.db.contactDao().getAllBlockedContacts()
        if (list.isNullOrEmpty()) {
            adapter.contactsList?.clear()
            adapter.notifyDataSetChanged()
            blockViewModel.getBlockNoList(token, myApp.db)
        } else {
            setData(list)
        }

//            blockViewModel.getBlockNoList(token, myApp.db)
//        val list = myApp.db.contactDao().getAllBlockedContacts()
//        setData(list)
    }

    fun setData(list: List<BlockContact>?) {
        dialog.dialog.dismiss()
        adapter.contactsList = list?.toMutableList()
        adapter.notifyDataSetChanged()
    }

    private val unblockListener = object : BlockListAdapter.UnBlockListener {
        override fun onUnBlock(blockContact: BlockContact?) {
            blockContact?.let {
                AlertDialog.Builder(mContext)
                    .setMessage("You are about to unblock " + it.number + ". Are you sure?")
                    .setPositiveButton("Yes") { _, _ -> unblock(it) }
                    .setNegativeButton("Cancel", null)
                    .create().show()
            }
        }

        override fun showDetail(blockContact: BlockContact?) {
            startActivityForResult(
                Intent(mContext, BlockContactDetail::class.java)
                    .putExtra("name", blockContact?.name ?: "Unknown")
                    .putExtra("phone", blockContact?.number)
                    .putExtra("block_number", blockContact?.blockNumber)
                    .putExtra("isEdit", true)
                , RC_CONTACT_CHANGE
            )
        }
    }

    private fun unblock(blockContact: BlockContact) {
        this.blockContact = blockContact
        blockViewModel.unblock(token, blockContact.number)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            BlockedListFragment().apply {
            }
    }

    private val blockNos = Observer<BaseNavEvent<BlockNoListResponse?>> {
        when (it) {
            is BaseNavEvent.StartLoading -> {
                dialog.show(mContext)
            }
            is BaseNavEvent.StopLoading -> {
                dialog.dialog.cancel()
            }
            is BaseNavEvent.Success -> {
                if (!it.data?.data?.block_list.isNullOrEmpty()) {
                    blockViewModel.saveDataToDb(myApp.db, it.data?.data?.block_list)
                }
            }
            is BaseNavEvent.Error -> {
                it.message?.let {
                    toast(it)
                }
            }
            is BaseNavEvent.ShowMessage -> {
                it.message?.let {
                    toast(it)
                }
            }
            is BaseNavEvent.JsonParseException -> {
                it.throwable?.message?.let { it1 -> toast(it1) }
            }
            is BaseNavEvent.NetWorkException -> {
                it.throwable?.message?.let { it1 -> toast(it1) }
            }
            is BaseNavEvent.UnKnownException -> {
                it.throwable?.message?.let { it1 -> toast(it1) }
            }
        }
    }

    private val unblockObserver = Observer<BaseNavEvent<Void?>> {
        when (it) {
            is BaseNavEvent.StartLoading -> {
                dialog.show(mContext)
            }
            is BaseNavEvent.StopLoading -> {
                dialog.dialog.cancel()
            }
            is BaseNavEvent.Success -> {
                Log.e(TAG, "Success")
                blockContact?.let {
                    myApp.db.contactDao().deleteContact(it)
                }
                blockContact = null
                getData()
            }
            is BaseNavEvent.Error -> {
                Log.e(TAG, "Error")
                it.message?.let {
                    toast(it)
                }
            }
            is BaseNavEvent.ShowMessage -> {
                it.message?.let {
                    toast(it)
                }
            }
            is BaseNavEvent.JsonParseException -> {
                it.throwable?.message?.let { it1 -> toast(it1) }
            }
            is BaseNavEvent.NetWorkException -> {
                it.throwable?.message?.let { it1 -> toast(it1) }
            }
            is BaseNavEvent.UnKnownException -> {
                it.throwable?.message?.let { it1 -> toast(it1) }
            }
        }
    }
}