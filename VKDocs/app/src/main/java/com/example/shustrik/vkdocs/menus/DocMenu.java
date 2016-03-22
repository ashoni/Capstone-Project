package com.example.shustrik.vkdocs.menus;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.example.shustrik.vkdocs.R;
import com.example.shustrik.vkdocs.adapters.DocListAdapter;
import com.example.shustrik.vkdocs.download.DocDownloader;
import com.example.shustrik.vkdocs.uicommon.AnimationManager;

import java.util.ArrayList;
import java.util.List;

public class DocMenu {
    private PopupWindow menuPopup;
    private MenuAdapter menuAdapter;
    private Activity activity;
    private int docId;
    private RecyclerView recyclerView;
    private boolean newPopupCreated = false;
    private PopupWindow.OnDismissListener animListener;
    private DocItemMenuListener docItemMenuListener;
    private boolean isOffline;

    public DocMenu(Activity activity,
                   RecyclerView recyclerView,
                   DocDownloader docDownloader,
                   int menuId) {
        this.activity = activity;
        this.recyclerView = recyclerView;

        Menu menu = new MenuBuilder(activity);
        activity.getMenuInflater().inflate(menuId, menu);
        List<MenuItem> menuItems = new ArrayList<>();
        for (int i = 0; i < menu.size(); i++) {
            menuItems.add(menu.getItem(i));
        }

        menuPopup = new PopupWindow(activity);

        docItemMenuListener = new DocItemMenuListener(activity, recyclerView, docDownloader, this);
        menuAdapter = new MenuAdapter(activity, R.layout.menu_item_layout, menuItems,
                docItemMenuListener, this);

        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View menuView = inflater.inflate(R.layout.menu_layout, null);
        ListView menuListView = (ListView) menuView.findViewById(R.id.menu_list);
        menuListView.setAdapter(menuAdapter);

        menuPopup.setContentView(menuView);
        menuPopup.setFocusable(true);
        menuPopup.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        menuPopup.setElevation(24);
        menuPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        menuPopup.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        menuPopup.setOutsideTouchable(true);
        menuPopup.setTouchable(true);
    }

    public void show(View v) {
        menuPopup.showAtLocation(recyclerView, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0);
        setFading(recyclerView, menuPopup);
    }

    public void hide() {
        menuPopup.dismiss();
    }

    public void setDocId(int docId) {
        this.docId = docId;
        docItemMenuListener.setDocId(docId);
    }

    public void setOwnerId(int ownerId) {
        docItemMenuListener.setOwnerId(ownerId);
    }

    public void setTitle(String title) {
        docItemMenuListener.setTitle(title);
    }

    public void setUrl(String url) {
        docItemMenuListener.setUrl(url);
    }

    public void setPosition(int position) {
        docItemMenuListener.setPosition(position);
    }

    public void setOffline(boolean isOffline) {
        //menuAdapter.setOffline(isOffline);
        this.isOffline = isOffline;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public void delayAnimation() {
        newPopupCreated = true;
    }

    public void continueAnimation() {
        newPopupCreated = false;
        animListener.onDismiss();
    }

    public void setFading(final RecyclerView recyclerView, PopupWindow popupMenu) {
        applyAnimation(0, recyclerView.getChildCount(), AnimationManager.getAnimFadeOut(activity));
        animListener = new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (!newPopupCreated) {
                    applyAnimation(0, recyclerView.getChildCount(), AnimationManager.getAnimFadeIn(activity));
                }
            }
        };
        popupMenu.setOnDismissListener(animListener);
    }

    private void applyAnimation(int from, int to, Animation animation) {
        for (int i = from; i <= to; i++) {
            final View child = recyclerView.getChildAt(i);
            if (child != null && docId != ((DocListAdapter.DocViewHolder)
                    (recyclerView.getChildViewHolder(child))).getDocId()) {
                View childInView = child.findViewById(R.id.list_item_grid);
                childInView.clearAnimation();
                childInView.startAnimation(animation);
            }
        }
    }
}
