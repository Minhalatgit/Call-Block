package com.blockcallnow.ui.main

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
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.text.InputType
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.blockcallnow.R
import com.blockcallnow.data.event.BaseNavEvent
import com.blockcallnow.data.preference.PrefManager
import com.blockcallnow.data.room.BlockContact
import com.blockcallnow.ui.base.BaseActivity
import com.blockcallnow.ui.login.LoginActivity
import com.blockcallnow.ui.menu.home.BlockContactDetail
import com.blockcallnow.ui.menu.home.BlockedListFragment
import com.blockcallnow.util.LogUtil
import com.blockcallnow.util.Utils
import com.blockcallnow.util.Utils.Companion.FULL_BLOCK
import com.blockcallnow.util.Utils.Companion.PARTIAL_BLOCK
import com.blockcallnow.util.dialog.CallBlockDialog
import com.blockcallnow.util.toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.layout_block_options.*
import java.util.*
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
    private val REQUEST_CODE_SET_DEFAULT_DIALER: Int = 1003

    private val blockOptionSheet = BlockOptionSheet()
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val TAG: String? = MainActivity::class.simpleName

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        LogUtil.e(
            TAG,
            "Pattern.matches(\"[a-z]\", address.toLowerCase()) " + Pattern.compile("[a-z]")
                .matcher("Al Khidmat".toLowerCase(Locale.ROOT))
                .find()
        )

        blockViewModel = ViewModelProvider(this).get(BlockViewModel::class.java)
        blockViewModel.blockNavEvent.observe(this, blockNoObserver)
//        val toolbar: Toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)
//        supportActionBar?.setHomeAsUpIndicator(null)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_blocked_list,
                R.id.nav_messages,
                R.id.nav_logs,
                R.id.nav_settings,
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

        val ivProfile = navView.getHeaderView(0).findViewById<ImageView>(R.id.iv_profile)
        val tvName = navView.getHeaderView(0).findViewById<TextView>(R.id.tv_name)
        tvName.text = userDetail?.name

        navView.setupWithNavController(navController)
//        navView.setNavigationItemSelectedListener { item ->
//            if (item.itemId == R.id.nav_logout) {
//                PrefManager.getSharedPreferences(mContext).edit().clear().apply()
//                startActivity(
//                    Intent(mContext, LoginActivity::class.java).addFlags(
//                        Intent.FLAG_ACTIVITY_CLEAR_TASK
//                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                )
//                finish()
//
//            } else {
//                navController.navigate(item.itemId)
//            }
//            true
//        }

        bottom_app_bar.navigationIcon =
            scaledDrawableResources(R.drawable.ic_home, R.dimen._25sdp, R.dimen._25sdp)

        val blockContactDao = myApp.db.contactDao()
        LogUtil.e(TAG, blockContactDao.getAllBlockedContacts().toString())

        requestRole()
        requestAppPermissions()
        callPermission()

        blockOptionSheet.navBlockOptions.observe(this,
            Observer {
                sendBlockContact(it)
            })
        LogUtil.e(TAG, "isSimAvailable " + isSimAvailable())

        val callSettingsIntent = Intent(TelecomManager.ACTION_SHOW_CALL_SETTINGS)
