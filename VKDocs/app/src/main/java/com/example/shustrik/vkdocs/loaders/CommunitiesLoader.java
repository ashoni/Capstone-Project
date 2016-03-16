package com.example.shustrik.vkdocs.loaders;


import android.support.v4.widget.SwipeRefreshLayout;

import com.example.shustrik.vkdocs.adapters.VKEntityListAdapter;
import com.example.shustrik.vkdocs.vk.MyVKEntity;
import com.example.shustrik.vkdocs.vk.MyVKEntityImpl;
import com.example.shustrik.vkdocs.vk.VKRequestCallback;
import com.example.shustrik.vkdocs.vk.VKRequests;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.model.VKApiCommunity;
import com.vk.sdk.api.model.VKApiCommunityArray;

import java.util.ArrayList;
import java.util.List;

public class CommunitiesLoader implements CustomLoader {
    public static final String TAG = "ANNA_CL";
    private VKEntityListAdapter adapter;

    private int offset = 0;
    private int count = 20;
    private boolean isRefreshing = false;

    private SwipeRefreshLayout swipe;


    public CommunitiesLoader(VKEntityListAdapter adapter, SwipeRefreshLayout swipe) {
        this.adapter = adapter;
        this.swipe = swipe;
        swipe.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        isRefreshing = true;
        swipe.setRefreshing(true);
        initLoader();
    }

    @Override
    public void initLoader() {
        offset = 0;
        if (!isRefreshing) {
            adapter.setLoading(true);
        }
        loadCommunities(offset, count);
    }

    private void loadCommunities(final int offset, int count) {
        VKRequests.getCommunities(new VKRequestCallback<VKApiCommunityArray>() {
            @Override
            public void onSuccess(VKApiCommunityArray communityArray) {
                updateAdapterState();
                List<MyVKEntity> communities = new ArrayList<>();
                for (VKApiCommunity community : communityArray) {
                    communities.add(new MyVKEntityImpl(community.getId(), community.name, community.photo_200));
                }
                adapter.swapData(communities);
            }

            @Override
            public void onError(VKError e) {
                //message that can't retrieve information
                updateAdapterState();
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
}
