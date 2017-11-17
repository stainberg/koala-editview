package com.stainberg.keditview

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * Created by Lynn.
 */

internal class TextExtraSpaceContainer : FrameLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val view = getChildAt(0) ?: return
        if (view is KoalaEditText) {
            view.measure(widthMeasureSpec, heightMeasureSpec)
            setMeasuredDimension(view.measuredWidth, view.measuredHeight - view.calculateExtraSpace())
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        getChildAt(0)?.layout(0, 0, measuredWidth, measuredHeight)
    }
}
