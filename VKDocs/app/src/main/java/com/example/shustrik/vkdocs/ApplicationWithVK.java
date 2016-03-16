package com.example.shustrik.vkdocs;

import android.app.Application;
import android.util.Log;

import com.example.shustrik.vkdocs.files.FilesRemoveTimer;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;

/**
 * VK SDK initialization
 */
public class ApplicationWithVK extends Application {

    public static final String TAG = "ANNA_Application";
    public static FilesRemoveTimer timer;

    VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
        @Override
        public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
            if (newToken == null) {
                Log.w(TAG, "New Token is null");
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        vkAccessTokenTracker.startTracking();
        VKSdk.initialize(getApplicationContext());
        timer = new FilesRemoveTimer(getApplicationContext());
    }
}
