package com.example.shustrik.vkdocs.loaders;


import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.example.shustrik.vkdocs.adapters.SpecDocListAdapter;
import com.example.shustrik.vkdocs.vk.MyVKDocsAttachments;
import com.example.shustrik.vkdocs.vk.VKRequestCallback;
import com.example.shustrik.vkdocs.vk.VKRequests;
import com.vk.sdk.api.VKError;

public class DialogDocsLoader implements CustomLoader, SpecDocListAdapter.LoadMore {
    public static String TAG = "ANNA_DDL";

    private String startFrom = "";
    private int count = 30;
    private int peerId;
    private boolean isRefreshing = false;
    private SwipeRefreshLayout swipe;

    private SpecDocListAdapter adapter;

    public DialogDocsLoader(SpecDocListAdapter adapter, int peerId, SwipeRefreshLayout swipe) {
        this.adapter = adapter;
        adapter.setLoadMore(this);
        this.peerId = peerId;
        this.swipe = swipe;
        swipe.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        isRefreshing = true;
        swipe.setRefreshing(true);
        adapter.swapData(null);
        initLoader();
    }

    @Override
    public void initLoader() {
        if (!isRefreshing) {
            adapter.setLoading(true);
        }
        startFrom = "";
        loadDialogDocs();
    }

    @Override
    public void load() {
        loadDialogDocs();
    }

    private void loadDialogDocs() {
        Log.w("ANNA!", "Loading from " + startFrom);
        VKRequests.getAttachments(new VKRequestCallback<MyVKDocsAttachments>() {
            @Override
            public void onSuccess(MyVKDocsAttachments documents) {
                updateAdapterState();
                Log.w("ANNA!", documents.toString());
                if (startFrom.isEmpty()) {
                    adapter.swapData(documents.getDocuments());
                    startFrom = documents.isNext() ? documents.getNext() : "";
                } else {
                    adapter.addData(documents.getDocuments());
                    startFrom = documents.isNext() ? documents.getNext() : "";
                }
                if (!documents.isNext()) {
                    adapter.notifyLoadFinished();
                }
                adapter.notifyLoadingComplete();
            }

            @Override
            public void onError(VKError e) {
                updateAdapterState();
                Log.w(TAG, e.errorMessage);
                adapter.notifyLoadingComplete();
            }
        }, peerId, startFrom, count);
    }

    private void updateAdapterState() {
        if (isRefreshing) {
            swipe.setRefreshing(false);
            isRefreshing = false;
        } else if (startFrom.isEmpty()) {
            adapter.setLoading(false);
        }
    }
}
