package com.stainberg.keditview;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

/**
 * Created by Stainberg on 8/2/17.
 */

public class KoalaEditText extends AppCompatEditText {

    private OnSelectionChangedListener changedListener;

    public KoalaEditText(Context context) {
        super(context);
    }

    public KoalaEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KoalaEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public KoalaEditText(Context context, OnSelectionChangedListener l) {
        super(context);
        changedListener = l;
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if(changedListener != null) {
            changedListener.onSelectionChanged(selStart, selEnd);
        }
    }

    interface OnSelectionChangedListener {
        void onSelectionChanged(int selStart, int selEnd);
    }
}
