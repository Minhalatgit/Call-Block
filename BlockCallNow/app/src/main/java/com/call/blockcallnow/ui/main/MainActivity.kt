package com.call.blockcallnow.ui.main

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Telephony
import android.text.InputType
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.call.blockcallnow.R
import com.call.blockcallnow.data.event.BaseNavEvent
import com.call.blockcallnow.data.preference.PrefManager
import com.call.blockcallnow.data.room.BlockContact
import com.call.blockcallnow.ui.base.BaseActivity
import com.call.blockcallnow.ui.login.LoginActivity
import com.call.blockcallnow.ui.menu.home.BlockContactDetail
import com.call.blockcallnow.ui.menu.home.BlockedListFragment
import com.call.blockcallnow.util.LogUtil
import com.call.blockcallnow.util.Utils
import com.call.blockcallnow.util.dialog.CallBlockDialog
import com.call.blockcallnow.util.toast
import com.google.android.material.navigation.NavigationView
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.layout_block_options.*
import java.util.regex.Pattern

class MainActivity : BaseActivity() {

    private lateinit var blockStatus: String
    lateinit var blockViewModel: BlockViewModel
    private var selectedPhoneNo: String = ""
    private var selectedOption: String = ""
    private var selectedContact: Uri? = null

    private val RC_CONTACT: Int = 1001
    private val RC_ROLE: Int = 1002
    private val RC_DEFAULT_SMS: Int = 1003
    private val RC_CONTACT_CHANGE: Int = 1004

    private lateinit var appBarConfiguration: AppBarConfiguration

    private val TAG: String? = MainActivity::class.simpleName

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        title = ""

        blockViewModel = ViewModelProvider(this).get(BlockViewModel::class.java)
        blockViewModel.blockNavEvent.observe(this, blockNoObserver)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        val ivProfile = navView.getHeaderView(0).findViewById<ImageView>(R.id.iv_profile)
        val tvName = navView.getHeaderView(0).findViewById<TextView>(R.id.tv_name)
        tvName.text = userDetail?.name?.capitalize()

        navView.setupWithNavController(navController)

