package com.stainberg.keditview

import android.content.Context
import android.support.v7.widget.AppCompatEditText
import android.util.AttributeSet

/**
 * Created by Stainberg on 8/2/17.
 */

internal class KoalaEditText : AppCompatEditText {

    private var changedListener : OnSelectionChangedListener? = null

    constructor(context : Context) : super(context) {}

    constructor(context : Context , attrs : AttributeSet) : super(context , attrs) {}

    constructor(context : Context , attrs : AttributeSet , defStyleAttr : Int) : super(context , attrs , defStyleAttr) {}

    constructor(context : Context , l : OnSelectionChangedListener) : super(context) {
        changedListener = l
    }

    override fun onSelectionChanged(selStart : Int , selEnd : Int) {
        super.onSelectionChanged(selStart , selEnd)
        changedListener?.onSelectionChanged(selStart , selEnd)
    }

    internal interface OnSelectionChangedListener {
        fun onSelectionChanged(selStart : Int , selEnd : Int)
    }
}
