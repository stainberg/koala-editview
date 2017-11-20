package com.stainberg.keditview

import android.content.Context
import android.graphics.Rect
import android.support.v7.widget.AppCompatEditText
import android.util.AttributeSet
import android.widget.TextView


/**
 * Created by Stainberg on 8/2/17.
 */

class KoalaEditText : AppCompatEditText {

    private var changedListener: OnSelectionChangedListener? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    fun setSelectionListener(l: OnSelectionChangedListener) {
        changedListener = l
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        changedListener?.onSelectionChanged(selStart, selEnd)
    }

    private val offset = context.dp2px(1f)
    private var firstInit = true
    fun calculateExtraSpace(): Int {
        var result = 0
        val lastLineIndex = lineCount - 1
        if (lastLineIndex >= 0) {
            val layout = layout
            val mRect = Rect()
            val baseline = getLineBounds(lastLineIndex, mRect)
            if (measuredHeight == getLayout().height) {
                result = mRect.bottom - (baseline + layout.paint.fontMetricsInt.descent)
            }
        }
        if (firstInit) {
            firstInit = false
            try {
                val cursorField = TextView::class.java.getDeclaredField("mCursorDrawableRes")
                cursorField.isAccessible = true
                if (result > offset) {
                    cursorField.set(this, R.drawable.text_cursor_no_reduce_padding)
                } else {
                    cursorField.set(this, R.drawable.text_cursor_normal)
                }
            } catch (e: Exception) {
            }
        }
        return result
    }

    override fun scrollBy(x: Int, y: Int) {
    }

    override fun scrollTo(x: Int, y: Int) {
    }

    companion object {
        interface OnSelectionChangedListener {
            fun onSelectionChanged(selStart: Int, selEnd: Int)
        }
    }
}
