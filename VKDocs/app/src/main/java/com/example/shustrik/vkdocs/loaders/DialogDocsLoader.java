package com.example.shustrik.vkdocs.loaders;


import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.example.shustrik.vkdocs.adapters.DocListAdapter;
import com.example.shustrik.vkdocs.adapters.LoadMore;
import com.example.shustrik.vkdocs.vk.MyVKApiDocument;
import com.example.shustrik.vkdocs.vk.MyVKDocsAttachments;
import com.example.shustrik.vkdocs.vk.VKRequestCallback;
import com.example.shustrik.vkdocs.vk.VKRequests;
import com.vk.sdk.api.VKError;

import java.util.ArrayList;
import java.util.List;

public class DialogDocsLoader implements CustomLoader, LoadMore {
    public static String TAG = "ANNA_DDL";

    private String startFrom = "";
    private int count = 30;
    private int peerId;
    private boolean isRefreshing = false;
    private SwipeRefreshLayout swipe;

    private DocListAdapter adapter;

    private String query;
    private boolean isSearch = false;

    public DialogDocsLoader(DocListAdapter adapter, int peerId, SwipeRefreshLayout swipe) {
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
    public void cancelSearch() {
        isSearch = false;
        query = "";
        initLoader();
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
                Log.w("ANNA!", documents.getDocuments().toString());
                List<MyVKApiDocument> goodDocs = new ArrayList<>();
                if (isSearch && query != null) {
                    Log.w("ANNA", "Difficult! " + query);
                    for (MyVKApiDocument doc : documents.getDocuments()) {
                        if (doc.title.toLowerCase().contains(query.toLowerCase())) {
                            goodDocs.add(doc);
                        }
                    }
                } else {
                    goodDocs.addAll(documents.getDocuments());
                }
                if (startFrom.isEmpty()) {
                    adapter.swapData(goodDocs);
                    startFrom = documents.isNext() ? documents.getNext() : "";
                } else {
                    adapter.addData(goodDocs);
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


    @Override
    public void search(String query) {
        if (query == null || query.isEmpty()) {
            isSearch = false;
            this.query = "";
            initLoader();
        } else {
            isSearch = true;
            this.query = query;
            Log.w("ANNA", "ddsearch");
            initLoader();
        }
    }
}
