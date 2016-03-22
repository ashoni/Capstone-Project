package com.example.shustrik.vkdocs.download;

public interface DownloadListener {
    void updateOpeningProgress(int docId, int progress);
    void releaseOpening(int docId);
    int getListenerId();
}
