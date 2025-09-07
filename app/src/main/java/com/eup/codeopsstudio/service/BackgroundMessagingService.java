package com.eup.codeopsstudio.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.eup.codeopsstudio.MainActivity;
import com.eup.codeopsstudio.common.AsyncTask;
import com.eup.codeopsstudio.common.ILog;
import com.eup.codeopsstudio.res.R;
import com.eup.codeopsstudio.service.fcm.FCMWorker;
import com.eup.codeopsstudio.util.BaseUtil;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * If you wish to do any message handling beyond receiving notifications on apps in the
 * background, create a new Service ( File > New > Service > Service ) that extends
 * FirebaseMessagingService . This service is necessary to receive notifications in foregrounded
 * apps, to receive data payload, to send upstream messages, and so on.
 * n this service create an onMessageReceived method to handle incoming messages.
 */
public class BackgroundMessagingService extends FirebaseMessagingService {
    private static final String TAG = "BackgroundMessagingService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        ILog.debug(TAG, "From: " + remoteMessage.getFrom());

        if (!remoteMessage
            .getData()
            .isEmpty()) {
            ILog.debug(TAG, "Message data payload: " + remoteMessage.getData());

            // Example: Check for a specific key in the data payload
            boolean needsLongRunningTask = remoteMessage
                .getData()
                .containsKey("long_task") && "true".equalsIgnoreCase(remoteMessage
                .getData()
                .get("long_task"));
            if (needsLongRunningTask) {
                scheduleJob(remoteMessage.getData());
            } else {
                handleNow(remoteMessage.getData());
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            ILog.debug(TAG, "Message Notification Body: " + remoteMessage
                .getNotification()
                .getBody());
            String notificationTitle = remoteMessage
                .getNotification()
                .getTitle();
            String notificationBody = remoteMessage
                .getNotification()
                .getBody();

            // If the app is in the foreground, the system won't show the notification
            // automatically.
            // You might want to display your own custom notification here.
            // This is also where you'd handle creating a notification from a data-only message.
            sendNotification(notificationTitle, notificationBody, remoteMessage.getData());
        } else if (!remoteMessage
            .getData()
            .isEmpty()) {
            // If it's a data-only message and you want to create a notification:
            // Extract title and body from data payload if they exist
            String title = remoteMessage
                .getData()
                .getOrDefault("title", "New Message");
            String body = remoteMessage
                .getData()
                .getOrDefault("body", "You have a new message.");
            sendNotification(title, body, remoteMessage.getData());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    // [START on_new_token]

    /**
     * There are two scenarios when onNewToken is called:
     * 1) When a new token is generated on initial app startup
     * 2) Whenever an existing token is changed
     * Under #2, there are three scenarios when the existing token is changed:
     * A) App is restored to a new device
     * B) User uninstalls/reinstalls the app
     * C) User clears app data
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        final String msg = "Refreshed token: " + token;
        ILog.debug(TAG, msg);
        AsyncTask.runOnUiThread(() -> BaseUtil.toastLong(msg));

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token);
    }

    /**
     * Persist token to third-party servers.
     * <p>
     * Modify this method to associate the user's FCM registration token with any
     * server-side account maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }

    /**
     * Handles short-lived tasks (less than 10 seconds
     *
     * @param dataPayload the received message payload
     * @see #scheduleJob(Map)
     */
    private void handleNow(@NonNull Map<String, String> dataPayload) {
        ILog.debug(TAG, "Short lived task is done.");
        // Example: Show a local notification, update UI if app is in foreground,
        // or process simple data.
    }

    /**
     * Handles long-running tasks
     *
     * @param dataPayload the received message payload
     * @see #handleNow(Map)
     */
    private void scheduleJob(@NonNull Map<String, String> dataPayload) {
        ILog.debug(TAG, "Long running task needs to be scheduled.");

        Data.Builder dataBuilder = new Data.Builder();
        for (Map.Entry<String, String> entry : dataPayload.entrySet()) {
            dataBuilder.putString(entry.getKey(), entry.getValue());
        }

        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest.Builder(FCMWorker.class);
        builder.setInputData(dataBuilder.build());
        OneTimeWorkRequest request = builder.build();
        WorkManager
            .getInstance(this)
            .beginWith(request)
            .enqueue();
    }

    private void sendNotification(String messageTitle, String messageBody,
        @NonNull Map<String, String> data) {
        Intent intent = new Intent(this, MainActivity.class);
        // You can add extras to the intent from the 'data' payload if needed
        for (Map.Entry<String, String> entry : data.entrySet()) {
            intent.putExtra(entry.getKey(), entry.getValue());
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        String channelId = getString(R.string.cloud_messaging_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,
            channelId).setSmallIcon(com.eup.codeopsstudio.res.R.drawable.ic_codeopsstudio) //
                      .setContentTitle(messageTitle)
                      .setContentText(messageBody)
                      .setAutoCancel(true)
                      .setSound(defaultSoundUri)
                      .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(channelId,
            "Channel human readable " + "title", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}


