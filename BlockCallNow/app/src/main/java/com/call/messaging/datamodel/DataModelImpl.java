/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.call.messaging.datamodel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.telephony.SubscriptionManager;

import com.call.messaging.datamodel.action.ActionService;
import com.call.messaging.datamodel.action.BackgroundWorker;
import com.call.messaging.datamodel.action.FixupMessageStatusOnStartupAction;
import com.call.messaging.datamodel.action.ProcessPendingMessagesAction;
import com.call.messaging.datamodel.data.BlockedParticipantsData;
import com.call.messaging.datamodel.data.BlockedParticipantsData.BlockedParticipantsDataListener;
import com.call.messaging.datamodel.data.ContactListItemData;
import com.call.messaging.datamodel.data.ContactPickerData;
import com.call.messaging.datamodel.data.ContactPickerData.ContactPickerDataListener;
import com.call.messaging.datamodel.data.ConversationData;
import com.call.messaging.datamodel.data.ConversationData.ConversationDataListener;
import com.call.messaging.datamodel.data.ConversationListData;
import com.call.messaging.datamodel.data.ConversationListData.ConversationListDataListener;
import com.call.messaging.datamodel.data.DraftMessageData;
import com.call.messaging.datamodel.data.GalleryGridItemData;
import com.call.messaging.datamodel.data.LaunchConversationData;
import com.call.messaging.datamodel.data.LaunchConversationData.LaunchConversationDataListener;
import com.call.messaging.datamodel.data.MediaPickerData;
import com.call.messaging.datamodel.data.MessagePartData;
import com.call.messaging.datamodel.data.ParticipantData;
import com.call.messaging.datamodel.data.ParticipantListItemData;
import com.call.messaging.datamodel.data.PeopleAndOptionsData;
import com.call.messaging.datamodel.data.PeopleAndOptionsData.PeopleAndOptionsDataListener;
import com.call.messaging.datamodel.data.PeopleOptionsItemData;
import com.call.messaging.datamodel.data.SettingsData;
import com.call.messaging.datamodel.data.SettingsData.SettingsDataListener;
import com.call.messaging.datamodel.data.SubscriptionListData;
import com.call.messaging.datamodel.data.VCardContactItemData;
import com.call.messaging.sms.MmsConfig;
import com.call.messaging.util.Assert;
import com.call.messaging.util.Assert.DoesNotRunOnMainThread;
import com.call.messaging.util.ConnectivityUtil;
import com.call.messaging.util.LogUtil;
import com.call.messaging.util.OsUtil;
import com.call.messaging.util.PhoneUtils;

import java.util.concurrent.ConcurrentHashMap;

public class DataModelImpl extends DataModel {
    private final Context mContext;
    private final ActionService mActionService;
    private final BackgroundWorker mDataModelWorker;
    private final DatabaseHelper mDatabaseHelper;
    private final SyncManager mSyncManager;

    // Cached ConnectivityUtil instance for Pre-N.
    private static ConnectivityUtil sConnectivityUtilInstanceCachePreN = null;
    // Cached ConnectivityUtil subId->instance for N and beyond
    private static final ConcurrentHashMap<Integer, ConnectivityUtil>
            sConnectivityUtilInstanceCacheN = new ConcurrentHashMap<>();

    public DataModelImpl(final Context context) {
        super();
        mContext = context;
        mActionService = new ActionService();
        mDataModelWorker = new BackgroundWorker();
        mDatabaseHelper = DatabaseHelper.getInstance(context);
        mSyncManager = new SyncManager();
    }

    @Override
    public ConversationListData createConversationListData(final Context context,
            final ConversationListDataListener listener, final boolean archivedMode) {
        return new ConversationListData(context, listener, archivedMode);
    }

    @Override
    public ConversationData createConversationData(final Context context,
                                                   final ConversationDataListener listener, final String conversationId) {
        return new ConversationData(context, listener, conversationId);
    }

    @Override
    public ContactListItemData createContactListItemData() {
        return new ContactListItemData();
    }

    @Override
    public ContactPickerData createContactPickerData(final Context context,
                                                     final ContactPickerDataListener listener) {
        return new ContactPickerData(context, listener);
    }

    @Override
    public BlockedParticipantsData createBlockedParticipantsData(
            final Context context, final BlockedParticipantsDataListener listener) {
        return new BlockedParticipantsData(context, listener);
    }

    @Override
    public MediaPickerData createMediaPickerData(final Context context) {
        return new MediaPickerData(context);
    }

    @Override
    public GalleryGridItemData createGalleryGridItemData() {
        return new GalleryGridItemData();
    }

    @Override
    public LaunchConversationData createLaunchConversationData(
            final LaunchConversationDataListener listener) {
       return new LaunchConversationData(listener);
    }

