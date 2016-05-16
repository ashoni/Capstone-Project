package com.example.shustrik.vkdocs.loaders;

import android.support.v4.widget.SwipeRefreshLayout;

import com.example.shustrik.vkdocs.adapters.DocListAdapter;
import com.example.shustrik.vkdocs.adapters.LoadMore;
import com.example.shustrik.vkdocs.vk.MyVKApiDocument;
import com.example.shustrik.vkdocs.vk.MyVKDocsArray;
import com.example.shustrik.vkdocs.vk.VKRequestCallback;
import com.example.shustrik.vkdocs.vk.VKRequests;
import com.vk.sdk.api.VKError;

import java.util.ArrayList;

/**
 * Implements global search in VK library
 */
public class GlobalLoader implements CustomLoader, LoadMore {
    private int count = 30;
    private boolean isRefreshing = false;
    private SwipeRefreshLayout swipe;
    private int offset = 0;

    private DocListAdapter adapter;

    private String query;

    public GlobalLoader(DocListAdapter adapter, SwipeRefreshLayout swipe) {
        this.adapter = adapter;
        adapter.setLoadMore(this);
        this.swipe = swipe;
        swipe.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        isRefreshing = true;
        swipe.setRefreshing(true);
        adapter.swapData(null);
        search(query);
    }

    @Override
    public void initLoader() {
        if (query == null || query.isEmpty()) {
            adapter.swapData(new ArrayList<MyVKApiDocument>());
            updateAdapterState();
            adapter.notifyLoadingComplete();
            adapter.notifyLoadFinished();
        } else {
            loadDocs();
        }
    }

    @Override
    public void cancelSearch() {
        query = null;
        initLoader();
    }

    @Override
    public void load() {
        loadDocs();
    }

    private void loadDocs() {
        VKRequests.globalSearch(new VKRequestCallback<MyVKDocsArray>() {
            @Override
            public void onSuccess(MyVKDocsArray documents) {
                updateAdapterState();
                if (offset == 0) {
                    adapter.swapData(documents);
                    offset = documents.size();
                } else {
                    adapter.addData(documents);
                    offset += documents.size();
                }
                if (documents.size() < count) {
                    adapter.notifyLoadFinished();
                }
                adapter.notifyLoadingComplete();
            }

            @Override
            public void onError(VKError e) {

            }
        }, query, offset, count);
    }


    private void updateAdapterState() {
        if (isRefreshing) {
            swipe.setRefreshing(false);
            isRefreshing = false;
        } else if (offset == 0) {
            adapter.setLoading(false);
        }
    }


    @Override
    public void search(String query) {
        this.query = query;
        initLoader();
    }
}
