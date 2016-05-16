package com.example.shustrik.vkdocs.download;

/**
 * Interface to listen DownloadService
 */
interface DownloadCallback {
    void onDownloadProgress(int docId, int progress);

    void onDownloadSuccess(int docId);

    void onDownloadFail(int docId);


    void onDownloadCancel(int docId);
}
