package com.example.shustrik.vkdocs.uicommon;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupMenu;

import com.example.shustrik.vkdocs.R;

public class AnimationManager {
    private AnimationManager() {}

    public static Animation getAnimFadeIn(Context context) {
        Animation animFadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        animFadeIn.setFillAfter(true);
        return animFadeIn;
    }

    public static Animation getAnimFadeOut(Context context) {
        Animation animFadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out);
        animFadeOut.setFillAfter(true);
        return animFadeOut;
    }
}
