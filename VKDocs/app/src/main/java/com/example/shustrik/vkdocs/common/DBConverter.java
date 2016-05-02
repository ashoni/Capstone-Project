package com.example.shustrik.vkdocs.common;

import android.content.ContentValues;

import com.example.shustrik.vkdocs.data.DocsContract;
import com.example.shustrik.vkdocs.vk.MyVKApiDialog;
import com.example.shustrik.vkdocs.vk.MyVKApiDocument;
import com.vk.sdk.api.model.VKApiCommunity;

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

    public static final String[] GROUP_COLUMNS = {
            DocsContract.CommunityEntry.TABLE_NAME + "." + DocsContract.CommunityEntry._ID,
            DocsContract.CommunityEntry.COLUMN_TITLE,
            DocsContract.CommunityEntry.COLUMN_PREVIEW_URL
    };

    public static final int COL_GROUP_ID = 0;
    public static final int COL_GROUP_TITLE = 1;
    public static final int COL_GROUP_PREVIEW = 2;

    public static final String[] DIALOG_COLUMNS = {
            DocsContract.DialogEntry.COLUMN_PEER_ID,
            DocsContract.DialogEntry.COLUMN_TITLE,
            DocsContract.DialogEntry.COLUMN_PREVIEW_URL
    };

    public static final int COL_DIALOG_PEER_ID = 0;
    public static final int COL_DIALOG_TITLE = 1;
    public static final int COL_DIALOG_PREVIEW = 2;

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

    public static ContentValues parseIntoValues(VKApiCommunity community, int ownerId) {
        ContentValues communityValues = new ContentValues();
        communityValues.put(DocsContract.CommunityEntry._ID, community.id);
        communityValues.put(DocsContract.CommunityEntry.COLUMN_DATE, 0);
        communityValues.put(DocsContract.CommunityEntry.COLUMN_TITLE, community.name);
        communityValues.put(DocsContract.CommunityEntry.COLUMN_OWNER_ID, ownerId);
        communityValues.put(DocsContract.CommunityEntry.COLUMN_PREVIEW_URL, community.photo_200);
        return communityValues;
    }

    public static ContentValues parseIntoValues(MyVKApiDialog dialog, int ownerId) {
        ContentValues communityValues = new ContentValues();
        communityValues.put(DocsContract.DialogEntry._ID, dialog.getId());
        communityValues.put(DocsContract.DialogEntry.COLUMN_DATE, dialog.message.date);
        communityValues.put(DocsContract.DialogEntry.COLUMN_OWNER_ID, ownerId);
        communityValues.put(DocsContract.DialogEntry.COLUMN_TITLE, dialog.getPeerName());
        communityValues.put(DocsContract.DialogEntry.COLUMN_PREVIEW_URL, dialog.getPreviewUrl());
        communityValues.put(DocsContract.DialogEntry.COLUMN_PEOPLE, dialog.getPeerIds().toString());
        communityValues.put(DocsContract.DialogEntry.COLUMN_PEER_ID, dialog.getPeerId());
        return communityValues;
    }
}