    @Override
    public PeopleOptionsItemData createPeopleOptionsItemData(final Context context) {
        return new PeopleOptionsItemData(context);
    }

    @Override
    public PeopleAndOptionsData createPeopleAndOptionsData(final String conversationId,
                                                           final Context context, final PeopleAndOptionsDataListener listener) {
        return new PeopleAndOptionsData(conversationId, context, listener);
    }

    @Override
    public VCardContactItemData createVCardContactItemData(final Context context,
            final MessagePartData data) {
        return new VCardContactItemData(context, data);
    }

    @Override
    public VCardContactItemData createVCardContactItemData(final Context context,
            final Uri vCardUri) {
        return new VCardContactItemData(context, vCardUri);
    }

    @Override
    public ParticipantListItemData createParticipantListItemData(
            final ParticipantData participant) {
        return new ParticipantListItemData(participant);
    }

    @Override
    public SubscriptionListData createSubscriptonListData(Context context) {
        return new SubscriptionListData(context);
    }

    @Override
    public SettingsData createSettingsData(Context context, SettingsDataListener listener) {
        return new SettingsData(context, listener);
    }

    @Override
    public DraftMessageData createDraftMessageData(String conversationId) {
        return new DraftMessageData(conversationId);
    }

    @Override
    public ActionService getActionService() {
        // We need to allow access to this on the UI thread since it's used to start actions.
        return mActionService;
    }

    @Override
    public BackgroundWorker getBackgroundWorkerForActionService() {
        return mDataModelWorker;
    }

    @Override
    @DoesNotRunOnMainThread
    public DatabaseWrapper getDatabase() {
        // We prevent the main UI thread from accessing the database since we have to allow
        // public access to this class to enable sub-packages to access data.
        Assert.isNotMainThread();
        return mDatabaseHelper.getDatabase();
    }

    @Override
    public SyncManager getSyncManager() {
        return mSyncManager;
    }

    @Override
    void onCreateTables(final SQLiteDatabase db) {
        LogUtil.w(LogUtil.BUGLE_TAG, "Rebuilt databases: reseting related state");
        // Clear other things that implicitly reference the DB
        SyncManager.resetLastSyncTimestamps();
    }

    @Override
    public void onActivityResume() {
        // Perform an incremental sync and register for changes if necessary
        mSyncManager.updateSyncObserver(mContext);

        // Trigger a participant refresh if needed, we should only need to refresh if there is
        // contact change while the activity was paused.
        ParticipantRefresh.refreshParticipantsIfNeeded();
    }

    @Override
    public void onApplicationCreated() {
        if (OsUtil.isAtLeastN()) {
            createConnectivityUtilForEachActiveSubscription();
        } else {
            sConnectivityUtilInstanceCachePreN = new ConnectivityUtil(mContext);
        }

        FixupMessageStatusOnStartupAction.fixupMessageStatus();
        ProcessPendingMessagesAction.processFirstPendingMessage();
        SyncManager.immediateSync();

        if (OsUtil.isAtLeastL_MR1()) {
            // Start listening for subscription change events for refreshing any data associated
            // with subscriptions.
            PhoneUtils.getDefault().toLMr1().registerOnSubscriptionsChangedListener(
                    new SubscriptionManager.OnSubscriptionsChangedListener() {
                        @Override
                        public void onSubscriptionsChanged() {
                            // TODO: This dynamically changes the mms config that app is
                            // currently using. It may cause inconsistency in some cases. We need
                            // to check the usage of mms config and handle the dynamic change
                            // gracefully
                            MmsConfig.loadAsync();
                            ParticipantRefresh.refreshSelfParticipants();
                            if (OsUtil.isAtLeastN()) {
                                createConnectivityUtilForEachActiveSubscription();
                            }
                        }
                    });
        }
    }

    private void createConnectivityUtilForEachActiveSubscription() {
        PhoneUtils.forEachActiveSubscription(new PhoneUtils.SubscriptionRunnable() {
            @Override
            public void runForSubscription(int subId) {
                // Create the ConnectivityUtil instance for given subId if absent.
                if (subId <= ParticipantData.DEFAULT_SELF_SUB_ID) {
                    subId = PhoneUtils.getDefault().getDefaultSmsSubscriptionId();
                }
                if (!sConnectivityUtilInstanceCacheN.containsKey(subId)) {
                    sConnectivityUtilInstanceCacheN.put(
                            subId, new ConnectivityUtil(mContext, subId));
                }
            }
        });
    }

    public static ConnectivityUtil getConnectivityUtil(final int subId) {
        if (OsUtil.isAtLeastN()) {
            return sConnectivityUtilInstanceCacheN.get(subId);
        } else {
            return sConnectivityUtilInstanceCachePreN;
        }
    }
}
