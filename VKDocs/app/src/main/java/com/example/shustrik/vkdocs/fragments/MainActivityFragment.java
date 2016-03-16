package com.example.shustrik.vkdocs.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.shustrik.vkdocs.R;
import com.example.shustrik.vkdocs.SelectCallback;
import com.example.shustrik.vkdocs.adapters.CustomAdapter;
import com.example.shustrik.vkdocs.download.DocDownloader;
import com.example.shustrik.vkdocs.loaders.CustomLoader;
import com.example.shustrik.vkdocs.uicommon.DividerItemDecoration;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 * <p/>
 * В Wearable можно подсмотреть анимацию
 * <p/>
 * Сделать колесо загрузки
 * <p/>
 * Дорогой блог, напиши про findviewbyid в navigationheader
 * <p/>
 * Отработать ситуацию, когда во время загрузки документа щёлкают по ещё одному -- Map? how to open?
 * Является ли DownloadService синхронизированным?
 * Что-то не так с выбором типа - pl пытался открыться через pdf-reader
 * <p/>
 * Если закрыть до завершения загрузки, то
 * java.lang.IllegalStateException: Fragment MainActivityFragment{c1e467a} not attached to Activity
 * at android.support.v4.app.Fragment.startActivity(Fragment.java:914)
 * at com.example.shustrik.vkdocs.MainActivityFragment.openFile(MainActivityFragment.java:150)
 * <p/>
 * Добавить Loading your files
 * https://www.google.com/design/spec/components/progress-activity.html#progress-activity-behavior
 *
 * Стрелка назад не работает, починить
 */
public class MainActivityFragment extends Fragment {
    @Bind(R.id.recyclerview)
    RecyclerView recyclerView;
    @Bind(R.id.recyclerview_docs_empty)
    TextView emptyView;
    @Bind(R.id.loading_display)
    LinearLayout loadingLayout;

    private DocDownloader docDownloader;
    private CustomAdapter adapter;
    private CustomLoader loader;
    private boolean isHomeAsUpEnabled;
    private String emptyText;
    private String loadText;


    public static MainActivityFragment getInstance(CustomAdapter adapter,
                                                   CustomLoader loader,
                                                   DocDownloader docDownloader,
                                                   boolean isHomeAsUpEnabled,
                                                   String emptyText,
                                                   String loadText) {
        MainActivityFragment fragment = new MainActivityFragment();
        fragment.adapter = adapter;
        fragment.loader = loader;
        fragment.isHomeAsUpEnabled = isHomeAsUpEnabled;
        fragment.docDownloader = docDownloader;
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
        ((TextView)(loadingLayout.findViewById(R.id.loading_text))).setText(loadText);

        adapter.setEmptyView(emptyView);
        adapter.bindRecyclerView(recyclerView);
        adapter.setLoadingView(loadingLayout);

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (docDownloader != null) {
            docDownloader.onDetach();
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        loader.initLoader();
        super.onActivityCreated(savedInstanceState);
    }
}
