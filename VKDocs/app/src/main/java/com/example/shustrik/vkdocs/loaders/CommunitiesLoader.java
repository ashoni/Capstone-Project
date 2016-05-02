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

public class CommunitiesLoader implements CustomLoader, LoadMore, LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = "ANNA_CL";
    public static final int START_COUNT = 40;
    private static CommunitiesLoader instance;
    private final int GROUP_LOADER = 17;
    private VKEntityListAdapter adapter;
    private List<MyVKEntity> communities = new ArrayList<>();
    private int offset = 0;
    private int count = 20;
    private boolean isRefreshing = false;
    private long ownerId;
    private Context context;
    private SwipeRefreshLayout swipe;
    private LoaderManager loaderManager;
    private String query;
    private boolean isSearch = false;

    private CommunitiesLoader(Context context, VKEntityListAdapter adapter, SwipeRefreshLayout swipe,
                              LoaderManager loaderManager, long ownerId) {
        this.adapter = adapter;
        adapter.setLoadMore(this);
        this.swipe = swipe;
        swipe.setOnRefreshListener(this);
        this.loaderManager = loaderManager;
        this.ownerId = ownerId;
        this.context = context;
    }

    public static CommunitiesLoader getInstance() {
        Log.w("ANNA", "Communities: get loader instance");
        return instance;
    }

    public static CommunitiesLoader initAndGetInstance(Context context, VKEntityListAdapter adapter, SwipeRefreshLayout swipe,
                                                       LoaderManager loaderManager, long ownerId) {
        Log.w("ANNA", "Communities: create loader instance " + ownerId);
        instance = new CommunitiesLoader(context, adapter, swipe, loaderManager, ownerId);
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
            Log.w("ANNA", "0 = " + communities.size());
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
        Log.w("ANNA", "Communities: refresh");
        isRefreshing = true;
        swipe.setRefreshing(true);
        VKDocsSyncAdapter.syncImmediately(context);
    }

    private void onRefreshFailed() {
        Log.w("ANNA", "Communities: refresh failed");
        updateAdapterState();
        adapter.onRefreshFailed();
    }

    @Override
    public void initLoader() {
        isSearch = false;
        Log.w("ANNA", "community loader : init");
        offset = 0;
        if (!isRefreshing) {
            Log.w("ANNA", "community loader : init without refresh, set adapter loading");
            adapter.setLoading(true);
        }
        loaderManager.restartLoader(GROUP_LOADER, null, this);
    }

    @Override
    public void load() {
        Log.w("ANNA", "load more: " + offset + " , " + count);
        loadCommunities();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri communitiesUri = DocsContract.CommunityEntry.buildCommunityUri(ownerId);
        Log.w("ANNA", communitiesUri.toString());
        if (id == GROUP_LOADER) {
            Log.w("ANNA", "Community loader: create");
            return new CursorLoader(context,
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
        Log.w("ANNA", "Community load: finished " + communities.size());
        offset = communities.size();
        adapter.swapData(communities);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapData(null);
    }

    private void loadCommunities() {
        Log.w("ANNA", "load communities: " + offset + " , " + count);
        VKRequests.getCommunities(new VKRequestCallback<VKApiCommunityArray>() {
            @Override
            public void onSuccess(VKApiCommunityArray communityArray) {
                updateAdapterState();
                Log.w("ANNA", "Communities load finished, " + communityArray.size());
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
                    Log.w("ANNA", "Communities: " + communities.size());
                    offset += newCommunities.size();
                    adapter.swapData(communities);
                }
                if (newCommunities.size() < count) {
                    adapter.notifyLoadFinished();
                }
            }

            @Override
            public void onError(VKError e) {
                //message that can't retrieve information
                updateAdapterState();
                adapter.notifyLoadingComplete();
            }
        }, offset, count);
    }

    private void updateAdapterState() {
        if (isRefreshing) {
            Log.w("ANNA", "Community after refresh update");
            swipe.setRefreshing(false);
            isRefreshing = false;
        } else if (offset == 0) {
            Log.w("ANNA", "Community after loading update");
            adapter.setLoading(false);
        }
    }

    public void onSyncUpdate(boolean isSuccess) {
        if (isSuccess) {
            Log.w("ANNA", "Community Sync update " + isSuccess);
            loaderManager.restartLoader(GROUP_LOADER, null, this);
        } else {
            if (isRefreshing) {
                onRefreshFailed();
            }
        }
    }
}
