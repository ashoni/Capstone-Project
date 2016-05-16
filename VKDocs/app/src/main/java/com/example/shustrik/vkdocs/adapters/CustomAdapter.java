package com.example.shustrik.vkdocs.adapters;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

//ToDo: Consider addinf setRefreshing to avoid early emptyView appearance

/**
 * Common interface for all adapters
 */
public interface CustomAdapter {
    void setEmptyView(View emptyView);

    void bindRecyclerView(RecyclerView recyclerView);

    void setLoadingView(View loadingView);

    void setLoading(boolean loading);

    void onSaveInstanceState(Bundle outState);

    void onRestoreInstanceState(Bundle savedInstanceState);

    void onRefreshFailed();
}
