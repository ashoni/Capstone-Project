package com.example.shustrik.vkdocs.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.shustrik.vkdocs.R;
import com.example.shustrik.vkdocs.common.Utils;
import com.example.shustrik.vkdocs.download.DocDownloader;
import com.example.shustrik.vkdocs.uicommon.DocIcons;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public abstract class BaseDocListAdapter extends RecyclerView.Adapter<BaseDocListAdapter.BaseAdapterViewHolder>
        implements CustomAdapter {
    private Context context;
    protected RecyclerView recyclerView;
    protected View emptyView;
    private DocDownloader docDownloader;
    private View loadingView;
    protected boolean loading;

    public BaseDocListAdapter(Context context, DocDownloader docDownloader) {
        this.context = context;
        this.docDownloader = docDownloader;
    }

    @Override
    public void setLoading(boolean loading) {
        this.loading = loading;
        if (loading) {
            Log.w("ANNA", "Set Visible " + ((TextView)(loadingView.findViewById(R.id.loading_text))).getText());
            loadingView.setVisibility(View.VISIBLE);
        } else {
            Log.w("ANNA", "Set Gone " + ((TextView)(loadingView.findViewById(R.id.loading_text))).getText());
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

    public Context getContext() {
        return context;
    }

    @Override
    public BaseAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {
            int layoutId = R.layout.doc_list_item;
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            view.setFocusable(true);
            return new BaseAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    abstract void moveToPosition(int position);

    protected void setDocId(BaseAdapterViewHolder holder, int docId) {
        holder.setDocId(docId);
        holder.getDocItemMenuListener().setDocId(docId);
    }

    protected void setOwnerId(BaseAdapterViewHolder holder, int ownerId) {
        holder.getDocItemMenuListener().setOwnerId(ownerId);
    }

    protected void setTitle(BaseAdapterViewHolder holder, String title) {
        holder.title.setText(title);
    }

    protected void setSize(BaseAdapterViewHolder holder, int size) {
        holder.size.setText(Utils.sizeToString(size));
    }

    protected void setDate(BaseAdapterViewHolder holder, int date) {
        holder.date.setText(Utils.convertDate(date));
    }

    protected void setUrl(BaseAdapterViewHolder holder, String url) {
        holder.setUrl(url);
    }

    protected void setOffline(BaseAdapterViewHolder holder, Integer offline) {
        //
    }

    protected void setPreview(BaseAdapterViewHolder holder, String previewUrl, Integer t) {
        if (previewUrl != null && !previewUrl.isEmpty()) {
            Picasso.with(getContext())
                    .load(previewUrl)
                    .placeholder(DocIcons.getIconId(t))
                    .error(DocIcons.getIconId(t))
                    .into(holder.preview);
        } else {
            holder.preview.setImageResource(DocIcons.getIconId(t));
        }
    }

    public class BaseAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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
        private DocItemMenuListener docItemMenuListener;

        public BaseAdapterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            Log.w("ANNA", "here");
            docItemMenuListener = new DocItemMenuListener(context, R.menu.my_docs_options,
                    recyclerView);
            overflow.setOnClickListener(docItemMenuListener);
            view.setOnClickListener(this);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.w("ANNA", "cancel on click: " + title.getText());
                    docDownloader.onCancelPressed(docId);
                }
            });
        }


        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            moveToPosition(adapterPosition);
            docDownloader.processToOpen(this);
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

        public ProgressBar getProgressBar() {
            return progressBar;
        }

        public DocItemMenuListener getDocItemMenuListener() {
            return docItemMenuListener;
        }

        public void setDownloadMode(boolean isActive) {
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
    }
}

