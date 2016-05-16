package com.example.shustrik.vkdocs.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.shustrik.vkdocs.MainActivity;
import com.example.shustrik.vkdocs.R;
import com.example.shustrik.vkdocs.SelectCallback;
import com.example.shustrik.vkdocs.adapters.CustomAdapter;
import com.example.shustrik.vkdocs.loaders.CustomLoader;
import com.example.shustrik.vkdocs.uicommon.DividerItemDecoration;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Стрелка назад не работает, починить
 */
public class MainActivityFragment extends Fragment {
    private static final String EMPTY_TEXT = "empty_text";
    private static final String LOADING_TEXT = "loading_text";
    private static final String HOME_UP = "home_up";
    private static final String FRAGMENT_TYPE = "fragment_type";
    private static final String TITLE = "title";

    @Bind(R.id.recyclerview)
    RecyclerView recyclerView;
    @Bind(R.id.recyclerview_docs_empty)
    TextView emptyView;
    @Bind(R.id.loading_display)
    LinearLayout loadingLayout;

    private CustomAdapter adapter;
    private CustomLoader loader;
    private boolean isHomeAsUpEnabled;
    private String emptyText;
    private String loadText;
    private int type;
    private String title;

    public MainActivityFragment() {
        type = -1;
    }

    public static MainActivityFragment getInstance(int t,
                                                   boolean isHomeAsUpEnabled,
                                                   String emptyText,
                                                   String loadText,
                                                   String title) {
        MainActivityFragment fragment = new MainActivityFragment();
        fragment.type = t;
        fragment.isHomeAsUpEnabled = isHomeAsUpEnabled;
        fragment.emptyText = emptyText;
        fragment.loadText = loadText;
        fragment.title = title;
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);

        if (type == -1 && savedInstanceState != null) {
            emptyText = savedInstanceState.getString(EMPTY_TEXT);
            loadText = savedInstanceState.getString(LOADING_TEXT);
            isHomeAsUpEnabled = savedInstanceState.getBoolean(HOME_UP);
            type = savedInstanceState.getInt(FRAGMENT_TYPE);
            title = savedInstanceState.getString(TITLE);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        ((MainActivity) getActivity()).setTitle(title);

        if (!isHomeAsUpEnabled) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            ((SelectCallback) getActivity()).setToggleListener(true);
        } else {
            ((SelectCallback) getActivity()).setToggleListener(false);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            ((MainActivity)getActivity()).getToggle().setToolbarNavigationClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });
        }

        emptyView.setText(emptyText);
        ((TextView) (loadingLayout.findViewById(R.id.loading_text))).setText(loadText);

        Pair<CustomAdapter, CustomLoader> pair = ((MainActivity) getActivity()).createAdapterAndLoader(type);
        adapter = pair.first;
        loader = pair.second;

        adapter.setEmptyView(emptyView);
        adapter.bindRecyclerView(recyclerView);
        adapter.setLoadingView(loadingLayout);

        if (savedInstanceState != null) {
            adapter.onRestoreInstanceState(savedInstanceState);
        }

        return rootView;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (adapter != null) {
            adapter.onSaveInstanceState(outState);
        }
        outState.putString(EMPTY_TEXT, emptyText);
        outState.putString(LOADING_TEXT, loadText);
        outState.putBoolean(HOME_UP, isHomeAsUpEnabled);
        outState.putInt(FRAGMENT_TYPE, type);
        outState.putString(TITLE, title);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        loader.initLoader();
        super.onActivityCreated(savedInstanceState);
    }

    public void search(String query) {
        loader.search(query);
    }

    public void backToList() {
        loader.cancelSearch();
    }
}


