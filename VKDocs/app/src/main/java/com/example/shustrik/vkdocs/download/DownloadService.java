package com.example.shustrik.vkdocs.download;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Asynctask? DownloadManager?
 */
public class DownloadService extends IntentService {
    public static final int UPDATE_PROGRESS = 8344;
    public static final int NOTIFY_READY = 7255;
    public static final int NOTIFY_CANCELLED = 6166;
    public static final String PROGRESS = "progress";
    public static final String RECEIVER = "receiver";
    public static final String FILE = "file";
    public static final String URL = "url";

    private final IBinder mBinder = new LocalBinder();
    private boolean cancelled = false;

    public DownloadService() {
        super("DownloadService");
    }

    public static File getTempFile(int docId, String title, Context context) {
        return new File(context.getFilesDir(), String.format("%d_%s",docId, title));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String urlToDownload = intent.getStringExtra(URL);
        ResultReceiver receiver = intent.getParcelableExtra(RECEIVER);
        String file = intent.getStringExtra(FILE);
        try {
            URL url = new URL(urlToDownload);
            URLConnection connection = url.openConnection();
            connection.connect();

            int fileLength = connection.getContentLength();
            int total = 0;
            int progress = 0;

            // download the file
            InputStream input = new BufferedInputStream(connection.getInputStream());
            OutputStream output = openFileOutput(file, Context.MODE_WORLD_READABLE);

            byte data[] = new byte[1024];
            int count;

            while (!cancelled && (count = input.read(data)) != -1) {
                total += count;
                progress = total * 100 / fileLength;
                Bundle resultData = new Bundle();
                resultData.putInt(PROGRESS, progress);
                receiver.send(UPDATE_PROGRESS, resultData);
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        } catch (IOException e) {
            //RECEIVER SEND FAIL
            e.printStackTrace();
        }
        Log.w("ANNA", "cancel here");
        if (cancelled) {
            Log.w("ANNA", "Cancelled");
            File f = new File(file);
            f.delete();
            receiver.send(NOTIFY_CANCELLED, new Bundle());
        } else {
            receiver.send(NOTIFY_READY, new Bundle());
        }
    }

    public void cancel() {
        Log.w("ANNA", "cancel download service");
        cancelled = true;
    }

    public class LocalBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }
}