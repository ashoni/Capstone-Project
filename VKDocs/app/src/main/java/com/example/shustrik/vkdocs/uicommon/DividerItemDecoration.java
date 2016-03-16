package com.example.shustrik.vkdocs.uicommon;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.example.shustrik.vkdocs.R;


public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private static final int[] ATTRS = new int[]{
            android.R.attr.listDivider
    };

    public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;

    public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

    private Drawable divider;

    private int orientation;

    public DividerItemDecoration(Context context, int orientation) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        divider = a.getDrawable(0);
        a.recycle();
        setOrientation(orientation);
    }

    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
            throw new IllegalArgumentException("invalid orientation");
        }
        this.orientation = orientation;
    }

//    @Override
//    public void onDraw(Canvas c, RecyclerView parent) {
//        if (orientation == VERTICAL_LIST) {
//            drawVertical(c, parent);
//        } else {
//            drawHorizontal(c, parent);
//        }
//    }
//
//    public void drawVertical(Canvas c, RecyclerView parent) {
//        final int margin = (parent.getWidth() - parent.getPaddingRight()
//                - parent.getPaddingLeft()) / 10;
//        final int left = parent.getPaddingLeft() + margin;
//        final int right = parent.getWidth() - parent.getPaddingRight() - margin;
//
//
//        final int childCount = parent.getChildCount();
//        for (int i = 0; i < childCount; i++) {
//            final View child = parent.getChildAt(i);
//            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
//                    .getLayoutParams();
//            final int top = child.getBottom() + params.bottomMargin;
//            final int bottom = top + divider.getIntrinsicHeight();
//            Log.w("ANNA", "Divider: "
//                    + left + " "
//                    + top + " "
//                    + right + " "
//                    + bottom);
//            divider.setBounds(left + params.leftMargin, top, right - params.leftMargin, top + 1);
//            divider.setColorFilter(ContextCompat.getColor(context, R.color.super_light_grey), PorterDuff.Mode.MULTIPLY);
//            divider.draw(c);
//        }
//    }
//
//    public void drawHorizontal(Canvas c, RecyclerView parent) {
//        final int top = parent.getPaddingTop();
//        final int bottom = parent.getHeight() - parent.getPaddingBottom();
//
//        final int childCount = parent.getChildCount();
//        for (int i = 0; i < childCount; i++) {
//            final View child = parent.getChildAt(i);
//            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
//                    .getLayoutParams();
//            final int left = child.getRight() + params.rightMargin;
//            final int right = left + divider.getIntrinsicWidth();
//            divider.setBounds(left, top, right, bottom);
//            divider.draw(c);
//        }
//    }

    //Расстояние между элементами
    @Override
    public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
        if (orientation == VERTICAL_LIST) {
            outRect.set(0, 0, 0, divider.getIntrinsicHeight());
        } else {
            outRect.set(0, 0, divider.getIntrinsicWidth(), 0);
        }
    }
}