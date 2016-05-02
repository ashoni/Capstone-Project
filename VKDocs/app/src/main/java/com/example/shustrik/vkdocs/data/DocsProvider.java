package com.example.shustrik.vkdocs.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

public class DocsProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DocsDBHelper mOpenHelper;

    static final int DOCS = 100;
    static final int DOCS_WITH_ID = 101;
    static final int DOCS_WITH_CONST = 102;
    static final int USERS = 200;
    static final int FILES = 300;
    static final int FILES_WITH_ID = 301;
    static final int GROUPS_WITH_USER = 400;
    static final int GROUPS = 401;
    static final int DIALOGS_WITH_USER = 500;
    static final int DIALOGS = 501;


    private static final String sDocIdSelection =
            DocsContract.DocumentEntry.TABLE_NAME+
                    "." + DocsContract.DocumentEntry._ID + " = ? ";

    private static final String sFileIdSelection =
            DocsContract.DocumentEntry.TABLE_NAME+
                    "." + DocsContract.FileEntry._ID + " = ? ";

    private static final String dialogOwnerSelection =
            DocsContract.DialogEntry.TABLE_NAME+
                    "." + DocsContract.DialogEntry.COLUMN_OWNER_ID + " = ? ";

    private static final String groupOwnerSelection =
            DocsContract.CommunityEntry.TABLE_NAME+
                    "." + DocsContract.CommunityEntry.COLUMN_OWNER_ID + " = ? ";


    private static final SQLiteQueryBuilder docQueryBuilder;
    private static final SQLiteQueryBuilder fileQueryBuilder;
    private static final SQLiteQueryBuilder docConstQueryBuilder;
    private static final SQLiteQueryBuilder groupsQueryBuilder;
    private static final SQLiteQueryBuilder dialogsQueryBuilder;

    static {
        docQueryBuilder = new SQLiteQueryBuilder();
        docQueryBuilder.setTables(DocsContract.DocumentEntry.TABLE_NAME);

        fileQueryBuilder = new SQLiteQueryBuilder();
        fileQueryBuilder.setTables(DocsContract.FileEntry.TABLE_NAME);

        docConstQueryBuilder = new SQLiteQueryBuilder();
        docConstQueryBuilder.setTables(
                DocsContract.DocumentEntry.TABLE_NAME + " LEFT JOIN " +
                        DocsContract.FileEntry.TABLE_NAME +
                        " ON " + DocsContract.DocumentEntry.TABLE_NAME +
                        "." + DocsContract.DocumentEntry._ID +
                        " = " + DocsContract.FileEntry.TABLE_NAME +
                        "." + DocsContract.FileEntry._ID);

        groupsQueryBuilder = new SQLiteQueryBuilder();
        groupsQueryBuilder.setTables(DocsContract.CommunityEntry.TABLE_NAME);

        dialogsQueryBuilder = new SQLiteQueryBuilder();
        dialogsQueryBuilder.setTables(DocsContract.DialogEntry.TABLE_NAME);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DocsDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case DOCS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DocsContract.DocumentEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case DOCS_WITH_ID: {
                retCursor = docQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        sDocIdSelection,
                        new String[]{DocsContract.DocumentEntry.getIdFromUri(uri)},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case DOCS_WITH_CONST: {
                retCursor = docConstQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case USERS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DocsContract.UserEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case FILES: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        DocsContract.FileEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case FILES_WITH_ID: {
                retCursor = fileQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        sFileIdSelection,
                        new String[]{DocsContract.FileEntry.getIdFromUri(uri)},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case DIALOGS_WITH_USER: {
                retCursor = dialogsQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        dialogOwnerSelection,
                        new String[]{DocsContract.DialogEntry.getOwnerIdFromUri(uri)},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case GROUPS_WITH_USER: {
                retCursor = groupsQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        groupOwnerSelection,
                        new String[]{DocsContract.CommunityEntry.getOwnerIdFromUri(uri)},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case DOCS:
                return DocsContract.DocumentEntry.CONTENT_TYPE;
            case USERS:
                return DocsContract.UserEntry.CONTENT_TYPE;
            case DOCS_WITH_ID:
                return DocsContract.DocumentEntry.CONTENT_ITEM_TYPE;
            case DOCS_WITH_CONST:
                return DocsContract.DocumentEntry.CONTENT_TYPE;
            case FILES:
                return DocsContract.FileEntry.CONTENT_TYPE;
            case FILES_WITH_ID:
                return DocsContract.FileEntry.CONTENT_ITEM_TYPE;
            case GROUPS_WITH_USER:
                return DocsContract.CommunityEntry.CONTENT_TYPE;
            case GROUPS:
                return DocsContract.CommunityEntry.CONTENT_TYPE;
            case DIALOGS_WITH_USER:
                return DocsContract.DialogEntry.COLUMN_TYPE;
            case DIALOGS:
                return DocsContract.DialogEntry.COLUMN_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case DOCS: {
                long _id = db.insert(DocsContract.DocumentEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = DocsContract.DocumentEntry.buildDocUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case USERS: {
                long _id = db.insert(DocsContract.UserEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = DocsContract.UserEntry.buildUserUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case FILES: {
                long _id = db.insert(DocsContract.FileEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = DocsContract.FileEntry.buildFileUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case GROUPS: {
                long _id = db.insert(DocsContract.CommunityEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = DocsContract.CommunityEntry.buildCommunityUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case DIALOGS: {
                long _id = db.insert(DocsContract.DialogEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = DocsContract.DialogEntry.buildDialogsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case DOCS:
                rowsDeleted = db.delete(
                        DocsContract.DocumentEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case USERS:
                rowsDeleted = db.delete(
                        DocsContract.UserEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case FILES:
                rowsDeleted = db.delete(
                        DocsContract.FileEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case DIALOGS:
                rowsDeleted = db.delete(
                        DocsContract.DialogEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case GROUPS:
                rowsDeleted = db.delete(
                        DocsContract.CommunityEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case DOCS:
                rowsUpdated = db.update(DocsContract.DocumentEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case USERS:
                rowsUpdated = db.update(DocsContract.UserEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case FILES:
                rowsUpdated = db.update(DocsContract.FileEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case DIALOGS:
                rowsUpdated = db.update(DocsContract.DialogEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case GROUPS:
                rowsUpdated = db.update(DocsContract.CommunityEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }



    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        Log.w("ANNA", "Bulk insert");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount;
        switch (match) {
            case DOCS:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DocsContract.DocumentEntry.TABLE_NAME, null, value);
                        Log.w("ANNA", "ID+" + _id);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case GROUPS:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DocsContract.CommunityEntry.TABLE_NAME, null, value);
                        Log.w("ANNA", "ID+" + _id);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case DIALOGS:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(DocsContract.DialogEntry.TABLE_NAME, null, value);
                        Log.w("ANNA", "ID+" + _id);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }


    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DocsContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, DocsContract.PATH_DOCS, DOCS);
        matcher.addURI(authority, DocsContract.PATH_DOCS + "/#", DOCS_WITH_ID);
        matcher.addURI(authority, DocsContract.PATH_USERS, USERS);
        matcher.addURI(authority, DocsContract.PATH_FILES + "/#", FILES_WITH_ID);
        matcher.addURI(authority, DocsContract.PATH_FILES, FILES);
        matcher.addURI(authority, DocsContract.PATH_DOCS + "/*", DOCS_WITH_CONST);
        matcher.addURI(authority, DocsContract.PATH_GROUPS + "/#", GROUPS_WITH_USER);
        matcher.addURI(authority, DocsContract.PATH_GROUPS, GROUPS);
        matcher.addURI(authority, DocsContract.PATH_DIALOGS + "/#", DIALOGS_WITH_USER);
        matcher.addURI(authority, DocsContract.PATH_DIALOGS, DIALOGS);
        return matcher;
    }
}