        bottom_app_bar.navigationIcon =
            scaledDrawableResources(R.drawable.ic_home, R.dimen._25sdp, R.dimen._25sdp)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_blocked_list,
                R.id.nav_messages,
                R.id.nav_logs,
                R.id.nav_settings,
                R.id.nav_change_password,
                R.id.nav_logout
            ), drawerLayout
        )
        iv_menu?.setOnClickListener {
            drawerLayout.open()
        }

        fab?.setOnClickListener {
            val dialog = CallBlockDialog()
            dialog.optionNavEvent.observe(this, Observer {
                if (it == 1) {
                    requestContactDetail()
                } else {
                    showInputDialog()
                }
            })
            dialog.show(supportFragmentManager, "hujai")
        }

        bottom_app_bar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_setting -> {
                    navController.navigate(R.id.nav_settings)
                }
            }
            return@setOnMenuItemClickListener true
        }

        bottom_app_bar?.setNavigationOnClickListener {
            //home icon clicked
            navController.navigate(R.id.nav_blocked_list)
        }

        requestRole() //for Android 10 and higher
        requestAppPermissions()
        callPermission()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestRole() {
        getSystemService(Context.ROLE_SERVICE)?.let {
            val roleManager = it as RoleManager
            if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                startActivityForResult(intent, RC_ROLE)
            } else {
                LogUtil.e(TAG, "you are already ROLE_CALL_SCREENING")
            }
        }
    }

    private fun requestAppPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            callPermissionForP()
        } else
            callPermission()
    }

    private fun callPermission() = runWithPermissions(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_PHONE_STATE
    ) {}

    private fun callPermissionForP() = runWithPermissions(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.ANSWER_PHONE_CALLS
    ) {}

    private fun showInputDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
        builder.setTitle("Enter Number")

        val input = EditText(mContext)
        input.hint = "+13712312382"
        input.inputType = InputType.TYPE_CLASS_PHONE
        builder.setView(input)

        builder.setPositiveButton("OK", null)

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        val dialog = builder.create()

        dialog.setOnShowListener {
            (it as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

                val phoneNo = input.text.toString()
                if (phoneNo.isBlank()) {
                    toast("Phone no can't be empty")
//                } else if (phoneNo.startsWith("+0") || !phoneNo.startsWith("+") &&
                } else if (!Pattern.compile("^\\+[1-9]").matcher(phoneNo).find()) {
                    toast("Please enter valid phone number with country code")
                } else {
                    selectedPhoneNo = phoneNo
                    showBlockOption("input")
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    private fun scaledDrawableResources(
        @DrawableRes id: Int,
        @DimenRes width: Int,
        @DimenRes height: Int
    ): Drawable {
        val w = resources.getDimension(width).toInt()
        val h = resources.getDimension(height).toInt()
        return scaledDrawable(id, w, h)
    }

    private fun scaledDrawable(@DrawableRes id: Int, width: Int, height: Int): Drawable {
        val bmp = BitmapFactory.decodeResource(resources, id)
        val bmpScaled = Bitmap.createScaledBitmap(bmp, width, height, false)
        return BitmapDrawable(resources, bmpScaled)
    }

    private fun requestContactDetail() {
        val pickContact = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        pickContact.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        startActivityForResult(pickContact, RC_CONTACT)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == RC_CONTACT) {
            selectedContact = data?.data
            showBlockOption("contact")
        } else if (requestCode == RC_ROLE) {
            if (resultCode == Activity.RESULT_OK) {
                LogUtil.e(TAG, "you are ROLE_CALL_SCREENING")
            } else {
                LogUtil.e(TAG, "Please allow role")
                requestRole()
            }
        } else if (requestCode == RC_DEFAULT_SMS) {
            if (resultCode == Activity.RESULT_OK) {
                LogUtil.e(TAG, "this is default app")
            } else {
                LogUtil.e(TAG, "Please allow sms default app")
//                openSMSAppChooser()
            }
        } else if (requestCode == RC_CONTACT_CHANGE) {
            if (resultCode == Activity.RESULT_OK) {
                updateContactList()
            } else
                LogUtil.e(TAG, "Contact does not change")
        }
    }

    private fun showBlockOption(option: String) {
        selectedOption = option
//        startActivity(Intent(mContext,BlockContactDetail::class.java).putExtra("name",))
        var name = "Unknown"
        var number = ""
        var blockedNumber = ""
        var uri: String? = null
        if (selectedOption == "contact") {
            selectedContact?.let {
                Utils.getCursorFromUri(mContext, it)?.let { cursor ->
                    if (cursor.moveToFirst()) {
                        val contact = Utils.cursorToBlockContact(mContext, cursor, "")
//                        blockViewModel.blockNo(token, contact.number, contact.name, status)
                        name = contact.name ?: "Unknown"
                        number = contact.number ?: ""
                        blockedNumber = contact.blockNumber ?: ""
                        uri = contact.uri
                    }
                }
            }
        } else {
//            blockViewModel.blockNo(token, selectedPhoneNo, null, status)
            number = selectedPhoneNo
        }
        startActivityForResult(
            Intent(mContext, BlockContactDetail::class.java)
                .putExtra("name", name)
                .putExtra("phone", number)
                .putExtra("block_number", blockedNumber)
                .putExtra("uri", uri)
                .putExtra("isEdit", false), RC_CONTACT_CHANGE
        )
//        blockOptionSheet.show(supportFragmentManager, "blockOptions")
    }

    private fun updateContactList() {
        val navHostFragment: NavHostFragment? =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val currentFragment = navHostFragment?.childFragmentManager?.fragments?.get(0)
        currentFragment?.let {
            if (it is BlockedListFragment) {
                it.getData()
            }
        }
    }

    private fun blockContact(data: Uri?, status: String) {

        data?.let {
            val cursor: Cursor? = contentResolver?.query(it, null, null, null, null)
            cursor?.let {
                if (cursor.moveToFirst()) {

                    val doa = myApp.db.contactDao()
                    val newBlockContact = Utils.cursorToBlockContact(mContext, cursor, status)
                    val oldContact = doa.getBlockContactFromNumber(newBlockContact.blockNumber)
                    oldContact?.let { blockContact ->
                        blockContact.name = newBlockContact.name
                        blockContact.uri = newBlockContact.uri
                        blockContact.blockStatus = newBlockContact.blockStatus
                        doa.updateBlockContact(oldContact)
                    } ?: doa.insertAll(listOf(newBlockContact))
                }
                cursor.close()
            }
        }
    }

    private fun blockContact(number: String, status: String) {

        val doa = myApp.db.contactDao()

        val blockNumber = Utils.getBlockNumber(mContext, number)
        val oldContact = doa.getBlockContactFromNumber(blockNumber)
        val newBlockContact = BlockContact(0, null, number, blockNumber, status, null, 0, 0)
        oldContact?.let {

            doa.updateBlockContact(oldContact)
        } ?: doa.insertAll(listOf(newBlockContact))
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private val blockNoObserver = Observer<BaseNavEvent<Void?>> {
        when (it) {
            is BaseNavEvent.StartLoading -> {
                dialog.show(mContext)
            }
            is BaseNavEvent.StopLoading -> {
                dialog.dialog.cancel()
            }
            is BaseNavEvent.Success -> {
                if (selectedOption == "contact") {
                    blockContact(selectedContact, blockStatus)
                    updateContactList()
                } else {
                    blockContact(selectedPhoneNo, blockStatus)
                    updateContactList()
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

    fun logout(item: MenuItem) {
        PrefManager.getSharedPreferences(mContext).edit().clear().apply()
        myApp.db.contactDao().clearTable()
        myApp.db.contactDao().deleteAllLogs()
        startActivity(
            Intent(mContext, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
        finish()
    }
}