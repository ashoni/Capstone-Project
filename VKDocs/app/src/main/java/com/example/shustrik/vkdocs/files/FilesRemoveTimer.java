package com.example.shustrik.vkdocs.files;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;


public class FilesRemoveTimer {
    private AlarmManager am;
    private Context context;
    private PendingIntent fileRemoveIntent;

    private static final int FIRST_SLEEP = 60;
    private static final int PERIOD = 60 * 60;
    private static final int REQUEST_CODE = 0;


    public FilesRemoveTimer(Context context) {
        this.context = context;
        am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        startTimer();
    }

    private void startTimer() {
        if (fileRemoveIntent == null) {
            createIntent();
        }
        Log.w("ANNA", "Starting timer");
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, FIRST_SLEEP * 1000,
                PERIOD * 1000, fileRemoveIntent);
    }

    private void createIntent() {
        Intent alarmIntent = new Intent(context, FilesRemoveService.AlarmReceiver.class);
        fileRemoveIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
