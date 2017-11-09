package com.stainberg.keditview

import android.animation.*
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

internal class EditorContainer : LinearLayout , View.OnTouchListener {
    constructor(context : Context?) : super(context)
    constructor(context : Context? , attrs : AttributeSet?) : super(context , attrs)
    constructor(context : Context? , attrs : AttributeSet? , defStyleAttr : Int) : super(context , attrs , defStyleAttr)
    constructor(context : Context? , attrs : AttributeSet? , defStyleAttr : Int , defStyleRes : Int) : super(context , attrs , defStyleAttr , defStyleRes)

    init {
        setOnTouchListener(this)
    }

    private var selectedView : View? = null
    private var downPoint : MotionEvent? = null
    private val gd = GestureDetector(context , GestureDetector.SimpleOnGestureListener())
    private val offset = context.dp2px(40f)
    private var topViewOffset = 0
    private var bottomViewOffset = 0
    private var isDragEnabled = true

    private fun getLocationView(ev : MotionEvent) : View? {
        return (0 until childCount)
                .map { getChildAt(it) }
                .firstOrNull { isTouchPointInView(it , ev.rawX.toInt() , ev.rawY.toInt()) }
    }

    private fun getParentScrollY() : Int {
        return (parent as NestedScrollView).scrollY
    }

    private fun checkMoveIfNeeded(ev : MotionEvent) {
        val scrollView = (parent as NestedScrollView?) ?: return
        val location = intArrayOf(0 , 0)
        scrollView.getLocationOnScreen(location)
        val top = location[1]
        val bottom = top + scrollView.height
        val y = ev.rawY
        Log.e("AAA" , "y=$y top=$top bottom=$bottom offset=$offset topViewOffset=$topViewOffset bottomViewOffset=$bottomViewOffset")
        if (y - topViewOffset - offset < top) {
            val offset = (Math.abs(y - topViewOffset - offset - top)).toInt()
            scrollView.fling(-offset)
        } else if (y + bottomViewOffset + offset > bottom) {
            val offset = (Math.abs(y + bottomViewOffset + offset - bottom)).toInt()
            scrollView.fling(offset)
        }
    }

    private fun swapIfNeeded(ev : MotionEvent) {
        val currentView = selectedView ?: return
        val position = indexOfChild(currentView)
        var isSwapped = false
        val currentCenter = ev.y - topViewOffset + currentView.height / 2
        val location = intArrayOf(0 , 0)
        if (position > 0) {
            val pre = getChildAt(position - 1)
            pre.getLocationOnScreen(location)
            val preCenter = location[1] + pre.height / 2
            if (currentCenter < preCenter) {
                isSwapped = true
                Log.e("AAA" , "<<<<<< position=$position currentCenter=$currentCenter preCenter=$preCenter")
                swap(pre , position - 1 , currentView , false)
            }
        }
        if (!isSwapped && position < childCount - 1) {
            val next = getChildAt(position + 1)
            next.getLocationOnScreen(location)
            val nextCenter = location[1] + next.height / 2
            if (currentCenter > nextCenter) {
                Log.e("AAA" , ">>>>>> position=$position currentCenter=$currentCenter preCenter=$nextCenter")
                swap(currentView , position , next , true)
            }
        }
    }

    private fun swap(first : View , firstIndex : Int , second : View , isSwapAfter : Boolean) {
        val firstHeight = first.height.toFloat()
        val secondHeight = second.height.toFloat()
        removeView(first)
        removeView(second)
        addView(first , firstIndex)
        addView(second , firstIndex)
        var anim : Animation
        if (isSwapAfter) {
            anim = TranslateAnimation(0f , 0f , firstHeight , 0f)
            anim.duration = 100
            second.startAnimation(anim)
        } else {
            anim = TranslateAnimation(0f , 0f , -secondHeight , 0f)
            anim.duration = 100
            first.startAnimation(anim)
        }
    }

    fun setDragEnabled(enable : Boolean) {
        isDragEnabled = enable
    }

