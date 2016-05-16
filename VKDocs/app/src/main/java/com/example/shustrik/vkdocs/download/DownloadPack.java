package com.example.shustrik.vkdocs.download;

import android.content.ServiceConnection;

import java.io.File;


/**
 * Stores information about each download process
 */
class DownloadPack {
    private ServiceConnection connection;
    private DownloadService service;
    private File f;
    private GOAL goal;
    private boolean attached = true;

    public DownloadPack(GOAL goal) {
        this.goal = goal;
    }

    public ServiceConnection getConnection() {
        return connection;
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

    public GOAL getGoal() {
        return goal;
    }

    public void setGoal(GOAL goal) {
        this.goal = goal;
    }

    public boolean isAttached() {
        return attached;
    }

    public void setAttached(boolean attached) {
        this.attached = attached;
    }

    public void cancelLoading() {
        service.cancel();
    }

    enum GOAL {
        TEMP,
        SAVE_TO_OFFLINE
    }
}
