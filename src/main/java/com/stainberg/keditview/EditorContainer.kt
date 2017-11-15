package com.stainberg.keditview

import android.content.*
import android.support.v4.widget.*
import android.util.*
import android.view.*
import android.view.animation.*
import android.widget.*
import java.lang.ref.*

/**
 * Created by Lynn.
 */

internal class EditorContainer : LinearLayout, View.OnTouchListener {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        setOnTouchListener(this)
    }

    private var selectedView: View? = null
    private var downPoint: MotionEvent? = null
    private val gd = GestureDetector(context, GestureDetector.SimpleOnGestureListener())
    private val offset = context.dp2px(40f)
    private var topViewOffset = 0
    private var bottomViewOffset = 0
    private var isDragEnabled = true

    private fun getLocationView(ev: MotionEvent): View? {
        return (0 until childCount)
                .map { getChildAt(it) }
                .firstOrNull { isTouchPointInView(it, ev.rawX.toInt(), ev.rawY.toInt()) }
    }

    private fun getParentScrollY(): Int {
        return (parent as NestedScrollView).scrollY
    }

    private fun checkMoveIfNeeded(ev: MotionEvent) {
        val scrollView = (parent as NestedScrollView?) ?: return
        val location = intArrayOf(0, 0)
        scrollView.getLocationOnScreen(location)
        val top = location[1]
        val bottom = top + scrollView.height
        val y = ev.rawY
        if (y - topViewOffset - offset < top) {
            val offset = (Math.abs(y - topViewOffset - offset - top)).toInt()
            scrollView.fling(-offset)
        } else if (y + bottomViewOffset + offset > bottom) {
            val offset = (Math.abs(y + bottomViewOffset + offset - bottom)).toInt()
            scrollView.fling(offset)
        }
    }

    private fun swapIfNeeded(ev: MotionEvent) {
        val currentView = selectedView ?: return
        val position = indexOfChild(currentView)
        var isSwapped = false
        val currentCenter = ev.y - topViewOffset + currentView.height / 2
        val location = intArrayOf(0, 0)
        if (position > 0) {
            val pre = getChildAt(position - 1)
            pre.getLocationOnScreen(location)
            val preCenter = location[1] + pre.height / 2
            if (currentCenter < preCenter) {
                isSwapped = true
                swap(pre, position - 1, currentView, false)
            }
        }
        if (!isSwapped && position < childCount - 1) {
            val next = getChildAt(position + 1)
            next.getLocationOnScreen(location)
            val nextCenter = location[1] + next.height / 2
            if (currentCenter > nextCenter) {
                swap(currentView, position, next, true)
            }
        }
    }

    private fun swap(first: View, firstIndex: Int, second: View, isSwapAfter: Boolean) {
        val firstHeight = first.height.toFloat()
        val secondHeight = second.height.toFloat()
        removeView(first)
        removeView(second)
        addView(first, firstIndex)
        addView(second, firstIndex)
        var anim: Animation
        if (isSwapAfter) {
            anim = TranslateAnimation(0f, 0f, firstHeight, 0f)
            anim.duration = 100
            second.startAnimation(anim)
        } else {
            anim = TranslateAnimation(0f, 0f, -secondHeight, 0f)
            anim.duration = 100
            first.startAnimation(anim)
        }
    }

    fun setDragEnabled(enable: Boolean) {
        isDragEnabled = enable
    }

    private fun isTouchPointInView(view: View?, x: Int, y: Int): Boolean {
        if (view == null) {
            return false
        }
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val left = location[0]
        val top = location[1]
        val right = left + view.measuredWidth
        val bottom = top + view.measuredHeight
        val flag = (y in top..bottom && x in left..right)
        if (flag) {
            topViewOffset = y - location[1]
            val bvo = minHeight - topViewOffset
            bottomViewOffset = if (bvo < 0) 0 else bvo
        }
        return flag
    }

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        child?.setOnClickListener { }
    }

    private var tf: SoftReference<FrameViewContainer?> = SoftReference(null)
    override fun onTouch(view: View?, ev: MotionEvent?): Boolean {
        val flag = super.onTouchEvent(ev)
        if (null == view || null == ev) return flag
        if (tf.get() == null) {
            tf = SoftReference(parent.parent as FrameViewContainer)
        }
        if (isDragEnabled && !flag) {
            gd.onTouchEvent(ev)
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (downPoint != null) {
                        downPoint?.recycle()
                    }
                    downPoint = MotionEvent.obtain(ev)
                    selectedView = getLocationView(ev)
                    if (selectedView != null) {
                        val top = selectedView!!.top - getParentScrollY()
                        val tf = tf.get()!!
                        tf.initStartOffset(MotionEvent.obtain(ev), top)
                        initSize()
                        smallImage()
                    }
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_MOVE -> {
                    tf.get()!!.setCurrentPosition(MotionEvent.obtain(ev))
                    tf.get()!!.refresh()
                    dispatchDrag(ev)
                }
                MotionEvent.ACTION_CANCEL,
                MotionEvent.ACTION_UP -> {
                    tf.get()!!.destroyFloatingView()
                    downPoint?.recycle()
                    downPoint = null
                    largeImage()
                    tf.get()!!.refresh()
                    dispatchDrag(ev)
                    if (selectedView?.visibility != View.VISIBLE) {
                        selectedView?.visibility = View.VISIBLE
                    }
                    parent?.requestDisallowInterceptTouchEvent(false)
                }
                else -> {
                }
            }
            return true
        } else {
            return flag
        }
    }

    private fun initSize() {
        val currentView = selectedView ?: return
        val content = currentView.findViewById<View>(R.id.content_bg) ?: return
        maxHeight = content.layoutParams.height
        minHeight = if (content.bottom - content.top > fixedMinHeight) fixedMinHeight else (content.bottom - content.top)
    }

    private var fixedMinHeight = context.dp2px(68f).toInt()
    private var minHeight = 0
    private var maxHeight = 0

    private fun smallImage() {
        if (maxHeight == 0) return
        val currentView = selectedView ?: return
        val content = currentView.findViewById<View>(R.id.content_bg) ?: return
        val lp = content.layoutParams
        lp.height = minHeight
        content.layoutParams = lp
        when (currentView) {
            is KoalaImageView -> {
                currentView.actionDown()
            }
            is KoalaFileView -> {
                currentView.actionDown()
            }
            is KoalaEditTextView -> {
                currentView.actionDown()
            }
        }
        content.requestLayout()
        content.post {
            tf.get()!!.initFloatingView(selectedView!!)
            if (selectedView?.visibility != View.INVISIBLE) {
                selectedView?.visibility = View.INVISIBLE
            }
        }
    }

    private fun largeImage() {
        if (maxHeight == 0) return
        val currentView = selectedView ?: return
        val content = currentView.findViewById<View>(R.id.content_bg) ?: return
        val lp = content.layoutParams
        lp.height = maxHeight
        content.layoutParams = lp
        when (currentView) {
            is KoalaImageView -> {
                currentView.actionUp()
            }
            is KoalaFileView -> {
                currentView.actionUp()
            }
            is KoalaEditTextView -> {
                currentView.actionUp()
            }
        }
        content.requestLayout()
        maxHeight = 0
    }

    private fun dispatchDrag(ev: MotionEvent) {
        post {
            checkMoveIfNeeded(ev)
            swapIfNeeded(ev)
        }
    }

    companion object {
        class ObjAnim(val sr: SoftReference<View>) {
            fun setValue(x: Int) {
                val lp = sr.get()?.layoutParams
                lp?.height = x
                sr.get()?.layoutParams = lp
            }
        }
    }
}
