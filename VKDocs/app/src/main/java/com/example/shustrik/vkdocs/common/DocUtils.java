package com.example.shustrik.vkdocs.common;

import android.content.Context;
import android.util.Log;

import com.example.shustrik.vkdocs.data.DocsContract;
import com.example.shustrik.vkdocs.vk.VKRequestCallback;
import com.example.shustrik.vkdocs.vk.VKRequests;
import com.vk.sdk.api.VKError;

public class DocUtils {
    public static void delete(int ownerId, final int docId,
                              final Context context,
                              final RequestCallback callback
                              ) {
        VKRequests.delete(new VKRequestCallback<Void>() {
            @Override
            public void onSuccess(Void obj) {
                context.getContentResolver().delete(DocsContract.FileEntry.CONTENT_URI,
                        DocsContract.FileEntry._ID + "=" + docId, null);
                context.getContentResolver().delete(DocsContract.DocumentEntry.CONTENT_URI,
                        DocsContract.DocumentEntry._ID + "=" + docId, null);
                callback.onSuccess();
            }

            @Override
            public void onError(VKError e) {
                callback.onFailure();
            }
        }, ownerId, docId);
    }

    public static void exportToGoogleDrive() {

    }

    public static void exportToDropBox() {

    }

    public interface RequestCallback {
        void onSuccess();
        void onFailure();
    }
}
