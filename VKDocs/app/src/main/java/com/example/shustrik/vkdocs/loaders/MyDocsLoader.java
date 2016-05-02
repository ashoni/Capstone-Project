package com.example.shustrik.vkdocs.loaders;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.example.shustrik.vkdocs.adapters.DocListAdapter;
import com.example.shustrik.vkdocs.common.DBConverter;
import com.example.shustrik.vkdocs.data.DocsContract;
import com.example.shustrik.vkdocs.sync.VKDocsSyncAdapter;
import com.example.shustrik.vkdocs.vk.MyVKApiDocument;

import java.util.ArrayList;
import java.util.List;


public class MyDocsLoader implements CustomLoader, LoaderManager.LoaderCallbacks<Cursor> {
    private final int DOC_LOADER = 13;
    private final int SEARCH_LOADER = 11;
    private Context context;
    private LoaderManager loaderManager;
    private DocListAdapter adapter;
    private SwipeRefreshLayout swipe;
    private boolean isRefreshing = false;
    private static MyDocsLoader instance;
    private String query;

    public static MyDocsLoader getInstance() {
        return instance;
    }

    public static MyDocsLoader initAndGetInstance(Context context, DocListAdapter adapter,
                                                  SwipeRefreshLayout swipe,
                                                  LoaderManager loaderManager) {
        instance = new MyDocsLoader(context, loaderManager, adapter, swipe);
        return instance;
    }

    private MyDocsLoader(Context context, LoaderManager loaderManager,
                         DocListAdapter adapter, SwipeRefreshLayout swipe) {
        this.context = context;
        this.loaderManager = loaderManager;
        this.adapter = adapter;
        this.swipe = swipe;
        swipe.setOnRefreshListener(this);
    }

    @Override
    public void search(String query) {
        Log.w("ANNA", "Search loader");
        this.query = query;
        adapter.setLoading(true);
        loaderManager.restartLoader(SEARCH_LOADER, null, this);
    }

    @Override
    public void onRefresh() {
        isRefreshing = true;
        swipe.setRefreshing(true);
        VKDocsSyncAdapter.syncImmediately(context);
    }

    private void onRefreshFailed() {
        updateAdapterState();
        adapter.onRefreshFailed();
    }

    @Override
    public void initLoader() {
        Log.w("ANNA", "Init loader");
        if (!isRefreshing) {
            Log.w("ANNA", "Set loading");
            adapter.setLoading(true);
        }
        loaderManager.restartLoader(DOC_LOADER, null, this);
        //loaderManager.initLoader(DOC_LOADER, null, this);
    }

    @Override
    public void cancelSearch() {
        initLoader();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Uri docConstUri = DocsContract.DocumentEntry.buildDocConstUri();
        Log.w("ANNA", "on create loader");
        if (i == DOC_LOADER) {
            return new CursorLoader(context,
                    docConstUri,
                    DBConverter.DOC_CONST_COLUMNS,
                    null,
                    null,
                    DocsContract.DocumentEntry.sortDateDesc());
        } else if (i == SEARCH_LOADER) {
            String where = (query == null || query.isEmpty()) ? null :
                    DocsContract.DocumentEntry.COLUMN_TITLE + " LIKE '%" + query + "%'";

            Log.w("ANNA", "Where: " + where);
            return new CursorLoader(context,
                    docConstUri,
                    DBConverter.DOC_CONST_COLUMNS,
                    where,
                    null,
                    DocsContract.DocumentEntry.sortDateDesc());
        }
        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        updateAdapterState();
        List<MyVKApiDocument> documents = new ArrayList<>();
        while (cursor.moveToNext()) {
            documents.add(new MyVKApiDocument(cursor));
        }
        Log.w("ANNA", "finish: " + documents.size());
        adapter.swapData(documents);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapData(null);
    }

    private void updateAdapterState() {
        if (isRefreshing) {
            swipe.setRefreshing(false);
            isRefreshing = false;
        } else {
            Log.w("ANNA", "Stop loading");
            adapter.setLoading(false);
        }
    }

    public void onSyncUpdate(boolean isSuccess) {
        if (isSuccess) {
            loaderManager.restartLoader(DOC_LOADER, null, this);
        } else {
            if (isRefreshing) {
                onRefreshFailed();
            }
        }
    }
}