    private fun isTouchPointInView(view : View? , x : Int , y : Int) : Boolean {
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

    private var tf : SoftReference<FrameViewContainer?> = SoftReference(null)
    override fun onTouch(view : View? , ev : MotionEvent?) : Boolean {
        if (null == view || null == ev) return super.onTouchEvent(ev)
        if (tf.get() == null) {
            tf = SoftReference(parent.parent as FrameViewContainer)
        }
        if (isDragEnabled) {
            gd.onTouchEvent(ev)
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (downPoint != null) {
                        downPoint?.recycle()
                    }
                    downPoint = MotionEvent.obtain(ev)
                    selectedView = getLocationView(ev)
                    if (selectedView != null) {
                        maxHeight = selectedView!!.layoutParams.height
                        val location = intArrayOf(0 , 0)
                        (parent as View).getLocationOnScreen(location)
                        val top = selectedView!!.top - getParentScrollY()
                        val tf = tf.get()!!
                        tf.initStartOffset(MotionEvent.obtain(ev) , top)
                        tf.initFloatingView(selectedView!!)
                    }
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!isLongPressing()) {
                        parent?.requestDisallowInterceptTouchEvent(false)
                    } else {
                        tf.get()!!.setCurrentPosition(MotionEvent.obtain(ev))
                    }
                }
                MotionEvent.ACTION_CANCEL ,
                MotionEvent.ACTION_UP -> {
                    tf.get()!!.destroyFloatingView()
                    downPoint?.recycle()
                    downPoint = null
                    parent?.requestDisallowInterceptTouchEvent(false)
                }
                else -> {
                }
            }
            if (isInLongPress()) {
                if (selectedView?.layoutParams?.height ?: 0 == maxHeight) {
                    smallImage()
                }
                tf.get()!!.refresh()
                dispatchDrag(ev)
                if (selectedView?.visibility != View.INVISIBLE) {
                    selectedView?.visibility = View.INVISIBLE
                }
            } else if ((MotionEvent.ACTION_UP == ev.action) or (MotionEvent.ACTION_CANCEL == ev.action)) {
                forceLargeImage()
                maxHeight = 0
                tf.get()!!.refresh()
                dispatchDrag(ev)
                if (selectedView?.visibility != View.VISIBLE) {
                    selectedView?.visibility = View.VISIBLE
                }
            }
            return true
        } else {
            return super.onTouchEvent(ev)
        }
    }

    private fun forceLargeImage() {
        clearSmallAnim()
        largeImage()
    }

    private fun clearSmallAnim() {
        smallAnim?.cancel()
        smallAnim?.removeAllListeners()
        smallAnim?.removeAllUpdateListeners()
    }

    private var isImgSmalling = false
    private var isImgLarging = false
    private var minHeight = context.dp2px(50f).toInt()
    private var maxHeight = 0
    private var smallAnim : ObjectAnimator? = null
    private var largeAnim : ObjectAnimator? = null

    private fun smallImage() {
        if (maxHeight == 0) return
        smallAnim?.let { clearSmallAnim() }
        val currentView = selectedView ?: return
        smallAnim = ObjectAnimator.ofInt(ObjAnim(SoftReference(currentView)) , "animValue" , currentView.layoutParams.height , minHeight).setDuration(50)
        smallAnim!!.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation : Animator?) {
            }

            override fun onAnimationEnd(animation : Animator?) {
                isImgSmalling = false
            }

            override fun onAnimationCancel(animation : Animator?) {
                isImgSmalling = false
            }

            override fun onAnimationStart(animation : Animator?) {
                isImgSmalling = true
            }
        })
        smallAnim!!.addUpdateListener {
            if (tf.get() == null) {
                tf = SoftReference(parent.parent as FrameViewContainer)
            }
            tf.get()!!.initFloatingView(currentView)
        }
        smallAnim!!.start()
    }

    private fun largeImage() {
        if (maxHeight == 0) return
        val currentView = selectedView ?: return
        largeAnim = ObjectAnimator.ofInt(ObjAnim(SoftReference(currentView)) , "animValue" , currentView.layoutParams.height , maxHeight).setDuration(50)
        largeAnim!!.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation : Animator?) {
            }

            override fun onAnimationEnd(animation : Animator?) {
                isImgLarging = false
                if (tf.get() == null) {
                    tf = SoftReference(parent.parent as FrameViewContainer)
                }
                tf.get()!!.destroyFloatingView()
            }

            override fun onAnimationCancel(animation : Animator?) {
                isImgLarging = false
            }

            override fun onAnimationStart(animation : Animator?) {
                isImgLarging = true
            }
        })
        largeAnim!!.start()
    }

    private class ObjAnim(view : SoftReference<View?>) {
        private val sr = view
        fun setAnimValue(x : Int) {
            val p = sr.get()?.layoutParams ?: return
            p.height = x
            sr.get()?.layoutParams = p
        }
    }

    private fun isLongPressing() : Boolean {
        val method = gd::class.java.getDeclaredField("mAlwaysInTapRegion")
        method.isAccessible = true
        val result = method.getBoolean(gd)
        method.isAccessible = false
        return result
    }

    private fun isInLongPress() : Boolean {
        val method = gd::class.java.getDeclaredField("mInLongPress")
        method.isAccessible = true
        val result = method.getBoolean(gd)
        method.isAccessible = false
        return result
    }

    private fun dispatchDrag(ev : MotionEvent) {
        post {
            checkMoveIfNeeded(ev)
            swapIfNeeded(ev)
        }
    }
}
