package com.stainberg.keditview

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

/**
 * Created by Stainberg on 7/5/17.
 */

class KoalaSliderView : FrameLayout , KoalaBaseCellView {

    constructor(context : Context) : super(context) {
        init(context)
    }

    constructor(context : Context , attrs : AttributeSet?) : super(context , attrs) {
        init(context)
    }

    constructor(context : Context , attrs : AttributeSet? , defStyleAttr : Int) : super(context , attrs , defStyleAttr) {
        init(context)
    }

    private fun init(context : Context) {
        val v = View(context)
        val lp = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , resources.getDimension(R.dimen.slider_heigh).toInt())
        v.setBackgroundColor(ContextCompat.getColor(getContext() , R.color.gray_placeholder_bg))
        addView(v , lp)
    }

    @Deprecated("")
    override fun reload() {

    }

    override fun enableDrag(enable : Boolean) {

    }

    override fun release() {

    }
}
