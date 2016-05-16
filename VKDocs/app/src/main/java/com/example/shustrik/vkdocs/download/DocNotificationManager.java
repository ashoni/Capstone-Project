package com.example.shustrik.vkdocs.download;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.example.shustrik.vkdocs.MainActivity;
import com.example.shustrik.vkdocs.R;

public class DocNotificationManager {
    private DocNotificationManager() {
    }

    public static void createUploadingNotification(Context context, String title) {
        createNotification(context, context.getString(R.string.upload_to_vk_notification, title),
                title.hashCode(),
                R.drawable.ic_file_upload_black_24dp);
    }

    public static void createDownloadingNotification(Context context, String title, int docId) {
        createNotification(context, context.getString(R.string.making_offline_notification, title),
                docId,
                R.drawable.ic_file_download_black_24dp);
    }

    private static void createNotification(Context context, String text, int notificationId, int iconId) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent =
                PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(iconId)
                .setContentIntent(contentIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder = builder.setContent(getComplexNotificationView(context, text));
        } else {
            builder = builder.setContentTitle(text);
        }

        notificationManager.notify(notificationId, builder.build());
    }


    private static RemoteViews getComplexNotificationView(Context context, String text) {
        RemoteViews notificationView = new RemoteViews(context.getPackageName(), R.layout.loading_notification);
        notificationView.setTextViewText(R.id.loading_title, text);
        return notificationView;
    }

    public static void dismissNotification(Context context, int notificationId) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }
}
