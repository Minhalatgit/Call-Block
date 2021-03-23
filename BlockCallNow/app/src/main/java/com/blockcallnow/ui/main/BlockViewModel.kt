package com.blockcallnow.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.blockcallnow.data.event.BaseNavEvent
import com.blockcallnow.data.model.*
import com.blockcallnow.data.room.AppDatabase
import com.blockcallnow.data.room.BlockContact
import com.blockcallnow.ui.base.BaseViewModel
import com.blockcallnow.util.LogUtil
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Action
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.internal.functions.Functions
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class BlockViewModel : BaseViewModel() {
    private val loginEvent = MutableLiveData<BaseNavEvent<LoginResponse?>>()
    val loginNavEvent: LiveData<BaseNavEvent<LoginResponse?>> = loginEvent

    private val navEvent = MutableLiveData<BaseNavEvent<Void?>>()
    val blockNavEvent: LiveData<BaseNavEvent<Void?>> = navEvent

    private val unblockEvent = MutableLiveData<BaseNavEvent<Void?>>()
    val unblockNavEvent: LiveData<BaseNavEvent<Void?>> = unblockEvent

    private val uploadEvent = MutableLiveData<BaseNavEvent<UploadAudioResponse?>>()
    val uploadNavEvent: LiveData<BaseNavEvent<UploadAudioResponse?>> = uploadEvent

    private val deleteAudioEvent = MutableLiveData<BaseNavEvent<Void?>>()
    val deleteAudioNavEvent: LiveData<BaseNavEvent<Void?>> = deleteAudioEvent

    private val detailEvent = MutableLiveData<BaseNavEvent<BlockNoDetail?>>()
    val detailNavEvent: LiveData<BaseNavEvent<BlockNoDetail?>> = detailEvent

    private val blockNoListEvent = MutableLiveData<BaseNavEvent<BlockNoListResponse?>>()
    val blockNoListNavEvent: LiveData<BaseNavEvent<BlockNoListResponse?>> = blockNoListEvent

    private val blockNoDBListEvent = MutableLiveData<List<BlockContact>>()
    val blockNoDBListNavEvent: LiveData<List<BlockContact>> = blockNoDBListEvent

    fun getUserProfile(token: String) {
        makeRequest(api.getUser(token), loginEvent)
    }

    fun getBlockNoList(token: String, appDatabase: AppDatabase) {
//        val list = appDatabase.contactDao().getAllBlockedContacts()
//        if (list.isNullOrEmpty()) {

        makeRequest(api.getBlockNoList(token), blockNoListEvent)
//        } else {

//            blockNoListEvent.value = BaseNavEvent.StopLoading()
//            blockNoDBListEvent.value = list

//        }
    }

    fun getBlockDetail(token: String, phoneNo: String) {
        makeRequest(api.getBlockNoDetail(token, phoneNo), detailEvent)
    }

    fun blockNo(
        token: String,
        phone: String?,
        formattedNo: String,
        name: String?,
        status: String,
        voiceGender: String,
        isGenericText: Int,
        lang: String,
        message: String?
    ) {
        makeRequest(
            api.blockNo(
                token,
                phone,
                name,
                formattedNo,
                status.toLowerCase(Locale.getDefault()),
                voiceGender,
                isGenericText,
                lang,
                message
            ), navEvent
        )
    }

    fun unblock(token: String, phoneNo: String?) {
        makeRequest(
            api.blockNo(token, phoneNo, null, null, "unblock", "M", 0, "en", null),
            unblockEvent
        )
    }

    fun uploadAudio(token: String, phoneNo: String, blockNo: String, file: File) {
        makeRequest(
            api.uploadAudio(
                token, phoneNo.toRequestBody(), blockNo.toRequestBody(), file.name.toRequestBody(),

                MultipartBody.Part.createFormData(
                    "file",
                    file.name,
                    file.asRequestBody("*/*".toMediaTypeOrNull())
                )
            ), uploadEvent
        )
    }

    fun deleteAudio(token: String, phoneNo: String) {
        makeRequest(api.deleteAudio(token, phoneNo), deleteAudioEvent)
    }

    fun saveDataToDb(db: AppDatabase, blockList: List<BlockNoDetails>?) {
        val list = ArrayList<BlockContact>()
        mDisposable.add(

            Observable.fromIterable(blockList)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .doOnSubscribe {
                    blockNoListEvent.value = BaseNavEvent.StartLoading()
                }
                .doOnNext {

                    list.add(
                        BlockContact(
                            0,
                            it.name,
                            it.phoneNo,
                            it.formatted_phone_no,
                            it.status ?: "",
                            null,
                            0,
                            0
                        )
                    )
                }
                .subscribe(Functions.emptyConsumer(),
                    Consumer<Throwable> {
                        LogUtil.e("BlockViewModel", "error while saving data to db ")
                    }, Action {
                        db.contactDao().insertAll(list)
                        blockNoDBListEvent.value = list
                    })
        )
    }
}