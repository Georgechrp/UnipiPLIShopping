package com.unipi.george.unipiplishopping.utils;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

public class NotificationHelper {

    private static final String CHANNEL_ID = "default_channel";
    private static final String CHANNEL_NAME = "Default Notifications";
    private static final String CHANNEL_DESCRIPTION = "Channel for default notifications";
    private static final String PREFS_NAME = "notification_prefs";

    private final Context context;

    public NotificationHelper(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private boolean shouldSendNotification(String uniqueKey) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return !prefs.contains(uniqueKey);
    }

    private void markNotificationAsSent(String uniqueKey) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(uniqueKey, true).apply();
    }

    @SuppressLint("MissingPermission")
    public void sendSimpleNotification(String title, String message, int notificationId) {
        String uniqueKey = title + message + notificationId;

        /*if (shouldSendNotification(uniqueKey)) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notificationId, builder.build());

            markNotificationAsSent(uniqueKey);
        }*/
    }
}
