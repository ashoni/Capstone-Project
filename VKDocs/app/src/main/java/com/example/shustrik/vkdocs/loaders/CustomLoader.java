package com.example.shustrik.vkdocs.loaders;

import android.support.v4.widget.SwipeRefreshLayout;


/**
 * Common loader interface: initLoader loads all entities,
 * search loads only entities which match to the query,
 * cancelSearch switches from search to load everything mode
 */
public interface CustomLoader extends SwipeRefreshLayout.OnRefreshListener {
    void initLoader();

    void cancelSearch();

    void search(String query);
}
