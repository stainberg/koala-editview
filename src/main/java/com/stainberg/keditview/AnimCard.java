package com.stainberg.keditview;

import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

/**
 * Created by Stainberg on 11/2/17.
 */

public class AnimCard {

    WeakReference<View> viewWeakReference;

    public AnimCard(View v) {
        viewWeakReference = new WeakReference<>(v);
    }

    public void setHeight(int y) {
        View v = viewWeakReference.get();
        if(v != null) {
            ViewGroup.LayoutParams lp =  v.getLayoutParams();
            lp.height = y;
            v.setLayoutParams(lp);
        }
    }

}

