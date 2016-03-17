package com.example.shustrik.vkdocs.adapters;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.PopupWindow;

import com.example.shustrik.vkdocs.R;
import com.example.shustrik.vkdocs.common.DocUtils;
import com.example.shustrik.vkdocs.uicommon.AnimationManager;

import java.lang.reflect.Field;

class DocItemMenuListener implements View.OnClickListener{
    private Context context;
    private int menuRes;
    private RecyclerView recyclerView;
    private int docId;
    private int ownerId;
    private String title;
    private int position;
    private boolean isPopup = false;
    private PopupMenu.OnDismissListener animListener;

    public DocItemMenuListener(Context context, int menuRes, RecyclerView recyclerView) {
        this.context = context;
        this.menuRes = menuRes;
        this.recyclerView = recyclerView;
    }

    /**
     * public MyPopupMenu(Context context, View anchor) {
     * // TODO Theme?
     * mContext = context;
     * mMenu = new MenuBuilder(context);
     * mMenu.setCallback(this);
     * mAnchor = anchor;
     * mPopup = new MenuPopupHelper(context, mMenu, anchor);
     * mPopup.setCallback(this);
     * mPopup.setForceShowIcon(true); //ADD THIS LINE
     * <p>
     * }
     */
    @Override
    public void onClick(final View v) {
        PopupMenu popupMenu = new PopupMenu(context, v);
        setFading(recyclerView, popupMenu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.doc_delete:
                        DocUtils.delete(ownerId, docId, context, new DocUtils.RequestCallback() {
                            @Override
                            public void onSuccess() {
                                recyclerView.removeViewAt(position);
                                recyclerView.getAdapter().notifyItemRemoved(position);
                            }

                            @Override
                            public void onFailure() {
                                //send a message
                            }
                        });
                        return true;

                    case R.id.doc_rename:
                        displayPopupWindow(title);
                        return true;
                    case R.id.doc_offline:
                        return true;
                    case R.id.doc_download:
                        return true;
                    case R.id.doc_add:
                        DocUtils.add(((BaseDocListAdapter) (recyclerView.getAdapter()))
                                .getDocumentOnMenuClick(position), context);
                        return true;
                    case R.id.doc_share_link:
                        return true;
                    case R.id.doc_share:
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
            argTypes = new Class[]{boolean.class};
            menuHelper.getClass().getDeclaredMethod("setForceShowIcon", argTypes).invoke(menuHelper, true);
        } catch (Exception e) {
            Log.w("ANNA", "error forcing menu icons to show", e);
        }

        popupMenu.inflate(menuRes);
        popupMenu.show();
    }


    //hide fab
    //узнать, что за чушь с тенью
    //можно добавить затенение исходного элемента
    //добавить snack и сюда, и в добавление (на успех, т.к. на экране мы результата не видим)
    private void displayPopupWindow(String title) {
        isPopup = true;

        final PopupWindow popup = new PopupWindow();
        //http://stackoverflow.com/questions/27259614/android-popupwindow-elevation-does-not-show-shadow
        popup.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popup.setElevation(24);
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                isPopup = false;
                animListener.onDismiss(null);
            }
        });
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.rename_window, null);
        popup.setContentView(layout);
        // Set content width and height
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        // Closes the popup window when touch outside of it - when looses focus
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        popup.setTouchable(true);
        popup.showAtLocation(recyclerView, Gravity.CENTER, 0, 0);
        // Show anchored to button
//        popup.setBackgroundDrawable(new BitmapDrawable());
//        popup.showAsDropDown(anchorView);
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
                DocUtils.rename(ownerId, docId, docTitle.getText().toString(), context,
                        new DocUtils.RequestCallback() {
                            @Override
                            public void onSuccess() {
                                ((BaseDocListAdapter)recyclerView.getAdapter())
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


    public void setFading(final RecyclerView recyclerView, PopupMenu popupMenu) {
        // Dim out all the other list items if they exist
        LinearLayoutManager layoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
        final int firstPos = layoutManager.findFirstVisibleItemPosition();
        Log.w("ANNA", "Popup: " + firstPos);
        final int lastPos = layoutManager.findLastVisibleItemPosition();
        Log.w("ANNA", "Popup: " + lastPos);

        //0, recyclerView.getChildCount()
        applyAnimation(0, recyclerView.getChildCount(), AnimationManager.getAnimFadeOut(context));
        animListener = new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                if (!isPopup) applyAnimation(0, recyclerView.getChildCount(), AnimationManager.getAnimFadeIn(context));
            }
        };
        popupMenu.setOnDismissListener(animListener);
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

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
