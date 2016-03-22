package com.example.shustrik.vkdocs.download;

import com.example.shustrik.vkdocs.CancelHandler;

public interface DocDownloader extends CancelHandler {
    boolean processToOpen(String url, String title, int docId);
    void processToOffline(String url, String title, int docId);
    void setOpenListener(DownloadListener listener);
}
