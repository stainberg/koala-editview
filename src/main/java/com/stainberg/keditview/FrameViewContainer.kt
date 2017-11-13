package com.stainberg.keditview

import android.content.*
import android.graphics.*
import android.graphics.drawable.*
import android.os.*
import android.support.annotation.*
import android.support.v7.widget.*
import android.util.*
import android.view.*
import android.widget.*
import android.graphics.Bitmap
import android.renderscript.*
import android.renderscript.Allocation
import android.R.attr.radius
import android.renderscript.ScriptIntrinsicBlur
import kotlinx.android.synthetic.main.item_view_image.view.*


/**
 * Created by Lynn.
 */

internal class FrameViewContainer : FrameLayout {
    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    private var floatingView: ImageView? = null
    private var viewTop = 0
    private var downPoint: MotionEvent? = null
    private var currentEvent: MotionEvent? = null
    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        if (canvas == null) {
            return
        }
        floatingView?.let {
            val currentY = currentEvent?.rawY ?: 0f
            val downY = downPoint?.rawY ?: 0f
            val status = canvas.save()
            val top = viewTop
            canvas.translate(0f, top + ((currentY - downY)))
            floatingView?.draw(canvas)
            canvas.restoreToCount(status)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        destroyFloatingView()
        destroyPoint()
    }

    fun destroyFloatingView() {
        val drawable = floatingView?.drawable
        drawable?.let {
            if (drawable is BitmapDrawable) {
                drawable.bitmap.recycle()
            }
        }
        floatingView = null
    }

    fun refresh() {
        invalidate()
    }

    fun initStartOffset(ev: MotionEvent, top: Int) {
        destroyPoint()
        downPoint = ev
        viewTop = top
    }

    private fun destroyPoint() {
        downPoint?.let { downPoint?.recycle() }
    }

    fun setCurrentPosition(event: MotionEvent?) {
        currentEvent?.let { currentEvent?.recycle() }
        currentEvent = event
    }

    fun initFloatingView(view: View, minHeight: Int) {
        destroyFloatingView()
        view.isDrawingCacheEnabled = true
        val minHeight = if (view.height > minHeight) minHeight else view.height
        val bmp = if (view.height > minHeight) {
            val startY = (view.height - minHeight) / 2
            Bitmap.createBitmap(view.drawingCache, 0, startY, view.width, minHeight)
        } else {
            Bitmap.createBitmap(view.drawingCache)
        }
        view.isDrawingCacheEnabled = false
        val img = AppCompatImageView(context)
        img.setPadding(0, 0, 0, 0)
        img.setBackgroundColor(Color.TRANSPARENT)
        img.setImageBitmap(bmp)
        img.scaleType = ImageView.ScaleType.CENTER
        img.layoutParams = ViewGroup.LayoutParams(view.width, minHeight)
        img.measure(view.width, minHeight)
        img.layout(0, 0, view.width, minHeight)
        floatingView = img
    }
}
