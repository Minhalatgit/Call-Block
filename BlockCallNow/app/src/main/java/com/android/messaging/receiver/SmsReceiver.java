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

package com.android.messaging.receiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.provider.Telephony.Sms;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import androidx.core.app.NotificationCompat.Style;
import androidx.core.app.NotificationManagerCompat;

import com.android.messaging.Factory;
import com.android.messaging.datamodel.BugleNotifications;
import com.android.messaging.datamodel.MessageNotificationState;
import com.android.messaging.datamodel.NoConfirmationSmsSendService;
import com.android.messaging.datamodel.action.ReceiveSmsMessageAction;
import com.android.messaging.sms.MmsUtils;
import com.android.messaging.ui.UIIntents;
import com.android.messaging.util.BugleGservices;
import com.android.messaging.util.BugleGservicesKeys;
import com.android.messaging.util.DebugUtils;
import com.android.messaging.util.LogUtil;
import com.android.messaging.util.OsUtil;
import com.android.messaging.util.PendingIntentConstants;
import com.android.messaging.util.PhoneUtils;
import com.blockcallnow.R;
import com.blockcallnow.app.BlockCallApplication;
import com.blockcallnow.data.model.BaseResponse;
import com.blockcallnow.data.model.BlockNoDetail;
import com.blockcallnow.data.model.BlockNoDetails;
import com.blockcallnow.data.model.UserDetail;
import com.blockcallnow.data.preference.BlockCallsPref;
import com.blockcallnow.data.preference.LoginPref;
import com.blockcallnow.data.room.BlockContactDao;
import com.blockcallnow.data.room.LogContact;
import com.blockcallnow.util.Utils;

import org.jetbrains.annotations.NotNull;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.blockcallnow.data.network.ApiConstant.TWILIO_NUMBER;

/**
 * Class that receives incoming SMS messages through android.provider.Telephony.SMS_RECEIVED
 * <p>
 * This class serves two purposes:
 * - Process phone verification SMS messages
 * - Handle SMS messages when the user has enabled us to be the default SMS app (Pre-KLP)
 */
