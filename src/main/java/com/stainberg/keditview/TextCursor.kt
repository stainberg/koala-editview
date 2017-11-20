package com.stainberg.keditview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * Created by Lynn.
 */

internal class TextCursor : View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    private val paint = Paint()
    private val colorVisible = context.resources.getColor(R.color.color_FF0458)

    init {
        paint.color = colorVisible
        paint.isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (null == canvas) return
        val tm = (drawingTime % 1000).toInt()
        when (tm) {
            in 300 until 500 -> {
                paint.alpha = (((tm - 300) / 200f) * 255).toInt()
            }
            in 500 until 600 -> {
                if (paint.alpha != 255) {
                    paint.alpha = 255
                }
            }
            in 600 until 800 -> {
                paint.alpha = (((800 - tm) / 200f) * 255).toInt()
            }
            else -> {
                if (paint.alpha != 0) {
                    paint.alpha = 0
                }
            }
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        invalidate()
    }
}
