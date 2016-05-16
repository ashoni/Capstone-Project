package com.example.shustrik.vkdocs.uicommon;


import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.example.shustrik.vkdocs.R;

/**
 * Requests permissions for files browsing
 */
public class OpenFileDialogPermissionsManager {
    public static final int READ_PERMISSION = 57;

    private OpenFileDialogPermissionsManager() {
    }

    public static void checkPermissions(final Activity activity, Callback callback) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showMessage(activity, activity.getString(R.string.ask_browse_permissions),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == DialogInterface.BUTTON_POSITIVE) {
                                    requestPermissions(activity);
                                }
                            }
                        });
            } else {
                requestPermissions(activity);
            }
        } else {
            callback.performIfGranted();
        }
    }

    public interface Callback {
        void performIfGranted();
    }

    private static void requestPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_PERMISSION);
    }

    private static void showMessage(Activity activity, String message,
                                    DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton(activity.getString(R.string.ok), okListener)
                .setNegativeButton(activity.getString(R.string.cancel), null)
                .create()
                .show();
    }
}

