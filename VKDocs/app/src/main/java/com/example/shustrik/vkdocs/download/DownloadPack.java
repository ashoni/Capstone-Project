package com.example.shustrik.vkdocs.download;

import android.content.ServiceConnection;

import com.example.shustrik.vkdocs.adapters.BaseDocListAdapter;

import java.io.File;


public class DownloadPack {
    private ServiceConnection connection;
    private DownloadService service;
    private BaseDocListAdapter.BaseAdapterViewHolder holder;
    private File f;

    public DownloadPack(BaseDocListAdapter.BaseAdapterViewHolder holder) {
        this.holder = holder;
    }

    public ServiceConnection getConnection() {
        return connection;
    }

    public void setProgress(int progress) {
        holder.getProgressBar().setProgress(progress);
    }

    public void setDownloadMode(boolean mode) {
        holder.setDownloadMode(mode);
    }

    public DownloadService getService() {
        return service;
    }

    public void setService(DownloadService service) {
        this.service = service;
    }

    public void setConnection(ServiceConnection connection) {
        this.connection = connection;
    }

    public File getFile() {
        return f;
    }

    public void setFile(File f) {
        this.f = f;
    }
}
