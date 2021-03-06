package com.stainberg.keditview

import android.content.*
import android.graphics.Rect
import android.support.v7.widget.*
import android.view.*
import android.view.inputmethod.InputMethodManager
import java.lang.ref.SoftReference

/**
 * Created by Lynn.
 */

internal fun Context.dp2px(dp: Float): Float {
    val scale = resources.displayMetrics.density
    return dp * scale + 0.5f
}

internal val Context.screenHeight: Int
    get() = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.height
internal val Context.screenWidth: Int
    get() = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.width

internal fun eventInView(ev: MotionEvent, view: View): Boolean {
    return (ev.x > view.left) and (ev.x < view.right) and (ev.y > view.top) and (ev.y < view.bottom)
}

internal val animTime = 100L

internal fun enableCard(ctx: Context, cardView: CardView, enable: Boolean) {
//    val lp = cardContainer.layoutParams as ViewGroup.MarginLayoutParams
    val offset = ctx.dp2px(4f)
    if (enable) {
//        lp.topMargin = 0
        cardView.cardElevation = offset
    } else {
//        lp.topMargin = offset.toInt()
//        lp.bottomMargin = offset.toInt()
        cardView.cardElevation = 0f
    }
//    cardContainer.layoutParams = lp
}

internal class PaddingAnim(view: View) {
    val sr = SoftReference(view)
    fun setPadding(padding: Int) {
        sr.get()?.setPadding(padding, padding, padding, padding)
    }
}

internal class HeightAnim(view: View) {
    val sr = SoftReference(view)
    fun setX(x: Int) {
        val lp = sr.get()?.layoutParams
        lp?.height = x
        sr.get()?.layoutParams = lp
    }
}

internal fun View.showSoft() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val view = rootView?.findFocus()
    view?.let { imm.showSoftInput(view, InputMethodManager.SHOW_FORCED) }
}

internal fun View.hideSoft() {
    val im = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (im.isActive) {
        rootView?.findFocus()?.windowToken?.let { im.hideSoftInputFromWindow(rootView.findFocus().windowToken, 0) }
    }
}

internal fun getLocation(view: View): IntArray {
    val location = IntArray(2)
    view.getLocationOnScreen(location)
    if (location[1] == 0) {
        val rect = Rect()
        view.getGlobalVisibleRect(rect)
        location[1] = rect.top
    }
    return location
}

internal val View.MARGIN_3
    get() = context.dp2px(3f).toInt()
internal val View.MARGIN_4
    get() = context.dp2px(4f).toInt()
internal val View.MARGIN_5
    get() = context.dp2px(5f).toInt()
internal val View.MARGIN_9
    get() = context.dp2px(9f).toInt()