package com.example.shustrik.vkdocs.data;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class DocsContract {
    public static final String CONTENT_AUTHORITY = "com.example.shustrik.vkdocs";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_DOCS = "docs";
    public static final String PATH_USERS = "users";
    public static final String PATH_FILES = "files";
    public static final String PATH_GROUPS = "groups";
    public static final String PATH_DIALOGS = "dialogs";

    public static final class DocumentEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DOCS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DOCS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DOCS;

        public static final String TABLE_NAME = "docs";

        public static final String COLUMN_OWNER_ID = "owner_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_SIZE = "size";
        public static final String COLUMN_TYPE = "doc_type";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_PREVIEW_URL = "preview_url";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_ACCESS_KEY = "access_key";


        public static Uri buildDocUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildDocConstUri() {
            return CONTENT_URI.buildUpon().appendPath("const").build();
        }

        public static String sortDateDesc() {
            return "date DESC";
        }

        public static String getIdFromUri(Uri uri) {
            return uri.getLastPathSegment();
        }
    }

    public static final class UserEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_USERS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USERS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_USERS;

        public static final String TABLE_NAME = "users";

        public static final String COLUMN_LAST = "date";

        public static Uri buildUserUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class FileEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FILES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FILES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FILES;

        public static final String TABLE_NAME = "files";

        public static final String COLUMN_LAST = "access_date";
        public static final String COLUMN_NAME = "file_name";
        public static final String COLUMN_OFFLINE = "offline_available";

        public static Uri buildFileUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getIdFromUri(Uri uri) {
            return uri.getLastPathSegment();
        }
    }

    public static final class DialogEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DIALOGS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DIALOGS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DIALOGS;

        // Table name
        public static final String TABLE_NAME = "dialogs";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_PEOPLE = "people";
        public static final String COLUMN_TYPE = "dialog_type";
        public static final String COLUMN_PREVIEW_URL = "preview_url";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_OWNER_ID = "owner_id";
        public static final String COLUMN_PEER_ID = "peer_id";


        public static Uri buildDialogsUri(long ownerId) {
            return ContentUris.withAppendedId(CONTENT_URI, ownerId);
        }

        public static String getOwnerIdFromUri(Uri uri) {
            return uri.getLastPathSegment();
        }

        public static String sortDateDesc() {
            return "date DESC";
        }
    }

    public static final class CommunityEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_GROUPS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GROUPS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GROUPS;

        public static final String TABLE_NAME = "groups";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_PREVIEW_URL = "preview_url";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_OWNER_ID = "owner_id";

        public static Uri buildCommunityUri(long ownerId) {
            return ContentUris.withAppendedId(CONTENT_URI, ownerId);
        }

        public static String getOwnerIdFromUri(Uri uri) {
            return uri.getLastPathSegment();
        }

        public static String sortDateDesc() {
            return "date DESC";
        }
    }

}

