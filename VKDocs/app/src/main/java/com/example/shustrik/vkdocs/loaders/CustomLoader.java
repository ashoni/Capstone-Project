package com.example.shustrik.vkdocs.loaders;

import android.support.v4.widget.SwipeRefreshLayout;

public interface CustomLoader extends SwipeRefreshLayout.OnRefreshListener {
    void initLoader();
}
