package com.stainberg.keditview;

import android.graphics.Point;
import android.view.View;

/**
 * Created by Stainberg on 10/13/17.
 */

public class KoalaDragShadowBuilder extends View.DragShadowBuilder {
    KoalaDragShadowBuilder(View view) {
        super(view);
    }

    @Override
    public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
        final View view = getView();
        if (view != null) {
            outShadowSize.set(view.getWidth(), view.getHeight());
            outShadowTouchPoint.set(outShadowSize.x - 45, outShadowSize.y / 2);
        }
    }
}
