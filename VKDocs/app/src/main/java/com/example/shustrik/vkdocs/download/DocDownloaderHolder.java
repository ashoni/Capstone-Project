package com.example.shustrik.vkdocs.download;

import android.app.Activity;
import android.util.Log;

public class DocDownloaderHolder {
    static DocDownloaderImpl docDownloader;
    static boolean attached = false;

    static {
        docDownloader = new DocDownloaderImpl();
    }

    private DocDownloaderHolder() {}

    public static void attach(Activity activity) {
        Log.w("ANNA", "Attached activity for ddholder");
        docDownloader.onAttach(activity);
        attached = true;
    }

    public static void detach() {
        Log.w("ANNA", "Detached activity from ddholder");
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
