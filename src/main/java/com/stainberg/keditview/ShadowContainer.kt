package com.stainberg.keditview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout

/**
 * Created by Lynn.
 */

internal class ShadowContainer : RelativeLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    private var paint = Paint()
    private var rects = RectF(0f, 0f, 0f, 0f)
    private val shadowColor = Color.parseColor("#12000000")
    private val highLightColor = Color.parseColor("#50000000")

    init {
        paint.color = Color.WHITE
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        paint.isAntiAlias = true
    }

    private val radius = context.dp2px(5f)
    private val offset = context.dp2px(1f)
    override fun dispatchDraw(canvas: Canvas?) {
        if (showShadow) {
            val child = getChildAt(0)
            child?.let {
                val shadowColor = if (highLight) {
                    highLightColor
                } else shadowColor
                paint.setShadowLayer(radius, offset, offset, shadowColor)
                rects.set(child.left.toFloat() + offset, child.top.toFloat() + offset, child.right.toFloat() - offset, child.bottom.toFloat() - offset)
                canvas?.drawRoundRect(rects, radius, radius, paint)
            }
        }
        super.dispatchDraw(canvas)
    }

    private var showShadow = false
    fun showShadow(show: Boolean) {
        this.showShadow = show
        invalidate()
    }

    private var highLight = false
    fun showHighLight(highLight: Boolean) {
        this.highLight = highLight
        invalidate()
    }

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        child?.setLayerType(View.LAYER_TYPE_NONE, null)
    }
}
