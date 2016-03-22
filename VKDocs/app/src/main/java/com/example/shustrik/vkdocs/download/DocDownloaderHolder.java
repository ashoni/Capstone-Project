package com.example.shustrik.vkdocs.download;

import android.app.Activity;

public class DocDownloaderHolder {
    static DocDownloaderImpl docDownloader;
    static boolean attached = false;

    static {
        docDownloader = new DocDownloaderImpl();
    }

    private DocDownloaderHolder() {}

    public static void attach(Activity activity) {
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
