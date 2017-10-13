package com.stainberg.keditview;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Stainberg on 10/13/17.
 */

class DragState {
    View view;
    int index;

    DragState(View view) {
        this.view = view;
        index = ((ViewGroup)view.getParent()).indexOfChild(view);
    }
}
