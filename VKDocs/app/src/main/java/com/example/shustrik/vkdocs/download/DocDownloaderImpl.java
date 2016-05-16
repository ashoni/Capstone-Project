package com.example.shustrik.vkdocs.download;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.Snackbar;

import com.example.shustrik.vkdocs.MainActivity;
import com.example.shustrik.vkdocs.R;
import com.example.shustrik.vkdocs.common.DocUtils;
import com.example.shustrik.vkdocs.common.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements DocDownloader
 */
class DocDownloaderImpl implements DownloadCallback, DocDownloader {
    private Map<Integer, DownloadPack> downloadPackMap = new HashMap<>();
    private int openingDocId = -1;

    private MainActivity activity;
    private DownloadListener openListener;


    public DocDownloaderImpl() {
    }


    /**
     * no need to keep loading file for open file if user moved to another fragment
     */
    @Override
    public void setOpenListener(DownloadListener openListener) {
        int curListenerId = this.openListener == null ? -1 : this.openListener.getListenerId();
        int newListenerId = openListener.getListenerId();

        if (openingDocId != -1 && curListenerId != newListenerId) {
            if (this.openListener != null) {
                this.openListener.releaseOpening(openingDocId);
            }
            if (downloadPackMap.get(openingDocId) == null) {
                openingDocId = -1;
            } else if (downloadPackMap.get(openingDocId).getGoal() == DownloadPack.GOAL.TEMP) {
                onCancelPressed(openingDocId);
            }
        }
        this.openListener = openListener;
    }

    private boolean isOpeningAvailable() {
        return openingDocId == -1;
    }

    public void setOpeningId(int openingDocId) {
        this.openingDocId = openingDocId;
    }

    public void onAttach(MainActivity activity) {
        this.activity = activity;
        //reattach all
    }

    public void onDetach() {
        for (DownloadPack dp : downloadPackMap.values()) {
            if (dp.isAttached()) {
                activity.unbindService(dp.getConnection());
            }
            dp.setAttached(false);
        }
    }

    @Override
    public void onCancelPressed(int docId) {
        DownloadPack pack = downloadPackMap.get(docId);
        if (pack != null) {
            pack.cancelLoading();
            releaseDownload(docId, pack);
        }
    }

    @Override
    public void onDownloadProgress(int docId, int progress) {
        //check adapter id (map docid - adapterid)
        if (openingDocId == docId) {
            openListener.updateOpeningProgress(docId, progress);
        }
    }

    @Override
    public void onDownloadSuccess(final int docId) {
        final File f = downloadPackMap.get(docId).getFile();
        if (docId == openingDocId) {
            MediaScannerConnection.scanFile(activity,
                    new String[]{f.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            openFile(f);
                        }
                    });
        }
        if (downloadPackMap.get(docId).getGoal() == DownloadPack.GOAL.SAVE_TO_OFFLINE) {
            MediaScannerConnection.scanFile(activity,
                    new String[]{f.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            DocUtils.setOfflineAvailable(activity, docId, f);
                        }
                    });
        }
        releaseDownload(docId, downloadPackMap.get(docId));
    }

    @Override
    public void onDownloadFail(int docId) {
        activity.snack(activity.getString(R.string.download_failed), Snackbar.LENGTH_SHORT);
        releaseDownload(docId, downloadPackMap.get(docId));
    }

    @Override
    public void onDownloadCancel(int docId) {
        releaseDownload(docId, downloadPackMap.get(docId));
    }

    //internal

    private void initDownload(int docId, final DownloadPack dp) {
        downloadPackMap.put(docId, dp);
        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                DownloadService.LocalBinder binder = (DownloadService.LocalBinder) service;
                dp.setService(binder.getService());
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };
        dp.setConnection(connection);
    }

    private void releaseDownload(int docId, DownloadPack dp) {
        if (dp != null && dp.isAttached()) {
            try {
                activity.unbindService(dp.getConnection());
            } catch (Exception e) {
                //after rotation service won't be binded
            }
        }
        if (openingDocId == docId) {
            openListener.releaseOpening(docId);
            openingDocId = -1;
        }
        DocNotificationManager.dismissNotification(activity, docId);
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

        String mimetype = Utils.getFileMimeType(file);

        intent.setDataAndType(Uri.fromFile(file), mimetype);
        activity.startActivity(intent);
    }

    public boolean processToOpen(String url, String title, int docId) {
        if (isOpeningAvailable()) {
            setOpeningId(docId);
            if (!downloadPackMap.containsKey(docId)) {
                initDownload(docId, new DownloadPack(DownloadPack.GOAL.TEMP));
                startAndBindDownloadService(docId, createDownloadIntent(url, title, docId));
            }
            return true;
        } else {
            activity.snack(activity.getString(R.string.open_concurrent), Snackbar.LENGTH_SHORT);
            return false;
        }
    }

    public void processToOffline(String url, String title, int docId) {
        if (!downloadPackMap.containsKey(docId)) {
            initDownload(docId, new DownloadPack(DownloadPack.GOAL.SAVE_TO_OFFLINE));
            startAndBindDownloadService(docId, createDownloadIntent(url, title, docId));
            DocNotificationManager.createDownloadingNotification(activity, title, docId);
        }
    }
}
