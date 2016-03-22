package com.example.shustrik.vkdocs.common;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.example.shustrik.vkdocs.data.DocsContract;
import com.example.shustrik.vkdocs.vk.MyVKApiDocument;
import com.example.shustrik.vkdocs.vk.VKRequestCallback;
import com.example.shustrik.vkdocs.vk.VKRequests;
import com.vk.sdk.api.VKError;

import java.io.File;
import java.util.Vector;

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

    public static void rename(int ownerId, final int docId, final String title,
                              final Context context,
                              final RequestCallback callback)
    {
        VKRequests.edit(new VKRequestCallback<Void>() {
            @Override
            public void onSuccess(Void obj) {
                ContentValues cv = new ContentValues();
                cv.put(DocsContract.DocumentEntry.COLUMN_TITLE, title);
                context.getContentResolver().update(DocsContract.DocumentEntry.CONTENT_URI,
                        cv, DocsContract.DocumentEntry._ID + "=" + docId, null);
                callback.onSuccess();
            }

            @Override
            public void onError(VKError e) {
                callback.onFailure();
            }
        }, ownerId, docId, title);
    }


    public static void add(final MyVKApiDocument document, final Context context) {
        VKRequests.addToUserDocs(new VKRequestCallback<Void>() {
            @Override
            public void onSuccess(Void obj) {
                context.getContentResolver().insert(DocsContract.DocumentEntry.CONTENT_URI,
                        DBConverter.parseIntoValues(document));
            }

            @Override
            public void onError(VKError e) {
                //send notification snack
            }
        }, document.owner_id, document.id, document.access_key);
    }

    public static void exportToGoogleDrive() {

    }

    public static void exportToDropBox() {

    }

    public static void setTempAvailable(Context context, int docId, File f) {
        ContentValues cv = new ContentValues();
        cv.put(DocsContract.FileEntry._ID, docId);
        cv.put(DocsContract.FileEntry.COLUMN_OFFLINE, 0);
        cv.put(DocsContract.FileEntry.COLUMN_LAST, System.currentTimeMillis() / 1000);
        cv.put(DocsContract.FileEntry.COLUMN_NAME, f.getName());
        context.getContentResolver().insert(DocsContract.FileEntry.CONTENT_URI, cv);
    }

    public static void setOfflineAvailable(Context context, int docId, File f) {
        ContentValues cv = new ContentValues();
        cv.put(DocsContract.FileEntry._ID, docId);
        cv.put(DocsContract.FileEntry.COLUMN_OFFLINE, 1);
        cv.put(DocsContract.FileEntry.COLUMN_LAST, System.currentTimeMillis() / 1000);
        cv.put(DocsContract.FileEntry.COLUMN_NAME, f.getName());
        context.getContentResolver().insert(DocsContract.FileEntry.CONTENT_URI, cv);
    }

    public static void setOfflineUnavailable(Context context, int docId) {
        ContentValues cv = new ContentValues();
        cv.put(DocsContract.FileEntry.COLUMN_OFFLINE, 0);
        context.getContentResolver().update(DocsContract.FileEntry.CONTENT_URI,
                cv, DocsContract.FileEntry._ID + "=" + docId, null);
    }

    public interface RequestCallback {
        void onSuccess();
        void onFailure();
    }
}
