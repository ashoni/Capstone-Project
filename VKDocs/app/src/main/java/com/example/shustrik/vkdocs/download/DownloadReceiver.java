package com.example.shustrik.vkdocs.download;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Receives notifications from the DownloadService and passes it to the DownloadCallback
 */
class DownloadReceiver extends ResultReceiver {
    private DownloadCallback callback;
    private int docId;

    public DownloadReceiver(Handler handler, DownloadCallback callback, int docId) {
        super(handler);
        this.callback = callback;
        this.docId = docId;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);

        if (resultCode == DownloadService.UPDATE_PROGRESS) {
            callback.onDownloadProgress(docId, resultData.getInt(DownloadService.PROGRESS));
        } else if (resultCode == DownloadService.NOTIFY_READY) {
            callback.onDownloadSuccess(docId);
        } else if (resultCode == DownloadService.NOTIFY_CANCELLED) {
            callback.onDownloadCancel(docId);
        } else if (resultCode == DownloadService.NOTIFY_FAILED) {
            callback.onDownloadFail(docId);
        }
    }
}