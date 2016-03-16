package com.example.shustrik.vkdocs.download;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.example.shustrik.vkdocs.CancelHandler;
import com.example.shustrik.vkdocs.MainActivity;
import com.example.shustrik.vkdocs.adapters.BaseDocListAdapter;
import com.example.shustrik.vkdocs.common.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class DocDownloader implements CancelHandler, DownloadCallback {
    private Map<Integer, DownloadPack> downloadPackMap = new HashMap<>();
    protected boolean openAvailable = true;

    private Activity activity;

    public static String TAG = "ANNA_D";


    public DocDownloader(Activity activity) {
        this.activity = activity;
    }

    public boolean isDownloadAvailable() {
        return openAvailable;
    }

    public void setDownloadAvailable(boolean openAvailable) {
        this.openAvailable = openAvailable;
    }

    public void onDetach() {
        for (DownloadPack dp : downloadPackMap.values()) {
            activity.unbindService(dp.getConnection());
        }
    }

    @Override
    public void onCancelPressed(int docId) {
        Log.w(TAG, "cancel " + docId);
        downloadPackMap.get(docId).getService().cancel();
    }

    @Override
    public void onDownloadProgress(int docId, int progress) {
        downloadPackMap.get(docId).setProgress(progress);
    }

    @Override
    public void onDownloadSuccess(int docId) {
        openFile(downloadPackMap.get(docId).getFile());
        releaseDownload(docId, downloadPackMap.get(docId));
    }

    @Override
    public void onDownloadFail(int docId) {
        releaseDownload(docId, downloadPackMap.get(docId));
    }

    //internal

    private void initDownload(final int docId, DownloadPack dp) {
        dp.setDownloadMode(true);
        downloadPackMap.put(docId, dp);
        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                DownloadService.LocalBinder binder = (DownloadService.LocalBinder) service;
                downloadPackMap.get(docId).setService(binder.getService());
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };
        downloadPackMap.get(docId).setConnection(connection);
    }

    private void releaseDownload(int docId, DownloadPack dp) {
        openAvailable = true;
        activity.unbindService(dp.getConnection());
        dp.setDownloadMode(false);
        downloadPackMap.remove(docId);
    }

    private void startAndBindDownloadService(int docId, Intent intent) {
        activity.startService(intent);
        activity.bindService(intent, downloadPackMap.get(docId).getConnection(), Context.BIND_AUTO_CREATE);
    }

    private Intent createDownloadIntent(String url, String title, int docId) {
        Intent intent = new Intent(activity, DownloadService.class);
        intent.putExtra(DownloadService.URL, url);

        File tmp = DownloadService.getTempFile(docId, title, activity);
        downloadPackMap.get(docId).setFile(tmp);

        intent.putExtra(DownloadService.FILE, tmp.getName());
        intent.putExtra(DownloadService.RECEIVER, new DownloadReceiver(new Handler(), this, docId));
        return intent;
    }

    private void openFile(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Log.w(TAG, "ready to open " + file.getAbsolutePath());
        String mimetype = Utils.getFileMimeType(file);
        Log.w(TAG, "mimetype " + mimetype);

        intent.setDataAndType(Uri.fromFile(file), mimetype);
        activity.startActivity(intent);
    }

    public void processToOpen(BaseDocListAdapter.BaseAdapterViewHolder holder) {
        if (isDownloadAvailable()) {
            setDownloadAvailable(false);
            initDownload(holder.getDocId(), new DownloadPack(holder));
            startAndBindDownloadService(holder.getDocId(),
                    createDownloadIntent(holder.getUrl(), holder.getTitle(), holder.getDocId()));
        } else {
            ((MainActivity) activity).snack("Another file is opening", Snackbar.LENGTH_SHORT);
        }
    }
}
