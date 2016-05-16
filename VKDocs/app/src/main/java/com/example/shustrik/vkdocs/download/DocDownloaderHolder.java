package com.example.shustrik.vkdocs.download;

import com.example.shustrik.vkdocs.MainActivity;

public class DocDownloaderHolder {
    static DocDownloaderImpl docDownloader;
    static boolean attached = false;

    static {
        docDownloader = new DocDownloaderImpl();
    }

    private DocDownloaderHolder() {
    }

    public static void attach(MainActivity activity) {
        docDownloader.onAttach(activity);
        attached = true;
    }

    public static void detach() {
        docDownloader.onDetach();
        attached = false;
    }

    public static DocDownloader getDocDownloader(DownloadListener listener) {
        if (!attached) {
            throw new IllegalStateException("Attach DocDownloader before using it");
        }
        docDownloader.setOpenListener(listener);
        return docDownloader;
    }
}
