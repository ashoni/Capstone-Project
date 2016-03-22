package com.example.shustrik.vkdocs.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
 * A placeholder fragment containing a simple view.
 * <p>
 * В Wearable можно подсмотреть анимацию
 * <p>
 * Сделать колесо загрузки
 * <p>
 * Дорогой блог, напиши про findviewbyid в navigationheader
 * <p>
 * Отработать ситуацию, когда во время загрузки документа щёлкают по ещё одному -- Map? how to open?
 * Является ли DownloadService синхронизированным?
 * Что-то не так с выбором типа - pl пытался открыться через pdf-reader
 * <p>
 * Если закрыть до завершения загрузки, то
 * java.lang.IllegalStateException: Fragment MainActivityFragment{c1e467a} not attached to Activity
 * at android.support.v4.app.Fragment.startActivity(Fragment.java:914)
 * at com.example.shustrik.vkdocs.MainActivityFragment.openFile(MainActivityFragment.java:150)
 * <p>
 * Добавить Loading your files
 * https://www.google.com/design/spec/components/progress-activity.html#progress-activity-behavior
 * <p>
 * Стрелка назад не работает, починить
 */
public class MainActivityFragment extends Fragment {
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

    public MainActivityFragment() {
        type = -1;
    }

    public static MainActivityFragment getInstance(int t,
                                                   boolean isHomeAsUpEnabled,
                                                   String emptyText,
                                                   String loadText) {
        MainActivityFragment fragment = new MainActivityFragment();
        fragment.type = t;
        fragment.isHomeAsUpEnabled = isHomeAsUpEnabled;
        fragment.emptyText = emptyText;
        fragment.loadText = loadText;
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);

        Log.w("ANNA", "Main fragment type = " + type);
        if (type == -1 && savedInstanceState != null) {
            Log.w("ANNA", "Main fragment restore");
            emptyText = savedInstanceState.getString("empty_text");
            loadText = savedInstanceState.getString("loading_text");
            isHomeAsUpEnabled = savedInstanceState.getBoolean("home_up");
            type = savedInstanceState.getInt("fragment_type");
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        if (!isHomeAsUpEnabled) {
            Log.w("ANNA", "HomeUp not enabled");
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            ((SelectCallback) getActivity()).setToggleListener(true);
        } else {
            Log.w("ANNA", "HomeUp enabled");
            ((SelectCallback) getActivity()).setToggleListener(false);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
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
        // When tablets rotate, the currently selected list item needs to be saved.
        if (adapter != null) {
            adapter.onSaveInstanceState(outState);
        }
        outState.putString("empty_text", emptyText);
        outState.putString("loading_text", loadText);
        outState.putBoolean("home_up", isHomeAsUpEnabled);
        outState.putInt("fragment_type", type);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        loader.initLoader();
        super.onActivityCreated(savedInstanceState);
    }
}


