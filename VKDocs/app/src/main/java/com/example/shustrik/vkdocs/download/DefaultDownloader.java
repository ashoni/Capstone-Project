package com.example.shustrik.vkdocs.download;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.example.shustrik.vkdocs.R;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Calls default download activity
 */
public class DefaultDownloader {
    public static final int DOWNLOAD_PERMISSION = 19;
    private final Queue<DownloadManager.Request> requests = new LinkedList<>();
    private Activity activity;
    private static DefaultDownloader downloader;

    public static void init(Activity activity) {
        downloader = new DefaultDownloader(activity);
    }

    public static DefaultDownloader getInstance() {
        return downloader;
    }

    private DefaultDownloader(Activity activity) {
        this.activity = activity;
    }

    public synchronized void downloadFile(String url, String title) {
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setAllowedOverRoaming(false);
        request.setTitle(title);
        request.setDescription("VKDocs");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title);
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        requests.offer(request);
        checkPermissions();
    }


    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showMessage(activity.getString(R.string.ask_for_download_permission),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == DialogInterface.BUTTON_POSITIVE) {
                                    requestPermissions();
                                }
                            }
                        });
            } else {
                requestPermissions();
            }
        } else {
            download();
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, DOWNLOAD_PERMISSION);
    }

    private void showMessage(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton(activity.getString(R.string.ok), okListener)
                .setNegativeButton(activity.getString(R.string.cancel), null)
                .create()
                .show();
    }

    public void download() {
        DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(requests.poll());
    }
}
