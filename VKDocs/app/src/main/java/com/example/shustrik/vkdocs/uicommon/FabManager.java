package com.example.shustrik.vkdocs.uicommon;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.example.shustrik.vkdocs.R;

/**
 * Shows New Document popup on fab click
 */
public class FabManager {
    private PopupWindow helpPopup;
    private View parentView;
    private FabCallback callback;

    public FabManager(Activity activity, View view, final FabCallback callback) {
        this.callback = callback;
        helpPopup = new PopupWindow(activity);
        parentView = view;
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.fab_options, null);

        final View drive = layout.findViewById(R.id.drive_upload);
        drive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onDriveUpload();
                dismiss();
            }
        });
        final View local = layout.findViewById(R.id.device_upload);
        local.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onLocalUpload();
                dismiss();
            }
        });

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        helpPopup.setContentView(layout);
        helpPopup.setFocusable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            helpPopup.setElevation(24);
        }
        helpPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        helpPopup.setWidth(WindowManager.LayoutParams.MATCH_PARENT);

        helpPopup.setOutsideTouchable(false);
        helpPopup.setTouchable(true);
        helpPopup.setAnimationStyle(R.style.FadingAnimation);
        helpPopup.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(activity, R.color.vk_grey_color)));
    }

    public interface FabCallback {
        void onLocalUpload();

        void onDriveUpload();

//        void onDismiss();
    }

    public void show() {
        helpPopup.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
    }

    public void dismiss() {
//        callback.onDismiss();
        helpPopup.dismiss();
    }
}
