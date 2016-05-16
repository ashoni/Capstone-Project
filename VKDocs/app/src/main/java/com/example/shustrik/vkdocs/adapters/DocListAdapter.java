package com.example.shustrik.vkdocs.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.shustrik.vkdocs.MainActivity;
import com.example.shustrik.vkdocs.R;
import com.example.shustrik.vkdocs.common.Utils;
import com.example.shustrik.vkdocs.download.DocDownloader;
import com.example.shustrik.vkdocs.download.DocDownloaderHolder;
import com.example.shustrik.vkdocs.download.DownloadListener;
import com.example.shustrik.vkdocs.menus.DocMenu;
import com.example.shustrik.vkdocs.uicommon.DocIcons;
import com.example.shustrik.vkdocs.vk.MyVKApiDocument;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Adapter for document list
 */
public class DocListAdapter extends RecyclerView.Adapter<DocListAdapter.DocViewHolder>
        implements CustomAdapter, DownloadListener {
    private List<MyVKApiDocument> documents;
    private LoadMore loadMore;

    private boolean loadFinished = false;
    private boolean loadingInProgress = false;
    private int loadingThreshold = 3;

    protected RecyclerView recyclerView;
    protected View emptyView;
    protected boolean loading;
    private MainActivity activity;
    private DocDownloader docDownloader;
    private View loadingView;
    private int menuId;

    private int openingDocId = -1;
    private int openingProgress = 0;
    private DocViewHolder openingViewHolder = null;
    private int listenerId = -1;


    public DocListAdapter(MainActivity activity, int menuId, int listenerId) {
        this.activity = activity;
        this.listenerId = listenerId;
        this.docDownloader = DocDownloaderHolder.getDocDownloader(this);
        this.menuId = menuId;
    }

    @Override
    public void onRefreshFailed() {
        activity.snack(activity.getString(R.string.refresh_failed), Snackbar.LENGTH_LONG);
    }


    public void onRemoved(int position) {
        documents.remove(position);
        notifyItemRemoved(position);
    }

    public void setLoadMore(LoadMore loadMore) {
        this.loadMore = loadMore;
    }

    @Override
    public int getListenerId() {
        return listenerId;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("op_doc_id", openingDocId);
        outState.putInt("op_progress", openingProgress);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        openingDocId = savedInstanceState.getInt("op_doc_id");
        openingProgress = savedInstanceState.getInt("op_progress");
    }

    @Override
    public void setLoading(boolean loading) {
        this.loading = loading;
        if (loading) {
            loadingView.setVisibility(View.VISIBLE);
        } else {
            loadingView.setVisibility(View.GONE);
        }
    }

    @Override
    public void setLoadingView(View loadingView) {
        this.loadingView = loadingView;
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
    }

    public void bindRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        recyclerView.setAdapter(this);
    }

    public Context getActivity() {
        return activity;
    }

    @Override
    public DocViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {
            int layoutId = R.layout.doc_list_item;
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            view.setFocusable(true);
            return new DocViewHolder(view, menuId);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    protected void setDocId(DocViewHolder holder, int docId) {
        holder.setDocId(docId);
        holder.getDocMenu().setDocId(docId);
        if (docId == openingDocId) {
            openingViewHolder = holder;
            openingViewHolder.setDownloadMode(true);
            openingViewHolder.setProgress(openingProgress);
        }
    }

    protected void setOwnerId(DocViewHolder holder, int ownerId) {
        holder.getDocMenu().setOwnerId(ownerId);
    }

    protected void setTitle(DocViewHolder holder, String title) {
        holder.title.setText(title);
        holder.getDocMenu().setTitle(title);
    }

    protected void setSize(DocViewHolder holder, int size) {
        holder.size.setText(Utils.sizeToString(size));
    }

    protected void setDate(DocViewHolder holder, int date) {
        holder.date.setText(Utils.convertDate(date));
    }

    protected void setUrl(DocViewHolder holder, String url) {
        holder.setUrl(url);
        holder.getDocMenu().setUrl(url);
    }

    protected void setOffline(DocViewHolder holder, Integer offline) {
        holder.getDocMenu().setOffline(offline > 0);
    }

    protected void setPreview(DocViewHolder holder, String previewUrl, Integer t) {
        if (previewUrl != null && !previewUrl.isEmpty()) {
            Picasso.with(getActivity())
                    .load(previewUrl)
                    .placeholder(DocIcons.getIconId(t))
                    .error(DocIcons.getIconId(t))
                    .into(holder.preview);
        } else {
            holder.preview.setImageResource(DocIcons.getIconId(t));
        }
    }

    protected void setPosition(DocViewHolder holder, int position) {
        holder.getDocMenu().setPosition(position);
    }

    public void releaseOpening(int docId) {
        if (openingDocId == docId) {
            openingViewHolder.setDownloadMode(false);
            openingDocId = -1;
            openingProgress = 0;
            openingViewHolder = null;
        }
    }

    public void updateOpeningProgress(int docId, int progress) {
        if (openingDocId == docId) {
            openingProgress = progress;
            if (openingViewHolder != null) {
                openingViewHolder.setProgress(progress);
            }
        }
    }


    public MyVKApiDocument getDocumentOnMenuClick(int position) {
        return (position >= 0 && position < documents.size()) ? documents.get(position) : null;
    }

    public void rename(int position, String title) {
        documents.get(position).title = title;
        notifyItemChanged(position);
    }


    @Override
    public void onBindViewHolder(DocViewHolder holder, int position) {
        MyVKApiDocument doc = documents.get(position);
        setDocId(holder, doc.getId());
        setOwnerId(holder, doc.owner_id);
        setTitle(holder, doc.title);
        setSize(holder, (int) doc.size);
        setDate(holder, (int) doc.getDate());
        setUrl(holder, doc.url);
        setOffline(holder, 0);
        setPreview(holder, doc.photo_130, doc.getFileType());

        setPosition(holder, position);

        if (!loadFinished && (position >= getItemCount() - loadingThreshold))
            load(position);
    }


    private synchronized void load(int position) {
        if (loadMore != null && !loadFinished && !loadingInProgress && position >= getItemCount() - loadingThreshold) {
            loadingInProgress = true;
            loadMore.load();
        }
    }


    public void addData(List<MyVKApiDocument> newDocuments) {
        if (documents == null || documents.isEmpty()) {
            documents = new ArrayList<>();
            documents.addAll(newDocuments);
            notifyDataSetChanged();
        } else {
            int start = documents.size();
            documents.addAll(newDocuments);
            notifyItemRangeInserted(start, newDocuments.size());
        }
    }


    public void notifyLoadingComplete() {
        recyclerView.setVisibility(getItemCount() != 0 ? View.VISIBLE : View.GONE);
        emptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
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

    public class DocViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @Bind(R.id.list_item_preview)
        ImageView preview;
        @Bind(R.id.list_item_title)
        TextView title;
        @Bind(R.id.list_item_date)
        TextView date;
        @Bind(R.id.list_item_size)
        TextView size;
        @Bind(R.id.menu_overflow)
        View overflow;
        @Bind(R.id.progressBar)
        ProgressBar progressBar;
        @Bind(R.id.cancel_loading)
        TextView cancel;

        private int docId;
        private String url;
        private DocMenu docMenu;

        public DocViewHolder(View view, int menuId) {
            super(view);
            ButterKnife.bind(this, view);
            docMenu = new DocMenu(activity, recyclerView, docDownloader, menuId);
            overflow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    docMenu.show(overflow);
                }
            });
            view.setOnClickListener(this);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    docDownloader.onCancelPressed(docId);
                }
            });
        }


        @Override
        public void onClick(View v) {
            if (openingDocId == -1) {
                int adapterPosition = getAdapterPosition();
                if (docDownloader.processToOpen(url, title.getText().toString(), docId)) {
                    openingDocId = docId;
                    openingProgress = 0;
                    openingViewHolder = this;
                    setDownloadMode(true);
                }
            }
        }

        public String getTitle() {
            return title.getText().toString();
        }

        public int getDocId() {
            return docId;
        }

        public void setDocId(int docId) {
            this.docId = docId;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public DocMenu getDocMenu() {
            return docMenu;
        }

        void setDownloadMode(boolean isActive) {
            if (isActive) {
                progressBar.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.VISIBLE);
                this.setIsRecyclable(false);
            } else {
                progressBar.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);
                this.setIsRecyclable(true);
            }
        }

        void setProgress(int progress) {
            progressBar.setProgress(progress);
        }
    }
}

