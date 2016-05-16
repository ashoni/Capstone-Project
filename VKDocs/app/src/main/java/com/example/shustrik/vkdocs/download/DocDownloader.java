package com.example.shustrik.vkdocs.download;


/**
 * Interface for internal download functionality
 */
public interface DocDownloader {
    boolean processToOpen(String url, String title, int docId);

    void processToOffline(String url, String title, int docId);

    void setOpenListener(DownloadListener listener);

    void onCancelPressed(int docId);
}
