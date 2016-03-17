package com.example.shustrik.vkdocs.adapters;

import android.content.Context;
import android.view.View;

import com.example.shustrik.vkdocs.download.DocDownloader;
import com.example.shustrik.vkdocs.vk.MyVKApiDocument;

import java.util.ArrayList;
import java.util.List;

public class SpecDocListAdapter extends BaseDocListAdapter {
    private List<MyVKApiDocument> documents;
    private LoadMore loadMore;

    private boolean loadFinished = false;
    private boolean loadingInProgress = false;
    private int loadingThreshold = 3;


    public SpecDocListAdapter(Context context, DocDownloader docDownloader, int menuId) {
        super(context, docDownloader, menuId);
    }

    public void setLoadMore(LoadMore loadMore) {
        this.loadMore = loadMore;
    }

    @Override
    public MyVKApiDocument getDocumentOnMenuClick() {
        int position = getDocumentOnMenuPosition();
        return (position >= 0 && position < documents.size()) ? documents.get(position) : null;
    }

    @Override
    void moveToPosition(int position) {

    }


    @Override
    public void onBindViewHolder(BaseAdapterViewHolder holder, int position) {
        MyVKApiDocument doc = documents.get(position);
        setDocId(holder, doc.getId());
        setOwnerId(holder, doc.owner_id);
        setTitle(holder, doc.title);
        setSize(holder, (int) doc.size);
        setDate(holder, (int) doc.getDate());
        setUrl(holder, doc.url);
        setOffline(holder, 0);
        setPreview(holder, doc.photo_130, doc.getFileType());

        if (!loadFinished && (position >= getItemCount() - loadingThreshold))
            load(position);
    }


    private synchronized void load(int position) {
        if (!loadFinished && !loadingInProgress && position >= getItemCount() - loadingThreshold) {
            loadingInProgress = true;
            loadMore.load();
        }
    }


    public void addData(List<MyVKApiDocument> newDocuments) {
        if (documents == null) {
            documents = new ArrayList<>();
        }
        int start = documents.size();
        documents.addAll(newDocuments);
        notifyItemRangeInserted(start, newDocuments.size());
    }


    public void notifyLoadingComplete() {
        loadingInProgress = false;
    }


    public void notifyLoadFinished() {
        loadFinished = true;
    }


    @Override
    public int getItemCount() {
        if (null == documents) return 0;
        return documents.size();
    }


    public void swapData(List<MyVKApiDocument> documents) {
        this.documents = documents;
        notifyDataSetChanged();
        recyclerView.setVisibility(getItemCount() != 0 && !loading ? View.VISIBLE : View.GONE);
        emptyView.setVisibility(getItemCount() == 0 && !loading ? View.VISIBLE : View.GONE);
    }


    public interface LoadMore {
        void load();
    }
}

