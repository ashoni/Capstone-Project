package com.example.shustrik.vkdocs.menus;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.example.shustrik.vkdocs.MainActivity;
import com.example.shustrik.vkdocs.R;
import com.example.shustrik.vkdocs.adapters.DocListAdapter;
import com.example.shustrik.vkdocs.common.DocUtils;
import com.example.shustrik.vkdocs.download.DefaultDownloader;
import com.example.shustrik.vkdocs.download.DocDownloader;

/**
 * Responsible for the document menu
 */
class DocItemMenuListener {
    private MainActivity activity;
    private RecyclerView recyclerView;
    private int docId;
    private int ownerId;
    private String title;
    private int position;
    private String url;
    private DocDownloader docDownloader;
    private DocMenu docMenu;

    public DocItemMenuListener(MainActivity activity, RecyclerView recyclerView,
                               DocDownloader docDownloader, DocMenu docMenu) {
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.docDownloader = docDownloader;
        this.docMenu = docMenu;
    }

    public boolean onClick(int menuId, boolean state) {
        switch (menuId) {
            case R.id.doc_offline:
                if (state) {
                    docDownloader.processToOffline(url, title, docId);
                } else {
                    docDownloader.onCancelPressed(docId);
                    DocUtils.setOfflineUnavailable(activity, docId);
                }
                return true;
            default:
                return false;
        }
    }

    public boolean onClick(int menuId) {
        switch (menuId) {
            case R.id.doc_delete:
                docMenu.hide();
                DocUtils.delete(ownerId, docId, activity, new DocUtils.RequestCallback() {
                    @Override
                    public void onSuccess() {
                        ((DocListAdapter) recyclerView.getAdapter()).onRemoved(position);
                    }

                    @Override
                    public void onFailure() {
                        activity.snack(activity.getString(R.string.delete_failure, title), Snackbar.LENGTH_SHORT);
                    }
                });
                return true;

            case R.id.doc_rename:
                displayPopupWindow(title);
                return true;
            case R.id.doc_download:
                docMenu.hide();
                DefaultDownloader.getInstance().downloadFile(url, title);
                return true;
            case R.id.doc_add:
                docMenu.hide();
                DocUtils.add(((DocListAdapter) (recyclerView.getAdapter()))
                        .getDocumentOnMenuClick(position), activity);
                return true;
            case R.id.doc_share_link:
                docMenu.hide();
                activity.startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(activity)
                        .setType("text/plain")
                        .setText(title + " : " + url)
                        .getIntent(), activity.getString(R.string.share)));
                return true;
//            case R.id.doc_share:
//                docMenu.hide();
//                return true;
            default:
                return false;
        }
    }


    private void displayPopupWindow(String title) {
        docMenu.delayAnimation();
        docMenu.hide();
        final PopupWindow popup = new PopupWindow();
        //http://stackoverflow.com/questions/27259614/android-popupwindow-elevation-does-not-show-shadow
        popup.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popup.setElevation(24);
        }
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                docMenu.continueAnimation();
            }
        });
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.rename_window, null);
        popup.setContentView(layout);
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        popup.setTouchable(true);
        popup.showAtLocation(recyclerView, Gravity.CENTER, 0, 0);
        final EditText docTitle = (EditText) layout.findViewById(R.id.new_caption);
        docTitle.setText(title);
        final View okView = layout.findViewById(R.id.rename_ok);
        View cancelView = layout.findViewById(R.id.rename_cancel);

        docTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    okView.setEnabled(false);
                } else {
                    okView.setEnabled(true);
                }
            }
        });

        okView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocUtils.rename(ownerId, docId, docTitle.getText().toString(), activity,
                        new DocUtils.RequestCallback() {
                            @Override
                            public void onSuccess() {
                                ((DocListAdapter) recyclerView.getAdapter())
                                        .rename(position, docTitle.getText().toString());
                                popup.dismiss();
                            }

                            @Override
                            public void onFailure() {

                            }
                        });
            }
        });

        cancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
