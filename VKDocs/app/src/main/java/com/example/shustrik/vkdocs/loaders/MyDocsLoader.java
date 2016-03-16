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

import com.example.shustrik.vkdocs.adapters.CursorDocListAdapter;
import com.example.shustrik.vkdocs.common.DBConverter;
import com.example.shustrik.vkdocs.data.DocsContract;


public class MyDocsLoader implements CustomLoader, LoaderManager.LoaderCallbacks<Cursor> {
    private final int DOC_LOADER = 13;
    private Context context;
    private LoaderManager loaderManager;
    private CursorDocListAdapter adapter;
    private SwipeRefreshLayout swipe;
    private boolean isRefreshing = false;

    public MyDocsLoader(Context context, LoaderManager loaderManager,
                        CursorDocListAdapter adapter, SwipeRefreshLayout swipe) {
        this.context = context;
        this.loaderManager = loaderManager;
        this.adapter = adapter;
        this.swipe = swipe;
        swipe.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        isRefreshing = true;
        swipe.setRefreshing(true);
        loaderManager.restartLoader(DOC_LOADER, null, this);
    }

    @Override
    public void initLoader() {
        if (!isRefreshing) {
            Log.w("ANNA", "Set loading");
            adapter.setLoading(true);
        }
        loaderManager.initLoader(DOC_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Uri docConstUri = DocsContract.DocumentEntry.buildDocConstUri();
        if (i == DOC_LOADER) {
            return new CursorLoader(context,
                    docConstUri,
                    DBConverter.DOC_CONST_COLUMNS,
                    null,
                    null,
                    DocsContract.DocumentEntry.sortDateDesc());
        }
        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        updateAdapterState();
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
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
}
