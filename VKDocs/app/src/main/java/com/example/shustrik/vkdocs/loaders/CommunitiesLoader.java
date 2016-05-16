package com.example.shustrik.vkdocs.loaders;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;

import com.example.shustrik.vkdocs.MainActivity;
import com.example.shustrik.vkdocs.R;
import com.example.shustrik.vkdocs.adapters.LoadMore;
import com.example.shustrik.vkdocs.adapters.VKEntityListAdapter;
import com.example.shustrik.vkdocs.common.DBConverter;
import com.example.shustrik.vkdocs.data.DocsContract;
import com.example.shustrik.vkdocs.sync.VKDocsSyncAdapter;
import com.example.shustrik.vkdocs.vk.MyVKEntity;
import com.example.shustrik.vkdocs.vk.MyVKEntityImpl;
import com.example.shustrik.vkdocs.vk.VKRequestCallback;
import com.example.shustrik.vkdocs.vk.VKRequests;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.model.VKApiCommunity;
import com.vk.sdk.api.model.VKApiCommunityArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Load communities list
 */
public class CommunitiesLoader implements CustomLoader, LoadMore, LoaderManager.LoaderCallbacks<Cursor> {
    public static final int START_COUNT = 40;
    private static CommunitiesLoader instance;
    private final int GROUP_LOADER = 17;
    private VKEntityListAdapter adapter;
    private List<MyVKEntity> communities = new ArrayList<>();
    private int offset = 0;
    private int count = 20;
    private boolean isRefreshing = false;
    private long ownerId;
    private MainActivity activity;
    private SwipeRefreshLayout swipe;
    private LoaderManager loaderManager;
    private String query;
    private boolean isSearch = false;

    private CommunitiesLoader(MainActivity activity, VKEntityListAdapter adapter, SwipeRefreshLayout swipe,
                              LoaderManager loaderManager, long ownerId) {
        this.adapter = adapter;
        adapter.setLoadMore(this);
        this.swipe = swipe;
        swipe.setOnRefreshListener(this);
        this.loaderManager = loaderManager;
        this.ownerId = ownerId;
        this.activity = activity;
    }

    public static CommunitiesLoader getInstance() {
        return instance;
    }

    public static CommunitiesLoader initAndGetInstance(MainActivity activity,
                                                       VKEntityListAdapter adapter,
                                                       SwipeRefreshLayout swipe,
                                                       LoaderManager loaderManager,
                                                       long ownerId) {
        instance = new CommunitiesLoader(activity, adapter, swipe, loaderManager, ownerId);
        return instance;
    }


    @Override
    public void search(String query) {
        if (query == null || query.isEmpty()) {
            isSearch = false;
            this.query = "";
            initLoader();
        } else {
            isSearch = true;
            this.query = query;
            List<MyVKEntity> searchList = new ArrayList<>();
            for (MyVKEntity community : communities) {
                if (community.getPeerName().toLowerCase().contains(query.toLowerCase())) {
                    searchList.add(community);
                }
            }
            communities = searchList;
            adapter.swapData(communities);
        }
    }

    @Override
    public void cancelSearch() {
        isSearch = false;
        query = "";
        initLoader();
    }

    @Override
    public void onRefresh() {
        isSearch = false;
        isRefreshing = true;
        swipe.setRefreshing(true);
        VKDocsSyncAdapter.syncImmediately(activity);
    }

    private void onRefreshFailed() {
        updateAdapterState();
        adapter.onRefreshFailed();
    }

    @Override
    public void initLoader() {
        isSearch = false;
        offset = 0;
        if (!isRefreshing) {
            adapter.setLoading(true);
        }
        loaderManager.restartLoader(GROUP_LOADER, null, this);
    }

    @Override
    public void load() {
        loadCommunities();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri communitiesUri = DocsContract.CommunityEntry.buildCommunityUri(ownerId);
        if (id == GROUP_LOADER) {
            return new CursorLoader(activity,
                    communitiesUri,
                    DBConverter.GROUP_COLUMNS,
                    null,
                    null,
                    DocsContract.CommunityEntry.sortDateDesc());
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        updateAdapterState();
        while (cursor.moveToNext()) {
            communities.add(new MyVKEntityImpl(cursor, MyVKEntityImpl.SrcType.GROUP));
        }
        offset = communities.size();
        adapter.swapData(communities);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapData(null);
    }

    private void loadCommunities() {
        VKRequests.getCommunities(new VKRequestCallback<VKApiCommunityArray>() {
            @Override
            public void onSuccess(VKApiCommunityArray communityArray) {
                updateAdapterState();
                adapter.notifyLoadingComplete();
                List<MyVKEntity> newCommunities = new ArrayList<>();
                for (VKApiCommunity community : communityArray) {
                    newCommunities.add(new MyVKEntityImpl(community.getId(), community.name, community.photo_200));
                }

                if (!newCommunities.isEmpty()) {
                    if (!isSearch) {
                        communities.addAll(newCommunities);
                    } else {
                        for (MyVKEntity community : newCommunities) {
                            if (community.getPeerName().contains(query)) {
                                communities.add(community);
                            }
                        }
                    }
                    offset += newCommunities.size();
                    adapter.swapData(communities);
                }
                if (newCommunities.size() < count) {
                    adapter.notifyLoadFinished();
                }
            }

            @Override
            public void onError(VKError e) {
                activity.snack(activity.getString(R.string.server_error), Snackbar.LENGTH_SHORT);
                updateAdapterState();
                adapter.swapData(new ArrayList<MyVKEntity>());
                adapter.notifyLoadingComplete();
            }
        }, offset, count);
    }

    private void updateAdapterState() {
        if (isRefreshing) {
            swipe.setRefreshing(false);
            isRefreshing = false;
        } else if (offset == 0) {
            adapter.setLoading(false);
        }
    }

    public void onSyncUpdate(boolean isSuccess) {
        if (isSuccess) {
            loaderManager.restartLoader(GROUP_LOADER, null, this);
        } else {
            if (isRefreshing) {
                onRefreshFailed();
            }
        }
    }
}
