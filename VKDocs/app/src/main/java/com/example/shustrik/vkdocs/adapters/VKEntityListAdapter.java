package com.example.shustrik.vkdocs.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shustrik.vkdocs.R;
import com.example.shustrik.vkdocs.uicommon.DocIcons;
import com.example.shustrik.vkdocs.vk.MyVKEntity;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class VKEntityListAdapter extends RecyclerView.Adapter<VKEntityListAdapter.DialogViewHolder>
        implements CustomAdapter {
    private List<MyVKEntity> vkEntities;
    private Context context;
    private View emptyView;
    private View loadingView;
    private boolean loading;
    private OnClickHandler handler;
    private RecyclerView recyclerView;


    public VKEntityListAdapter(Context context, OnClickHandler handler) {
        this.context = context;
        this.handler = handler;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

    }

    @Override
    public void setLoading(boolean loading) {
        this.loading = loading;
        if (loading) {
            loadingView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        } else {
            loadingView.setVisibility(View.GONE);
        }
    }

    @Override
    public void setLoadingView(View loadingView) {
        this.loadingView = loadingView;
    }

    @Override
    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
    }

    @Override
    public void bindRecyclerView(RecyclerView recyclerView) {
        recyclerView.setAdapter(this);
        this.recyclerView = recyclerView;
    }

    @Override
    public DialogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {
            int layoutId = R.layout.dialog_list_item;
            View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            view.setFocusable(true);
            return new DialogViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(DialogViewHolder holder, int position) {
        MyVKEntity vkEntity = vkEntities.get(position);
        holder.setPeerId(vkEntity.getPeerId());
        holder.username.setText(vkEntity.getPeerName());
        if (vkEntity.getPreviewUrl() != null) {
            Picasso.with(context)
                    .load(vkEntity.getPreviewUrl())
                    .placeholder(DocIcons.getChatIcon())
                    .error(DocIcons.getChatIcon())
                    .into(holder.preview);
        } else {
            holder.preview.setImageResource(DocIcons.getChatIcon());
        }
    }

    @Override
    public int getItemCount() {
        if (null == vkEntities) return 0;
        return vkEntities.size();
    }

    public void swapData(List<MyVKEntity> vkEntities) {
        this.vkEntities = vkEntities;
        notifyDataSetChanged();
        recyclerView.setVisibility(getItemCount() != 0 && !loading ? View.VISIBLE : View.GONE);
        emptyView.setVisibility(getItemCount() == 0 && !loading ? View.VISIBLE : View.GONE);
    }

    public interface OnClickHandler {
        void onClick(DialogViewHolder vh);
    }

    public class DialogViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @Bind(R.id.dialog_preview)
        ImageView preview;
        @Bind(R.id.username)
        TextView username;

        private int peerId;

        public DialogViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        public void setPeerId(int peerId) {
            this.peerId = peerId;
        }

        @Override
        public void onClick(View v) {
            handler.onClick(this);
        }

        public int getPeerId() {
            return peerId;
        }
    }
}
