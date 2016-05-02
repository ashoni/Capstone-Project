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

    public static void createNotification(Context context, String title, int docId) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent =
                PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_file_download_black_24dp)
                .setContentIntent(contentIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder = builder.setContent(getComplexNotificationView(context, title));
        } else {
            builder = builder.setContentTitle("Making " + title + " available offline");
        }

//        Bitmap largeIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.art_storm);
//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(context)
//
//                        .setSmallIcon(R.drawable.art_clear)
//                        .setLargeIcon(largeIcon)
//                        .setContentTitle("Weather Alert!")
//                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
//                        .setContentText(message)
//                        .setPriority(NotificationCompat.PRIORITY_HIGH);
//        mBuilder.setContentIntent(contentIntent);
        notificationManager.notify(docId, builder.build());
    }


    private static RemoteViews getComplexNotificationView(Context context, String title) {
        RemoteViews notificationView = new RemoteViews(context.getPackageName(), R.layout.loading_notification);
        notificationView.setTextViewText(R.id.loading_title, "Making " + title + " available offline");
        return notificationView;
    }

    public static void dismissNotification(Context context, int docId) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(docId);
    }
}
