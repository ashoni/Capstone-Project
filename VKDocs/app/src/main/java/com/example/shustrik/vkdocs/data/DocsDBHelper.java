package com.example.shustrik.vkdocs.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.shustrik.vkdocs.data.DocsContract.DocumentEntry;
import com.example.shustrik.vkdocs.data.DocsContract.UserEntry;
import com.example.shustrik.vkdocs.data.DocsContract.FileEntry;

public class DocsDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "vkdocs.db";

    public DocsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a table to hold locations.  A location consists of the string supplied in the
        // location setting, the city name, and the latitude and longitude
        final String SQL_CREATE_DOCUMENT_TABLE = "CREATE TABLE " + DocumentEntry.TABLE_NAME + " (" +
                DocumentEntry._ID + " INTEGER PRIMARY KEY, " +
                DocumentEntry.COLUMN_ACCESS_KEY + " TEXT, " +
                DocumentEntry.COLUMN_OWNER_ID + " INTEGER NOT NULL, " +
                DocumentEntry.COLUMN_PREVIEW_URL + " TEXT, " +
                DocumentEntry.COLUMN_SIZE + " INTEGER, " + // NOT NULL
                DocumentEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                DocumentEntry.COLUMN_URL + " TEXT NOT NULL, " +
                DocumentEntry.COLUMN_TYPE + " INTEGER NOT NULL, " + // NOT NULL
                DocumentEntry.COLUMN_DATE + " INTEGER NOT NULL, " + // NOT NULL
                " FOREIGN KEY (" + DocumentEntry.COLUMN_OWNER_ID + ") REFERENCES " +
                UserEntry.TABLE_NAME + " (" + UserEntry._ID + ") " +
                " );";

        final String SQL_CREATE_FILE_TABLE = "CREATE TABLE " + FileEntry.TABLE_NAME + " (" +
                FileEntry._ID + " INTEGER PRIMARY KEY," +
                FileEntry.COLUMN_NAME + " TEXT UNIQUE NOT NULL," +
                FileEntry.COLUMN_LAST + " INTEGER NOT NULL, " +
                FileEntry.COLUMN_OFFLINE + " TINYINT(1) NOT NULL, " +
                " FOREIGN KEY (" + DocumentEntry._ID + ") REFERENCES " +
                FileEntry.TABLE_NAME + " (" + FileEntry._ID + ") " +
                " );";

        final String SQL_CREATE_USER_TABLE = "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
                UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                UserEntry.COLUMN_LAST + " INTEGER NOT NULL " +
                " );";

        db.execSQL(SQL_CREATE_USER_TABLE);
        db.execSQL(SQL_CREATE_FILE_TABLE);
        db.execSQL(SQL_CREATE_DOCUMENT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DocumentEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FileEntry.TABLE_NAME);
        onCreate(db);
    }
}
