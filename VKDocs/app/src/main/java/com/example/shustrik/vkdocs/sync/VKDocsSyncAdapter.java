package com.example.shustrik.vkdocs.sync;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

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
    // Interval at which to sync with the weather, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
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
        Log.d("ANNA", "----------------------------------------------------------Starting sync");

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
            }

            @Override
            public void onError(VKError e) {
                Log.w("ANNA", "Load mydocs error: " + e);

                if (MyDocsLoader.getInstance() != null) {
                    MyDocsLoader.getInstance().onSyncUpdate(false);
                }
            }
        });

        VKRequests.getCommunities(new VKRequestCallback<VKApiCommunityArray>() {
            @Override
            public void onSuccess(VKApiCommunityArray communityArray) {
                Log.w("ANNA", "sync :: comm :: got :: " + communityArray.size());
                Set<String> oldIds = prefs.getStringSet("group_ids", new HashSet<String>());
                Set<String> newIds = new HashSet<>();

                Vector<ContentValues> cVVector = new Vector<>(communityArray.size());
                for (VKApiCommunity community : communityArray) {
                    newIds.add(String.valueOf(community.id));
                    if (!oldIds.contains(String.valueOf(community.id))) {
                        cVVector.add(DBConverter.parseIntoValues(community, prefs.getInt(MainActivity.USER_ID, 0)));
                    }
                }
                Log.w("ANNA", "sync :: comm :: new ::" + newIds.size() + " old :: " + oldIds.size());
                insertToDb(cVVector, DocsContract.CommunityEntry.CONTENT_URI);
                oldIds.removeAll(newIds);
                Log.w("ANNA", "sync :: old: " + oldIds.size());
                for (String id : oldIds) {
                    deleteGroupFromDB(id);
                }
                SharedPreferences.Editor e = prefs.edit();
                e.putStringSet("group_ids", newIds);
                Log.w("ANNA", "sync group success: " + newIds.size());
                e.commit();
                if (CommunitiesLoader.getInstance() != null) {
                    CommunitiesLoader.getInstance().onSyncUpdate(true);
                }
            }

            @Override
            public void onError(VKError e) {
                Log.w("ANNA", "Sync group error");
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
                Log.w("ANNA", "Dialogs sync: " + isSuccess);
                if (DialogsLoader.getInstance() != null) {
                    DialogsLoader.getInstance().onSyncUpdate(isSuccess);
                }
            }
        });
    }


//    private void sendBroadcast(String name, boolean isSuccess) {
//        Intent dataUpdatedIntent = new Intent(name).setPackage(context.getPackageName());
//        dataUpdatedIntent.putExtra("result", isSuccess ? "success" : "fail");
//        context.sendBroadcast(dataUpdatedIntent);
//    }


    private void insertToDb(Vector<ContentValues> cVVector, Uri uri) {
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            contentResolver.bulkInsert(uri, cvArray);
            //notify anybody
        }
    }


    /**
     * All or title only?
     */
    private void updateDb(MyVKApiDocument doc) {
        ContentValues cv = new ContentValues();
        cv.put(DocsContract.DocumentEntry.COLUMN_TITLE, doc.title);
        contentResolver.update(DocsContract.DocumentEntry.CONTENT_URI,
                cv, DocsContract.DocumentEntry._ID + "=" + doc.id, null);
        //notify anybody
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

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
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

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        VKDocsSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