public final class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = LogUtil.BUGLE_TAG;

    private static ArrayList<Pattern> sIgnoreSmsPatterns;

    /**
     * Enable or disable the SmsReceiver as appropriate. Pre-KLP we use this receiver for
     * receiving incoming SMS messages. For KLP+ this receiver is not used when running as the
     * primary user and the SmsDeliverReceiver is used for receiving incoming SMS messages.
     * When running as a secondary user, this receiver is still used to trigger the incoming
     * notification.
     */
    public static void updateSmsReceiveHandler(final Context context) {
        boolean smsReceiverEnabled;
        boolean mmsWapPushReceiverEnabled;
        boolean respondViaMessageEnabled;
        boolean broadcastAbortEnabled;

        if (OsUtil.isAtLeastKLP()) {
            // When we're running as the secondary user, we don't get the new SMS_DELIVER intent,
            // only the primary user receives that. As secondary, we need to go old-school and
            // listen for the SMS_RECEIVED intent. For the secondary user, use this SmsReceiver
            // for both sms and mms notification. For the primary user on KLP (and above), we don't
            // use the SmsReceiver.
            smsReceiverEnabled = OsUtil.isSecondaryUser();
            // On KLP use the new deliver event for mms
            mmsWapPushReceiverEnabled = false;
            // On KLP we need to always enable this handler to show in the list of sms apps
            respondViaMessageEnabled = true;
            // On KLP we don't need to abort the broadcast
            broadcastAbortEnabled = false;
        } else {
            // On JB we use the sms receiver for both sms/mms delivery
            final boolean carrierSmsEnabled = PhoneUtils.getDefault().isSmsEnabled();
            smsReceiverEnabled = carrierSmsEnabled;

            // On JB we use the mms receiver when sms/mms is enabled
            mmsWapPushReceiverEnabled = carrierSmsEnabled;
            // On JB this is dynamic to make sure we don't show in dialer if sms is disabled
            respondViaMessageEnabled = carrierSmsEnabled;
            // On JB we need to abort broadcasts if SMS is enabled
            broadcastAbortEnabled = carrierSmsEnabled;
        }

        final PackageManager packageManager = context.getPackageManager();
        final boolean logv = LogUtil.isLoggable(TAG, LogUtil.VERBOSE);
        if (smsReceiverEnabled) {
            if (logv) {
                LogUtil.v(TAG, "Enabling SMS message receiving");
            }
            packageManager.setComponentEnabledSetting(
                    new ComponentName(context, SmsReceiver.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        } else {
            if (logv) {
                LogUtil.v(TAG, "Disabling SMS message receiving");
            }
            packageManager.setComponentEnabledSetting(
                    new ComponentName(context, SmsReceiver.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
        if (mmsWapPushReceiverEnabled) {
            if (logv) {
                LogUtil.v(TAG, "Enabling MMS message receiving");
            }
            packageManager.setComponentEnabledSetting(
                    new ComponentName(context, MmsWapPushReceiver.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        } else {
            if (logv) {
                LogUtil.v(TAG, "Disabling MMS message receiving");
            }
            packageManager.setComponentEnabledSetting(
                    new ComponentName(context, MmsWapPushReceiver.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
        if (broadcastAbortEnabled) {
            if (logv) {
                LogUtil.v(TAG, "Enabling SMS/MMS broadcast abort");
            }
            packageManager.setComponentEnabledSetting(
                    new ComponentName(context, AbortSmsReceiver.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            packageManager.setComponentEnabledSetting(
                    new ComponentName(context, AbortMmsWapPushReceiver.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        } else {
            if (logv) {
                LogUtil.v(TAG, "Disabling SMS/MMS broadcast abort");
            }
            packageManager.setComponentEnabledSetting(
                    new ComponentName(context, AbortSmsReceiver.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            packageManager.setComponentEnabledSetting(
                    new ComponentName(context, AbortMmsWapPushReceiver.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
        if (respondViaMessageEnabled) {
            if (logv) {
                LogUtil.v(TAG, "Enabling respond via message intent");
            }
            packageManager.setComponentEnabledSetting(
                    new ComponentName(context, NoConfirmationSmsSendService.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        } else {
            if (logv) {
                LogUtil.v(TAG, "Disabling respond via message intent");
            }
            packageManager.setComponentEnabledSetting(
                    new ComponentName(context, NoConfirmationSmsSendService.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

    private static final String EXTRA_ERROR_CODE = "errorCode";
    private static final String EXTRA_SUB_ID = "subscription";

    public static void deliverSmsIntent(final Context context, final Intent intent) {
        final android.telephony.SmsMessage[] messages = getMessagesFromIntent(intent);

        // Check messages for validity
        if (messages == null || messages.length < 1) {
            LogUtil.e(TAG, "processReceivedSms: null or zero or ignored message");
            return;
        }

        final int errorCode =
                intent.getIntExtra(EXTRA_ERROR_CODE, SendStatusReceiver.NO_ERROR_CODE);
        // Always convert negative subIds into -1
        int subId = PhoneUtils.getDefault().getEffectiveIncomingSubIdFromSystem(
                intent, EXTRA_SUB_ID);
        deliverSmsMessages(context, subId, errorCode, messages);
        if (MmsUtils.isDumpSmsEnabled()) {
            final String format = intent.getStringExtra("format");
            DebugUtils.dumpSms(messages[0].getTimestampMillis(), messages, format);
        }
    }

    public static void deliverSmsMessages(final Context context, final int subId,
                                          final int errorCode, final android.telephony.SmsMessage[] messages) {
        final ContentValues messageValues =
                MmsUtils.parseReceivedSmsMessage(context, messages, errorCode);
        String address = messageValues.getAsString(Sms.ADDRESS);
        LogUtil.e(TAG, "address " + address);
        UserDetail user = LoginPref.INSTANCE.getLoginObject(context);
        if (user != null) {
            BlockContactDao dao = BlockCallApplication.Companion.getAppContext().getDb().contactDao();
            String blockNumber = Utils.Companion.getBlockNumber(context, address);

            if (dao.getBlockContactFromNumber(blockNumber) != null) {
                Log.e(TAG, blockNumber + " is present in blocked list");

                getDetailAndSendSms(address, context, dao);

                return;
            } else if (BlockCallsPref.INSTANCE.getMsgUnknownNumber(context)) {
                Log.d(TAG, address + " block unknown number feature");
                if (!Utils.Companion.contactExists(context, address)) {
                    Log.d(TAG, address + " does not exist");

                    getDetailAndSendSms(address, context, dao);

                    return;
                }
            } else if (BlockCallsPref.INSTANCE.getMsgNonNumericNUmber(context)) {
                if (Pattern.compile(
                        "[a-z]").matcher(address.toLowerCase(Locale.ROOT)).find()) {
                    LogUtil.e(TAG, "the number is non numeric");

                    getDetailAndSendSms(address, context, dao);

                    return;
                }
            }
        }

        LogUtil.v(TAG, "SmsReceiver.deliverSmsMessages");

        final long nowInMillis = System.currentTimeMillis();
        final long receivedTimestampMs = MmsUtils.getMessageDate(messages[0], nowInMillis);

        messageValues.put(Sms.Inbox.DATE, receivedTimestampMs);
        // Default to unread and unseen for us but ReceiveSmsMessageAction will override
        // seen for the telephony db.
        messageValues.put(Sms.Inbox.READ, 0);
        messageValues.put(Sms.Inbox.SEEN, 0);
        if (OsUtil.isAtLeastL_MR1()) {
            messageValues.put(Sms.SUBSCRIPTION_ID, subId);
        }

        if (messages[0].getMessageClass() == android.telephony.SmsMessage.MessageClass.CLASS_0 ||
                DebugUtils.debugClassZeroSmsEnabled()) {
            Factory.get().getUIIntents().launchClassZeroActivity(context, messageValues);
        } else {
            final ReceiveSmsMessageAction action = new ReceiveSmsMessageAction(messageValues);
            action.start();
        }
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {

        LogUtil.v(TAG, "SmsReceiver.onReceive " + intent);
        // On KLP+ we only take delivery of SMS messages in SmsDeliverReceiver.
        if (PhoneUtils.getDefault().isSmsEnabled()) {
            final String action = intent.getAction();
            if (OsUtil.isSecondaryUser() &&
                    (Sms.Intents.SMS_RECEIVED_ACTION.equals(action) ||
                            // TODO: update this with the actual constant from Telephony
                            "android.provider.Telephony.MMS_DOWNLOADED".equals(action))) {
                postNewMessageSecondaryUserNotification();
            } else if (!OsUtil.isAtLeastKLP()) {
                deliverSmsIntent(context, intent);
            }
        }
//        abortBroadcast();
    }

    private static class SecondaryUserNotificationState extends MessageNotificationState {
        SecondaryUserNotificationState() {
            super(null);
        }

        @Override
        protected Style build(Builder builder) {
            return null;
        }

        @Override
        public boolean getNotificationVibrate() {
            return true;
        }
    }

    public static void postNewMessageSecondaryUserNotification() {
        final Context context = Factory.get().getApplicationContext();
        final Resources resources = context.getResources();
        final PendingIntent pendingIntent = UIIntents.get()
                .getPendingIntentForSecondaryUserNewMessageNotification(context);

        final Builder builder = new Builder(context, context.getString(R.string.channel_general_notification_id));
        builder.setContentTitle(resources.getString(R.string.secondary_user_new_message_title))
                .setTicker(resources.getString(R.string.secondary_user_new_message_ticker))
                .setChannelId(context.getString(R.string.channel_general_notification_id))
                .setSmallIcon(R.drawable.ic_sms_light)
                // Returning PRIORITY_HIGH causes L to put up a HUD notification. Without it, the ticker
                // isn't displayed.
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        final NotificationCompat.BigTextStyle bigTextStyle =
                new NotificationCompat.BigTextStyle(builder);
        bigTextStyle.bigText(resources.getString(R.string.secondary_user_new_message_title));
        final Notification notification = bigTextStyle.build();

        final NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(Factory.get().getApplicationContext());

        int defaults = Notification.DEFAULT_LIGHTS;
        if (BugleNotifications.shouldVibrate(new SecondaryUserNotificationState())) {
            defaults |= Notification.DEFAULT_VIBRATE;
        }
        notification.defaults = defaults;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel(context.getString(R.string.channel_general_notification_id), context.getString(R.string.channel_general_notification_name), NotificationManager.IMPORTANCE_HIGH));
        }
        notificationManager.notify(getNotificationTag(),
                PendingIntentConstants.SMS_SECONDARY_USER_NOTIFICATION_ID, notification);
    }

    /**
     * Cancel the notification
     */
    public static void cancelSecondaryUserNotification() {
        final NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(Factory.get().getApplicationContext());
        notificationManager.cancel(getNotificationTag(),
                PendingIntentConstants.SMS_SECONDARY_USER_NOTIFICATION_ID);
    }

    private static String getNotificationTag() {
        return Factory.get().getApplicationContext().getPackageName() + ":secondaryuser";
    }

    /**
     * Compile all of the patterns we check for to ignore system SMS messages.
     */
    private static void compileIgnoreSmsPatterns() {
        // Get the pattern set from GServices
        final String smsIgnoreRegex = BugleGservices.get().getString(
                BugleGservicesKeys.SMS_IGNORE_MESSAGE_REGEX,
                BugleGservicesKeys.SMS_IGNORE_MESSAGE_REGEX_DEFAULT);
        if (smsIgnoreRegex != null) {
            final String[] ignoreSmsExpressions = smsIgnoreRegex.split("\n");
            if (ignoreSmsExpressions.length != 0) {
                sIgnoreSmsPatterns = new ArrayList<Pattern>();
                for (int i = 0; i < ignoreSmsExpressions.length; i++) {
                    try {
                        sIgnoreSmsPatterns.add(Pattern.compile(ignoreSmsExpressions[i]));
                    } catch (PatternSyntaxException e) {
                        LogUtil.e(TAG, "compileIgnoreSmsPatterns: Skipping bad expression: " +
                                ignoreSmsExpressions[i]);
                    }
                }
            }
        }
    }

    /**
     * Get the SMS messages from the specified SMS intent.
     *
     * @return the messages. If there is an error or the message should be ignored, return null.
     */
    public static android.telephony.SmsMessage[] getMessagesFromIntent(Intent intent) {
        final android.telephony.SmsMessage[] messages = Sms.Intents.getMessagesFromIntent(intent);

        // Check messages for validity
        if (messages == null || messages.length < 1) {
            return null;
        }
        // Sometimes, SmsMessage.mWrappedSmsMessage is null causing NPE when we access
        // the methods on it although the SmsMessage itself is not null. So do this check
        // before we do anything on the parsed SmsMessages.
        try {
            final String messageBody = messages[0].getDisplayMessageBody();
            if (messageBody != null) {
                // Compile patterns if necessary
                if (sIgnoreSmsPatterns == null) {
                    compileIgnoreSmsPatterns();
                }
                // Check against filters
                for (final Pattern pattern : sIgnoreSmsPatterns) {
                    if (pattern.matcher(messageBody).matches()) {
                        return null;
                    }
                }
            }
        } catch (final NullPointerException e) {
            LogUtil.e(TAG, "shouldIgnoreMessage: NPE inside SmsMessage");
            return null;
        }
        return messages;
    }


    /**
     * Check the specified SMS intent to see if the message should be ignored
     *
     * @return true if the message should be ignored
     */
    public static boolean shouldIgnoreMessage(Intent intent) {
        return getMessagesFromIntent(intent) == null;
    }

    private static void getDetailAndSendSms(String phoneNumber, Context context, BlockContactDao dao) {

        String blockNumber = Utils.Companion.getBlockNumber(context, phoneNumber);
        Log.e(TAG, blockNumber + " get detail from it and send sms and phone number is " + phoneNumber);

        BlockCallApplication.Companion.getAppContext().getApi2().getBlockNoDetailForAudio(
                "Bearer " + LoginPref.INSTANCE.getApiToken(context),
                blockNumber.replaceAll("[\\s\\-]", "")
        ).enqueue(new Callback<BaseResponse<BlockNoDetail>>() {
            @Override
            public void onResponse(@NotNull Call<BaseResponse<BlockNoDetail>> call, @NotNull Response<BaseResponse<BlockNoDetail>> response) {
                Log.e(TAG, "onResponse: " + response.body());

                BlockNoDetails blockDetail = response.body().getData().getBlockNoDetails();
                if (blockDetail != null) {
                    String blockName = blockDetail.getName();
                    String blockNum = blockDetail.getPhoneNo();
                    if (blockName != null && blockName.equalsIgnoreCase("Unknown")) {
                        blockName = blockNum;
                    }
                    Log.e(TAG, "Block number:" + blockNum + "Block name: " + blockName);
                    dao.insertLog(new LogContact(0, blockName, blockNum, false));

                    if (blockDetail.getMessage() != null) {
                        Utils.Companion.smsTwiloNumber(blockDetail.getPhoneNo(), TWILIO_NUMBER, blockDetail.getMessage());
                    } else {
                        Utils.Companion.smsTwiloNumber(blockDetail.getPhoneNo(), TWILIO_NUMBER, "The person you’ve text has blocked you. Good Bye!");
                    }
                } else {
                    Log.e(TAG, "Sending sms other than blocked contact list");
                    dao.insertLog(new LogContact(0, phoneNumber, phoneNumber, false));
                    Utils.Companion.smsTwiloNumber(phoneNumber, TWILIO_NUMBER, "The person you’ve text has blocked you. Good Bye!");
                }
            }

            @Override
            public void onFailure(@NotNull Call<BaseResponse<BlockNoDetail>> call, @NotNull Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }
}
