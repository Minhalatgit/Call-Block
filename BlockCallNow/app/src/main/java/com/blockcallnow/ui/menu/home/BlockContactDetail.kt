package com.blockcallnow.ui.menu.home

import android.Manifest
import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.text.InputType
import android.util.Log
import android.view.View.*
import android.widget.CompoundButton
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blockcallnow.R
import com.blockcallnow.data.event.BaseNavEvent
import com.blockcallnow.data.model.BlockNoDetail
import com.blockcallnow.data.model.BlockNoDetails
import com.blockcallnow.data.model.UploadAudioResponse
import com.blockcallnow.data.room.BlockContact
import com.blockcallnow.databinding.ActivityBlockContactDetailBinding
import com.blockcallnow.ui.base.BaseActivity
import com.blockcallnow.ui.main.BlockViewModel
import com.blockcallnow.util.FileUtils
import com.blockcallnow.util.LogUtil
import com.blockcallnow.util.Utils
import com.blockcallnow.util.Utils.Companion.FULL_BLOCK
import com.blockcallnow.util.Utils.Companion.PARTIAL_BLOCK
import com.blockcallnow.util.Utils.Companion.PLAN_PRO
import com.blockcallnow.util.Utils.Companion.PLAN_STAND
import com.blockcallnow.util.Utils.Companion.PLAN_TRIAL
import com.blockcallnow.util.toast
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import kotlinx.android.synthetic.main.activity_block_contact_detail.view.*
import omrecorder.OmRecorder
import omrecorder.PullTransport
import omrecorder.Recorder
import java.io.File
import java.util.regex.Pattern

