package com.example.shustrik.vkdocs.download;


interface DownloadCallback {
    void onDownloadProgress(int docId, int progress);

    void onDownloadSuccess(int docId);

    void onDownloadFail(int docId);
}