//        callSettingsIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//        startActivity(callSettingsIntent)
        val info = packageManager.resolveActivity(callSettingsIntent, 0)
        LogUtil.e(TAG, "info " + Gson().toJson(info))
    }

    private fun isSimAvailable(): Boolean {
        var isAvailable = false
        val telMgr =
            mContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val simState = telMgr.simState
        when (simState) {
            TelephonyManager.SIM_STATE_ABSENT -> {
            }
            TelephonyManager.SIM_STATE_NETWORK_LOCKED -> {
            }
            TelephonyManager.SIM_STATE_PIN_REQUIRED -> {
            }
            TelephonyManager.SIM_STATE_PUK_REQUIRED -> {
            }
            TelephonyManager.SIM_STATE_READY -> isAvailable = true
            TelephonyManager.SIM_STATE_UNKNOWN -> {
            }
        }
        return isAvailable
    }

    fun isSimSupport(): Boolean {
        val tm: TelephonyManager =
            getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager //gets the current TelephonyManager
        return !(tm.simState == TelephonyManager.SIM_STATE_ABSENT)
    }

    private fun requestAppPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            callPermissionForP()
        } else
            callPermission()
    }

    private fun sendBlockContact(status: String) {
        blockStatus = status
        if (selectedOption == "contact") {
            selectedContact?.let {
                Utils.getCursorFromUri(mContext, it)?.let { cursor ->
                    if (cursor.moveToFirst()) {
                        val contact = Utils.cursorToBlockContact(mContext, cursor, status)
//                        blockViewModel.blockNo(token, contact.number, contact.name, status)
                    }
                }
            }
        } else {
//            blockViewModel.blockNo(token, selectedPhoneNo, null, status)
        }
    }

    private fun showInputDialog() {

        val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
        builder.setTitle("Enter Number")

        val input = EditText(mContext)
        input.hint = "+13712312382"
        input.inputType = InputType.TYPE_CLASS_PHONE
        builder.setView(input)

        builder.setPositiveButton("OK", null)

        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

        val dialog = builder.create()

        dialog.setOnShowListener {
            (it as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

                val phoneNo = input.text.toString()
                if (phoneNo.isBlank()) {
                    toast("Phone no can't be empty")
//                } else if (phoneNo.startsWith("+0") || !phoneNo.startsWith("+") &&
                } else if (!Pattern.compile("^\\+[1-9]").matcher(phoneNo).find()
                ) {
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

    fun hideFAB() {
        bottom_app_bar?.fabCradleMargin = 0F
        bottom_app_bar?.fabCradleRoundedCornerRadius = 0F
        fab?.visibility = GONE

//    app:fabCradleMargin="0dp"
//    app:fabCradleRoundedCornerRadius="@dimen/_70sdp"
    }

    fun showFAB() {
        bottom_app_bar?.fabCradleMargin = 0F
        bottom_app_bar?.fabCradleRoundedCornerRadius = resources.getDimension(R.dimen._70sdp)
        fab?.visibility = VISIBLE
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
                openSMSAppChooser(this)
            } else {
                LogUtil.e(TAG, "Please allow role")
                requestRole()
            }
        } else if (requestCode == RC_DEFAULT_SMS) {
            if (resultCode == Activity.RESULT_OK) {

                LogUtil.e(TAG, "this is default app")

            } else {
                LogUtil.e(TAG, "Please allow sms default app")
//                openSMSappChooser(this)
            }

        } else if (requestCode == RC_CONTACT_CHANGE) {
            if (resultCode == Activity.RESULT_OK) {
                updateContactList()
            } else
                LogUtil.e(TAG, "Contact Does not change")
        }
    }

    private fun openSMSAppChooser(context: Context?) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {

            val roleManager = getSystemService(RoleManager::class.java)
            if (!roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                val roleRequestIntent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                startActivityForResult(roleRequestIntent, RC_DEFAULT_SMS)
            } else {
                LogUtil.e(TAG, "you are already default app")
            }

        } else {
            val setSmsAppIntent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
            setSmsAppIntent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
            startActivityForResult(setSmsAppIntent, RC_DEFAULT_SMS)
        }
//        PackageManager packageManager = context.getPackageManager();
//        ComponentName componentName = new ComponentName(context, DefaultSMSAppChooserActivity.class);
//        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
//
//        Intent selector = new Intent(Intent.ACTION_MAIN);
//        selector.addCategory(Intent.CATEGORY_APP_MESSAGING);
//        selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(selector);
//
//        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
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

    //    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.test, menu)
//        return true
//    }
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestRole() {
        getSystemService(Context.ROLE_SERVICE)?.let {
            val roleManager = it as RoleManager
            if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                startActivityForResult(intent, RC_ROLE)
            } else
                LogUtil.e(TAG, "you are already ROLE_CALL_SCREENING")
            openSMSAppChooser(this)
        }
//        val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
//        startActivityForResult(
//            intent.putExtra(
//                TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
//                packageName
//            ), REQUEST_CODE_SET_DEFAULT_DIALER
//        )
    }

    private fun callPermission() = runWithPermissions(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_SMS,
        Manifest.permission.READ_CONTACTS
    ) {
        //            Toast.makeText(
        //                this,
        //                "Call Phone permission added",
        //                Toast.LENGTH_SHORT
        //            ).show();
        // Do the stuff with permissions safely
    }

    private fun callPermissionForP() = runWithPermissions(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_SMS,
        Manifest.permission.ANSWER_PHONE_CALLS
    ) {

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    class BlockOptionSheet : BottomSheetDialogFragment() {

        private val blockOptions = MutableLiveData<String>()
        val navBlockOptions: LiveData<String> = blockOptions

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.layout_block_options, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            tv_partial_block?.setOnClickListener {
                blockOptions.value = PARTIAL_BLOCK
                dismiss()
            }
            tv_full_block.setOnClickListener {
                blockOptions.value = FULL_BLOCK
                dismiss()
            }
        }
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
        startActivity(
            Intent(mContext, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
        finish()
    }
}