class BlockContactDetail : BaseActivity(),
    MediaPlayer.OnCompletionListener,
    CompoundButton.OnCheckedChangeListener {

    private val TAG: String = "BlockContactDetail"
    lateinit var blockViewModel: BlockViewModel

    private var destFile: File? = null
    private lateinit var player: MediaPlayer
    private var recorder: Recorder? = null
    private var isRecording: Boolean = false

    lateinit var phoneNo: String
    lateinit var name: String
    lateinit var blockNumber: String
    var blockStatus = ""
    var photoUri: String? = null
    var isEdit = false

    //    val audioRecorder = NaraeAudioRecorder()
    var rbSelectionLang: RadioButton? = null

    private val binding: ActivityBlockContactDetailBinding by binding(R.layout.activity_block_contact_detail)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        blockViewModel = ViewModelProvider(this).get(BlockViewModel::class.java)
        blockViewModel.uploadNavEvent.observe(this, uploadEvent)
        blockViewModel.deleteAudioNavEvent.observe(this, deleteEvent)
        blockViewModel.detailNavEvent.observe(this, detailEvent)
        blockViewModel.blockNavEvent.observe(this, blockNoObserver)

        name = intent?.getStringExtra("name") ?: ""
        phoneNo = intent?.getStringExtra("phone") ?: ""
        blockNumber = intent?.getStringExtra("block_number") ?: ""
        photoUri = intent?.getStringExtra("uri") ?: ""
        isEdit = intent?.getBooleanExtra("isEdit", false) ?: false

        if (isEdit) {
            binding.toolbar.tv_title?.text = "Edit Contact"
            binding.btnSubmit.text = "Update Contact"
            blockViewModel.getBlockDetail(token, phoneNo)
        } else {
            blockNumber = Utils.getBlockNumber(mContext, phoneNo)
            binding.toolbar.tv_title?.text = "Add"
            binding.btnSubmit.text = "Block Contact"
            binding.tvName.text = name
            binding.etNumber.setText(phoneNo)
            if (phoneNo.startsWith("+0") || !phoneNo.startsWith("+")) {
                binding.etNumber.isEnabled = true
                binding.etMessage.inputType = InputType.TYPE_CLASS_PHONE
            } else {
                binding.etNumber.isEnabled = false
                binding.etMessage.inputType = 0
            }
            setPref()

            binding.rgStatus.check(R.id.rb_partial_block)
            binding.rbPartialBlock.isChecked = true
            binding.rgMessage.check(R.id.rb_generic_msg)
            binding.rgVoice.check(R.id.rb_male_voice)
            binding.rbEng.isChecked = true
            rbSelectionLang = binding.rbEng
        }
        setClickListener()
    }

    private fun setClickListener() {
        binding.rbEng.setOnCheckedChangeListener(this)
        binding.rbRus.setOnCheckedChangeListener(this)
        binding.rbSpanish.setOnCheckedChangeListener(this)
        binding.rbFrench.setOnCheckedChangeListener(this)
        binding.rbArabic.setOnCheckedChangeListener(this)
        binding.rbChinese.setOnCheckedChangeListener(this)

        binding.btnSubmit.setOnClickListener {
//            if (isEdit) {
//
//            } else {
            addBlockContact()
//            }

        }
        binding.ivPlayPause.setOnClickListener {
            if (::player.isInitialized && player.isPlaying) {
                stopPlaying()
            } else {
                startPlaying()
            }
        }
        binding.toolbar.iv_back?.setOnClickListener {
            onBackPressed()
        }
        binding.fabMic.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                recordPermission()
            }
        }
        binding.ivDelete.setOnClickListener {
//            blockViewModel.deleteAudio(token, phoneNo)
            destFile = null
            showRecordingView()
        }
        binding.rgStatus.setOnCheckedChangeListener { group, checkId ->
            LogUtil.e(TAG, "Block Status Radio group changes")
            when (checkId) {
                R.id.rb_partial_block -> {
                    blockStatus = PARTIAL_BLOCK
//                    binding.rbCustomMessage.isEnabled = false
//                    binding.rbGenericMsg.isChecked = true
                    binding.etMessage.setText("")
                    binding.cvAudio.visibility = GONE
                    binding.cvVoice.visibility = VISIBLE
                    binding.cvLangs.visibility = VISIBLE
                }
                R.id.rb_full_block -> {
                    blockStatus = FULL_BLOCK
//                    binding.rbCustomMessage.isEnabled = true
                    binding.cvVoice.visibility = GONE
                    binding.cvLangs.visibility = GONE
                    binding.cvAudio.visibility = VISIBLE
                }
            }
        }
        binding.rgMessage.setOnCheckedChangeListener { group, checkedId ->
            LogUtil.e(TAG, "Message Radio group changes")
            when (checkedId) {
                R.id.rb_custom_message -> {
                    binding.etMessage.isEnabled = true
                    binding.etMessage.inputType = InputType.TYPE_CLASS_TEXT
                }
                R.id.rb_generic_msg -> {
                    binding.etMessage.isEnabled = false
                    binding.etMessage.inputType = 0
                    binding.etMessage.setText("")
                }
                -1 -> {
                    binding.etMessage.isEnabled = false
                    binding.etMessage.inputType = 0
                    binding.etMessage.setText("")

                    binding.cvVoice.visibility = VISIBLE
                    binding.cvLangs.visibility = VISIBLE
                }

            }
        }
    }

    private fun addBlockContact() {
        LogUtil.e(TAG, "binding.etMessage.isEnabled " + binding.etMessage.isEnabled)

        phoneNo = binding.etNumber.text.toString()
//        if (phoneNo.startsWith("+0")|| !phoneNo.startsWith("+")) {
        if (!Pattern.compile("^\\+[1-9]").matcher(phoneNo).find()) {
            toast("Invalid Phone number. Please enter phone number with country code")
            return
        }
        val blockStatusSelection = binding.rgStatus.checkedRadioButtonId
        when (blockStatusSelection) {
            -1 -> {
                toast("Please select block status")
                return
            }
            R.id.rb_partial_block -> {
                blockStatus = PARTIAL_BLOCK
                if (!binding.rbMaleVoice.isChecked && !binding.rbFemaleVoice.isChecked) {
                    toast("Please select voice")
                    return
                }
                if (!binding.rbGenericMsg.isChecked && !binding.rbCustomMessage.isChecked) {
                    toast("Please select message")
                    return
                }
                if (binding.rbCustomMessage.isChecked && binding.etMessage.text.isBlank()) {
                    toast("Please write custom message up to 100 characters")
                    return
                }
                if (rbSelectionLang == null) {
                    toast("Please select a language")
                    return
                }

                val voice = if (binding.rbMaleVoice.isChecked) "M" else "F"
                val msgType = if (binding.rbGenericMsg.isChecked) 1 else 0
                val msg: String? = if (msgType == 1) null else binding.etMessage.text.toString()
                val lang = rbSelectionLang?.tag.toString()
                blockViewModel.blockNo(
                    token,
                    phoneNo,
                    blockNumber,
                    name,
                    blockStatus,
                    voice, msgType, lang, msg
                )
            }
            R.id.rb_full_block -> {
                blockStatus = FULL_BLOCK
                if (!binding.rbGenericMsg.isChecked && !binding.rbCustomMessage.isChecked) {
                    toast("Please select message")
                    return
                }
                if (binding.rbCustomMessage.isChecked && binding.etMessage.text.isBlank()) {
                    toast("Please write custom message up to 100 characters")
                    return
                }
                if (binding.llRecord.visibility == VISIBLE && destFile == null) {
                    toast("Please record custom audio message")
                    return
                }
                if (isRecording) {
                    toast("Please stop recording")
                    return
                }
                val voice = if (binding.rbMaleVoice.isChecked) "M" else "F"
                val msgType = if (binding.rbGenericMsg.isChecked) 1 else 0
                val msg: String? = if (msgType == 1) null else binding.etMessage.text.toString()
                val lang = rbSelectionLang?.tag.toString()
                blockViewModel.blockNo(
                    token,
                    phoneNo,
                    blockNumber,
                    name,
                    blockStatus,
                    voice, msgType, lang, msg
                )
            }
        }
    }

    private fun validateDataNewContact(): Boolean {
        val blockStatusSelection = binding.rgStatus.checkedRadioButtonId
        if (blockStatusSelection == -1) {
            toast("Please select block status")
            return false
        } else if (blockStatusSelection == R.id.rb_partial_block) {
            blockStatus = PARTIAL_BLOCK
        } else if (blockStatusSelection == R.id.rb_full_block) {
            blockStatus = FULL_BLOCK
        }

        if (blockStatus == "partial") {

        } else {

        }
        return true
    }

    private fun getSelectedLang(): String {
        return ""
    }

    private fun initPlayer(path: String?, uri: Uri?) {
        player = MediaPlayer()
        player.setOnCompletionListener(this)
//        player.setOnBufferingUpdateListener { mp, percent ->
//            if(percent<100){
//
//                binding.pb.visibility= VISIBLE
//                binding.ivPlayPause.visibility=GONE
//            }
//            else{
//                binding.pb.visibility= VISIBLE
//                binding.ivPlayPause.visibility= GONE
//            }
//
//        }
        if (uri != null)
            player.setDataSource(mContext, uri)
        else if (path != null)
            player.setDataSource(path)
        player.setOnPreparedListener {
            binding.pb.visibility = GONE
            binding.ivPlayPause.visibility = VISIBLE
            binding.chronometerPlayer.base = SystemClock.elapsedRealtime()
            it.start()
            binding.chronometerPlayer.start()
            binding.ivPlayPause.setImageResource(R.drawable.ic_pause)
        }
    }

    private fun startPlaying() {
        try {
            player.prepareAsync()
            binding.pb.visibility = VISIBLE
            binding.ivPlayPause.visibility = INVISIBLE
        } catch (e: Exception) {
            Log.e(TAG, "startPlaying: ${e.message}")
        }
    }

    private fun recordPermission() =
        runWithPermissions(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) {
            startRecording()
        }

    private fun startRecording() {
//        destFile = File(
//            Environment.getExternalStorageDirectory(),
//            "/BlockNow/" + System.currentTimeMillis() + ".wav"
//        )
        destFile = FileUtils.createWAVFile(mContext, System.currentTimeMillis().toString())
        recorder = OmRecorder.wav(
            PullTransport.Default(Utils.getMic()), destFile
        )
        isRecording = true
        recorder?.startRecording()
        binding.chronometer.base = SystemClock.elapsedRealtime()
        binding.chronometer.start()
        binding.fabMic.setImageResource(R.drawable.ic_mic)
        binding.fabMic.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#f52626"))
    }

    private fun stopRecording() {
        try {
            recorder?.stopRecording()
        } catch (e: Exception) {
            e.printStackTrace()
        }

//        audioRecorder.stopRecording()
        binding.chronometer.stop()
        isRecording = false

//        blockViewModel.uploadAudio(token, phoneNo, destFile)
        binding.fabMic.setImageResource(R.drawable.ic_mic_none)

        binding.fabMic.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.colorAccent))
        showPlayerView(null, Uri.fromFile(destFile))
    }

    override fun onCompletion(mp: MediaPlayer?) {
        stopPlaying()
    }

    private fun stopPlaying() {
        player.stop()
        binding.chronometerPlayer.stop()
        binding.ivPlayPause.setImageResource(R.drawable.ic_play)
    }

    override fun onStop() {
        if (::player.isInitialized && player.isPlaying)
            player.stop()
//        recorder?.stopRecording()
        super.onStop()
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

                if (blockStatus == "full" && destFile != null) {

                    blockViewModel.uploadAudio(token, phoneNo, destFile!!)

                } else {
                    blockContact(phoneNo, blockNumber, blockStatus, photoUri)
                    setResult(Activity.RESULT_OK)
                    finish()
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

    private fun blockContact(number: String, blockNumber: String, status: String, uri: String?) {
        val doa = myApp.db.contactDao()
//        val blockNumber = Utils.getBlockNumber(mContext, number)
        val oldContact = doa.getBlockContactFromNumber(blockNumber)
        val newBlockContact = BlockContact(0, name, number, blockNumber, status, uri, 0, 0)
        oldContact?.let {
            it.name = newBlockContact.name
            it.uri = newBlockContact.uri
            it.blockStatus = status
            doa.updateBlockContact(it)
        } ?: doa.insertAll(listOf(newBlockContact))
    }

    private val uploadEvent = Observer<BaseNavEvent<UploadAudioResponse?>> {
        when (it) {
            is BaseNavEvent.StartLoading -> {
                dialog.show(mContext)
            }
            is BaseNavEvent.StopLoading -> {
                dialog.dialog.cancel()
            }
            is BaseNavEvent.Success -> {
//                binding.llRecord.visibility = GONE
//                binding.rl.visibility = VISIBLE
//                binding.fabMic.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFF"))
//                binding.fabMic.setImageResource(R.drawable.ic_mic_none)
//                it.data?.data?.audio?.fileUrl?.let { path ->
//                    showPlayerView(path)
//                }
                blockContact(phoneNo, blockNumber, blockStatus, photoUri)
                setResult(Activity.RESULT_OK)
                finish()
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

    private val deleteEvent = Observer<BaseNavEvent<Void?>> {
        when (it) {
            is BaseNavEvent.StartLoading -> {
                dialog.show(mContext)
            }
            is BaseNavEvent.StopLoading -> {
                dialog.dialog.cancel()
            }
            is BaseNavEvent.Success -> {
                showRecordingView()
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

    private val detailEvent = Observer<BaseNavEvent<BlockNoDetail?>> {
        when (it) {
            is BaseNavEvent.StartLoading -> {
                dialog.show(mContext)
            }
            is BaseNavEvent.StopLoading -> {
                dialog.dialog.cancel()
            }
            is BaseNavEvent.Success -> {

                it.data?.data?.let { detail ->

                    detail.blockNoDetails?.let {
                        if (it.status == "full") {
                            binding.cvVoice.visibility = GONE
                            binding.cvLangs.visibility = GONE
                            detail.audio?.fileUrl?.let { fileUrl ->
                                Log.d(TAG, "Audio file is $fileUrl ")
                                showPlayerView(fileUrl, null)
                            } ?: run {
                                showRecordingView()
                            }
                        } else {
                            binding.cvVoice.visibility = VISIBLE
                            binding.cvLangs.visibility = VISIBLE
                        }
                        setPref()
                        setContactInfo(it)
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

    private fun setContactInfo(block: BlockNoDetails) {
        binding.tvName.text = block.name
        binding.etNumber.setText(block.phoneNo)
//        if (phoneNo.startsWith("+0")|| !phoneNo.startsWith("+")){
        if (!Pattern.compile("^\\+[1-9]").matcher(phoneNo).find()) {
            binding.etNumber.isEnabled = true
            binding.etMessage.inputType = InputType.TYPE_CLASS_PHONE
        } else {
            binding.etNumber.isEnabled = false
            binding.etMessage.inputType = 0
        }
        if (block.status == "partial")
            binding.rbPartialBlock.isChecked = true
        else if (block.status == "full")
            binding.rbFullBlock.isChecked = true
        if (block.is_generic_text == 0)
            binding.rbGenericMsg.isChecked = false
        else if (block.is_generic_text == 1)
            binding.rbGenericMsg.isChecked = true
        block.message?.let {
            binding.rbCustomMessage.isChecked = true
            binding.etMessage.setText(it)
        } ?: run {
            binding.rbCustomMessage.isChecked = false
        }
        if (block.set_voice_gender == "M") {
            binding.rbMaleVoice.isChecked = true
        } else if (block.set_voice_gender == "F")
            binding.rbFemaleVoice.isChecked = true

        if (block.set_voice_lang == "en") {
            binding.rbEng.isChecked = true
            rbSelectionLang = binding.rbEng
        } else if (block.set_voice_lang == "ru") {
            binding.rbRus.isChecked = true
            rbSelectionLang = binding.rbRus
        } else if (block.set_voice_lang == "es") {
            binding.rbSpanish.isChecked = true
            rbSelectionLang = binding.rbSpanish
        } else if (block.set_voice_lang == "chi") {
            binding.rbChinese.isChecked = true
            rbSelectionLang = binding.rbChinese
        } else if (block.set_voice_lang == "ar") {
            binding.rbArabic.isChecked = true
            rbSelectionLang = binding.rbArabic
        } else if (block.set_voice_lang == "fr") {
            binding.rbFrench.isChecked = true
            rbSelectionLang = binding.rbFrench
        }
    }

    private fun setPref() {
        if (userDetail?.paywhirl_plan_id == PLAN_TRIAL || userDetail?.paywhirl_plan_id == PLAN_STAND) {

            binding.tv1.visibility = VISIBLE
            binding.tv2.visibility = VISIBLE
            binding.tv3.visibility = VISIBLE
            binding.tv4.visibility = VISIBLE
            binding.rbFullBlock.isEnabled = false

            binding.rbCustomMessage.isEnabled = false
            binding.etMessage.isEnabled = false
            binding.rbFemaleVoice.isEnabled = false
            binding.rbMaleVoice.isEnabled = false

            binding.rbEng.isEnabled = false
            binding.rbSpanish.isEnabled = false
            binding.rbRus.isEnabled = false
            binding.rbArabic.isEnabled = false
            binding.rbFrench.isEnabled = false
            binding.rbChinese.isEnabled = false

        } else if (userDetail?.paywhirl_plan_id == PLAN_PRO) {
            binding.tv1.visibility = GONE
            binding.tv2.visibility = GONE
            binding.tv3.visibility = GONE
            binding.tv4.visibility = GONE
            binding.rbFullBlock.isEnabled = true
            binding.rbCustomMessage.isEnabled = true
            binding.etMessage.isEnabled = true
            binding.rbFemaleVoice.isEnabled = true
            binding.rbMaleVoice.isEnabled = true
            binding.rbEng.isEnabled = true
            binding.rbSpanish.isEnabled = true
            binding.rbRus.isEnabled = true
            binding.rbArabic.isEnabled = true
            binding.rbFrench.isEnabled = true
            binding.rbChinese.isEnabled = true
        }
    }

    private fun showRecordingView() {
        binding.rl.visibility = GONE
        binding.llRecord.visibility = VISIBLE
        binding.fabMic.setImageResource(R.drawable.ic_mic_none)
        binding.chronometer.base = SystemClock.elapsedRealtime()
        binding.fabMic.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.colorAccent))
    }

    private fun showPlayerView(fileUrl: String?, uri: Uri?) {
        binding.rl.visibility = VISIBLE
        binding.llRecord.visibility = GONE
        binding.fabMic.setImageResource(R.drawable.ic_mic_none)
        binding.fabMic.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.colorAccent))
        initPlayer(fileUrl, uri)
    }

//    private fun blockContact(data: Uri?, status: String) {
//
//        data?.let {
//
//
//            val cursor: Cursor? = contentResolver?.query(it, null, null, null, null)
//            cursor?.let {
//                if (cursor.moveToFirst()) {
//
//                    val doa = myApp.db.contactDao()
//                    val newBlockContact = Utils.cursorToBlockContact(mContext, cursor, status)
//                    val oldContact = doa.getBlockContactFromNumber(newBlockContact.blockNumber)
//                    oldContact?.let { blockContact ->
//                        blockContact.name = newBlockContact.name
//                        blockContact.uri = newBlockContact.uri
//                        blockContact.blockStatus = newBlockContact.blockStatus
//                        doa.updateBlockContact(oldContact)
//                    } ?: doa.insertAll(listOf(newBlockContact))
//
//
//                }
//                cursor.close()
//            }
//        }
//    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        rbSelectionLang?.isChecked = false
        rbSelectionLang = (buttonView as RadioButton)
    }
}