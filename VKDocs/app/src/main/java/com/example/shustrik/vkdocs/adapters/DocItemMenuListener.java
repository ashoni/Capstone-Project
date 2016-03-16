package com.example.shustrik.vkdocs.adapters;


import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.PopupMenu;

import com.example.shustrik.vkdocs.R;
import com.example.shustrik.vkdocs.common.DocUtils;
import com.example.shustrik.vkdocs.uicommon.AnimationManager;

import java.lang.reflect.Field;

class DocItemMenuListener implements View.OnClickListener {
    private Context context;
    private int menuRes;
    private RecyclerView recyclerView;
    private int docId;
    private int ownerId;

    public DocItemMenuListener(Context context, int menuRes, RecyclerView recyclerView) {
        this.context = context;
        this.menuRes = menuRes;
        this.recyclerView = recyclerView;
    }

    /**
     * public MyPopupMenu(Context context, View anchor) {
     // TODO Theme?
     mContext = context;
     mMenu = new MenuBuilder(context);
     mMenu.setCallback(this);
     mAnchor = anchor;
     mPopup = new MenuPopupHelper(context, mMenu, anchor);
     mPopup.setCallback(this);
     mPopup.setForceShowIcon(true); //ADD THIS LINE

     }
     */
    @Override
    public void onClick(final View v) {
        PopupMenu popupMenu = new PopupMenu(context, v);
        setFading(recyclerView, popupMenu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.doc_delete:
                        DocUtils.delete(ownerId, docId, context, new DocUtils.RequestCallback() {
                            @Override
                            public void onSuccess() {
                                recyclerView.removeView(v);
                            }

                            @Override
                            public void onFailure() {
                                //send a message
                            }
                        });
                        return true;

                    case R.id.doc_share:
                        //renameAlbum(mAlbum);
                        return true;

                    default:
                        return false;
                }
            }
        });

        // Force icons to show
        Object menuHelper;
        Class[] argTypes;
        try {
            Field fMenuHelper = PopupMenu.class.getDeclaredField("mPopup");
            fMenuHelper.setAccessible(true);
            menuHelper = fMenuHelper.get(popupMenu);
            argTypes = new Class[] { boolean.class };
            menuHelper.getClass().getDeclaredMethod("setForceShowIcon", argTypes).invoke(menuHelper, true);
        } catch (Exception e) {
            Log.w("ANNA", "error forcing menu icons to show", e);
        }

        popupMenu.inflate(menuRes);
        popupMenu.show();
    }


    public void setFading(final RecyclerView recyclerView, PopupMenu popupMenu) {
        // Dim out all the other list items if they exist
        LinearLayoutManager layoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
        final int firstPos = layoutManager.findFirstVisibleItemPosition();
        Log.w("ANNA", "Popup: " + firstPos);
        final int lastPos = layoutManager.findLastVisibleItemPosition();
        Log.w("ANNA", "Popup: " + lastPos);

        //0, recyclerView.getChildCount()
        applyAnimation(0, recyclerView.getChildCount(), AnimationManager.getAnimFadeOut(context));
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu popupMenu) {
                applyAnimation(0, recyclerView.getChildCount(), AnimationManager.getAnimFadeIn(context));
            }
        });
    }

    private void applyAnimation(int from, int to, Animation animation) {
        for (int i = from; i <= to; i++) {
            final View child = recyclerView.getChildAt(i);
            if (child == null ||
                    docId == ((BaseDocListAdapter.BaseAdapterViewHolder)
                            (recyclerView.getChildViewHolder(child))).getDocId()) {
                Log.w("ANNA", "Miss " + i);
                continue;
            }
            Log.w("ANNA", "Got " + i);
            View childInView = child.findViewById(R.id.list_item_grid);
            childInView.clearAnimation();
            childInView.startAnimation(animation);
        }
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }
}
