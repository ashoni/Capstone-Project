package com.example.shustrik.vkdocs.common;

import android.content.ContentValues;

import com.example.shustrik.vkdocs.data.DocsContract;
import com.example.shustrik.vkdocs.vk.MyVKApiDocument;

public class DBConverter {
    public static final String[] DOC_CONST_COLUMNS = {
            DocsContract.DocumentEntry.TABLE_NAME + "." + DocsContract.DocumentEntry._ID,
            DocsContract.DocumentEntry.COLUMN_OWNER_ID,
            DocsContract.FileEntry.COLUMN_OFFLINE,
            DocsContract.DocumentEntry.COLUMN_URL,
            DocsContract.DocumentEntry.COLUMN_TITLE,
            DocsContract.DocumentEntry.COLUMN_TYPE,
            DocsContract.DocumentEntry.COLUMN_ACCESS_KEY,
            DocsContract.DocumentEntry.COLUMN_PREVIEW_URL,
            DocsContract.DocumentEntry.COLUMN_SIZE,
            DocsContract.DocumentEntry.COLUMN_DATE
    };

    public static final int COL_DOC_ID = 0;
    public static final int COL_DOC_OWNER_ID = 1;
    public static final int COL_OFFLINE = 2;
    public static final int COL_URL = 3;
    public static final int COL_TITLE = 4;
    public static final int COL_TYPE = 5;
    public static final int COL_ACCESS_KEY = 6;
    public static final int COL_PREVIEW_URL = 7;
    public static final int COL_SIZE = 8;
    public static final int COL_DATE = 9;

    private static final String[] USER_COLUMNS = {
            DocsContract.UserEntry.TABLE_NAME + "." + DocsContract.UserEntry._ID,
            DocsContract.UserEntry.COLUMN_LAST
    };

    public static final String[] FILE_COLUMNS = {
            DocsContract.FileEntry.TABLE_NAME + "." + DocsContract.FileEntry._ID,
            DocsContract.FileEntry.COLUMN_NAME,
            DocsContract.FileEntry.COLUMN_LAST,
            DocsContract.FileEntry.COLUMN_OFFLINE
    };

    public static final int COL_FILE_ID = 0;
    public static final int COL_FILE_NAME = 1;
    public static final int COL_FILE_LAST = 2;
    public static final int COL_FILE_OFFLINE = 3;


    //Использовать при синхронизации
    public static ContentValues parseIntoValues(MyVKApiDocument doc) {
        ContentValues docsValues = new ContentValues();
        docsValues.put(DocsContract.DocumentEntry._ID, doc.id);
        docsValues.put(DocsContract.DocumentEntry.COLUMN_ACCESS_KEY, doc.access_key);
        docsValues.put(DocsContract.DocumentEntry.COLUMN_OWNER_ID, doc.owner_id);
        docsValues.put(DocsContract.DocumentEntry.COLUMN_SIZE, doc.size);
        docsValues.put(DocsContract.DocumentEntry.COLUMN_TITLE, doc.title);
        docsValues.put(DocsContract.DocumentEntry.COLUMN_TYPE, doc.getFileType());
        docsValues.put(DocsContract.DocumentEntry.COLUMN_URL, doc.url);
        docsValues.put(DocsContract.DocumentEntry.COLUMN_DATE, doc.getDate());
        if (!doc.photo_130.isEmpty()) {
            docsValues.put(DocsContract.DocumentEntry.COLUMN_PREVIEW_URL, doc.photo_130);
        } else if (!doc.photo_100.isEmpty()) {
            docsValues.put(DocsContract.DocumentEntry.COLUMN_PREVIEW_URL, doc.photo_100);
        }
        return docsValues;
    }
}
