package com.example.shustrik.vkdocs.sync;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class VKDocsSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static VKDocsSyncAdapter sSunshineSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSunshineSyncAdapter == null) {
                sSunshineSyncAdapter = new VKDocsSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSunshineSyncAdapter.getSyncAdapterBinder();
    }
}
