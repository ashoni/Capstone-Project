package com.example.shustrik.vkdocs.common;

import android.content.ContentValues;
import android.content.Context;
import android.support.design.widget.Snackbar;

import com.example.shustrik.vkdocs.MainActivity;
import com.example.shustrik.vkdocs.R;
import com.example.shustrik.vkdocs.data.DocsContract;
import com.example.shustrik.vkdocs.sync.VKDocsSyncAdapter;
import com.example.shustrik.vkdocs.sync.VKDocsSyncService;
import com.example.shustrik.vkdocs.vk.MyVKApiDocument;
import com.example.shustrik.vkdocs.vk.VKRequestCallback;
import com.example.shustrik.vkdocs.vk.VKRequests;
import com.vk.sdk.api.VKError;

import java.io.File;


//ToDo: add export document functionality

/**
 * Wrapper for VK operations
 */
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
                              final RequestCallback callback) {
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


    public static void add(final MyVKApiDocument document, final MainActivity activity) {
        VKRequests.addToUserDocs(new VKRequestCallback<Void>() {
            @Override
            public void onSuccess(Void obj) {
                activity.getContentResolver().insert(DocsContract.DocumentEntry.CONTENT_URI,
                        DBConverter.parseIntoValues(document));
                VKDocsSyncAdapter.syncImmediately(activity);
                activity.snack(activity.getString(R.string.add_success, document.title),
                        Snackbar.LENGTH_SHORT);
            }

            @Override
            public void onError(VKError e) {
                activity.snack(activity.getString(R.string.add_new_error), Snackbar.LENGTH_LONG);
            }
        }, document.owner_id, document.id, document.access_key);
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
