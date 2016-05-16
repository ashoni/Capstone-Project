package com.example.shustrik.vkdocs.sync;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.example.shustrik.vkdocs.MainActivity;
import com.example.shustrik.vkdocs.R;
import com.example.shustrik.vkdocs.common.DBConverter;
import com.example.shustrik.vkdocs.data.DocsContract;
import com.example.shustrik.vkdocs.loaders.CommunitiesLoader;
import com.example.shustrik.vkdocs.loaders.DialogsLoader;
import com.example.shustrik.vkdocs.loaders.MyDocsLoader;
import com.example.shustrik.vkdocs.vk.MyVKApiDialog;
import com.example.shustrik.vkdocs.vk.MyVKApiDocument;
import com.example.shustrik.vkdocs.vk.MyVKDocsArray;
import com.example.shustrik.vkdocs.vk.VKRequestCallback;
import com.example.shustrik.vkdocs.vk.VKRequests;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.model.VKApiCommunity;
import com.vk.sdk.api.model.VKApiCommunityArray;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;


public class VKDocsSyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String ACTION_DOCS_UPDATED =
            "com.example.shustrik.vkdocs.app.ACTION_DOCS_UPDATED";
    public static final String ACTION_GROUPS_UPDATED =
            "com.example.shustrik.vkdocs.app.ACTION_GROUPS_UPDATED";
    public static final String ACTION_DIALOGS_UPDATED =
            "com.example.shustrik.vkdocs.app.ACTION_DIALOGS_UPDATED";
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    private ContentResolver contentResolver;
    private Context context;

    public VKDocsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.context = context;
        contentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        final SharedPreferences prefs = context.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE);

        VKRequests.getDocs(new VKRequestCallback<MyVKDocsArray>() {
            @Override
            public void onSuccess(MyVKDocsArray docsArray) {
                Vector<ContentValues> cVVector = new Vector<>(docsArray.size());

                Set<String> oldIds = prefs.getStringSet("ids", new HashSet<String>());
                Set<String> newIds = new HashSet<>();

                for (MyVKApiDocument doc : docsArray) {
                    String id = String.valueOf(doc.id);

                    newIds.add(id);
                    if (oldIds.contains(id)) {
                        updateDb(doc);
                    } else {
                        cVVector.add(DBConverter.parseIntoValues(doc));
                    }
                }
                insertToDb(cVVector, DocsContract.DocumentEntry.CONTENT_URI);
                oldIds.removeAll(newIds);
                for (String id : oldIds) {
                    deleteDocFromDB(id);
                }

                SharedPreferences.Editor e = prefs.edit();
                e.putStringSet("ids", newIds);
                e.commit();
                if (MyDocsLoader.getInstance() != null) {
                    MyDocsLoader.getInstance().onSyncUpdate(true);
                }
                updateWidgets();
            }

            @Override
            public void onError(VKError e) {
                if (MyDocsLoader.getInstance() != null) {
                    MyDocsLoader.getInstance().onSyncUpdate(false);
                }
            }
        });

        VKRequests.getCommunities(new VKRequestCallback<VKApiCommunityArray>() {
            @Override
            public void onSuccess(VKApiCommunityArray communityArray) {
                Set<String> oldIds = prefs.getStringSet("group_ids", new HashSet<String>());
                Set<String> newIds = new HashSet<>();

                Vector<ContentValues> cVVector = new Vector<>(communityArray.size());
                for (VKApiCommunity community : communityArray) {
                    newIds.add(String.valueOf(community.id));
                    if (!oldIds.contains(String.valueOf(community.id))) {
                        cVVector.add(DBConverter.parseIntoValues(community, prefs.getInt(MainActivity.USER_ID, 0)));
                    }
                }
                insertToDb(cVVector, DocsContract.CommunityEntry.CONTENT_URI);
                oldIds.removeAll(newIds);
                for (String id : oldIds) {
                    deleteGroupFromDB(id);
                }
                SharedPreferences.Editor e = prefs.edit();
                e.putStringSet("group_ids", newIds);
                e.commit();
                if (CommunitiesLoader.getInstance() != null) {
                    CommunitiesLoader.getInstance().onSyncUpdate(true);
                }
            }

            @Override
            public void onError(VKError e) {
                if (CommunitiesLoader.getInstance() != null) {
                    CommunitiesLoader.getInstance().onSyncUpdate(false);
                }
            }
        }, 0, CommunitiesLoader.START_COUNT);

        DialogsLoader.loadDialogs(0, DialogsLoader.START_COUNT, new DialogsLoader.Callback() {
            @Override
            public void process(List<MyVKApiDialog> dialogs, boolean isSuccess) {
                if (isSuccess) {
                    clearDialogsDB();
                    Vector<ContentValues> cVVector = new Vector<>(dialogs.size());
                    for (MyVKApiDialog dialog : dialogs) {
                        cVVector.add(DBConverter.parseIntoValues(dialog, prefs.getInt(MainActivity.USER_ID, 0)));
                    }
                    insertToDb(cVVector, DocsContract.DialogEntry.CONTENT_URI);
                }
                if (DialogsLoader.getInstance() != null) {
                    DialogsLoader.getInstance().onSyncUpdate(isSuccess);
                }
            }
        });
    }


    private void insertToDb(Vector<ContentValues> cVVector, Uri uri) {
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            contentResolver.bulkInsert(uri, cvArray);
        }
    }


    private void updateDb(MyVKApiDocument doc) {
        ContentValues cv = new ContentValues();
        cv.put(DocsContract.DocumentEntry.COLUMN_TITLE, doc.title);
        contentResolver.update(DocsContract.DocumentEntry.CONTENT_URI,
                cv, DocsContract.DocumentEntry._ID + "=" + doc.id, null);
    }


    private void deleteDocFromDB(String id) {
        contentResolver.delete(DocsContract.DocumentEntry.CONTENT_URI,
                DocsContract.DocumentEntry._ID + "=" + id, null);
    }

    private void deleteGroupFromDB(String id) {
        contentResolver.delete(DocsContract.CommunityEntry.CONTENT_URI,
                DocsContract.CommunityEntry._ID + "=" + id, null);
    }

    private void clearDialogsDB() {
        contentResolver.delete(DocsContract.DialogEntry.CONTENT_URI,
                null, null);
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static Account getSyncAccount(Context context) {
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        if (null == accountManager.getPassword(newAccount)) {

            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        VKDocsSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    private void updateWidgets() {
        Intent dataUpdatedIntent = new Intent(ACTION_DOCS_UPDATED).setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }
}
