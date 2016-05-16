package com.example.shustrik.vkdocs.files;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.example.shustrik.vkdocs.common.DBConverter;
import com.example.shustrik.vkdocs.data.DocsContract;

import java.io.File;
import java.util.HashSet;
import java.util.Set;


/**
 * Removes all temporary files created earlier than 10 minutes ago
 */
public class FilesRemoveService extends IntentService implements Loader.OnLoadCompleteListener<Cursor> {
    public static final String FRS_NAME = "FileRemoveService";

    private File dir;
    private static CursorLoader cursorLoader;
    private final int FILE_LOADER = 29;
    //10 minutes in seconds
    private final int PERIOD = 60 * 10;

    public FilesRemoveService() {
        super(FRS_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        dir = getApplicationContext().getFilesDir();
        cursorLoader = new CursorLoader(getApplicationContext(),
                DocsContract.FileEntry.CONTENT_URI,
                DBConverter.FILE_COLUMNS,
                null,
                null,
                null);
        cursorLoader.registerListener(FILE_LOADER, this);
        cursorLoader.startLoading();
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor cursor) {
        long now = System.currentTimeMillis() / 1000;
        Set<Integer> toDeleteFromTable = new HashSet<>();
        Set<String> toKeepFiles = new HashSet<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(DBConverter.COL_FILE_ID);
            String title = cursor.getString(DBConverter.COL_FILE_NAME);
            int last = cursor.getInt(DBConverter.COL_FILE_LAST);
            int keep = cursor.getInt(DBConverter.COL_FILE_OFFLINE);

            if (keep == 0 && (now - last) > PERIOD) {
                toDeleteFromTable.add(id);
            } else {
                toKeepFiles.add(title);
            }
        }
        cursor.close();

        for (Integer id : toDeleteFromTable) {
            deleteFromDB(id.toString());
        }
        if (dir.listFiles() == null) {
            return;
        }
        for (File f : dir.listFiles()) {
            if (!toKeepFiles.contains(f.getName())) {
                f.delete();
            }
        }
    }

    private void deleteFromDB(String id) {
        getContentResolver().delete(DocsContract.FileEntry.CONTENT_URI,
                DocsContract.FileEntry._ID + "=" + id, null);
    }

    @Override
    public void onDestroy() {
//        if (cursorLoader != null) {
//            cursorLoader.unregisterListener(this);
//            cursorLoader.cancelLoad();
//            cursorLoader.stopLoading();
//        }
    }

    public static class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent sendIntent = new Intent(context, FilesRemoveService.class);
            context.startService(sendIntent);
        }
    }
}
