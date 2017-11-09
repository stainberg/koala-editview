package com.stainberg.keditview

import android.content.*
import android.view.*

/**
 * Created by Lynn.
 */

fun Context.dp2px(dp : Float) : Float {
    val scale = resources.displayMetrics.density
    return dp * scale + 0.5f
}

fun eventInView(ev : MotionEvent , view : View) : Boolean {
    return (ev.x > view.left) and (ev.x < view.right) and (ev.y > view.top) and (ev.y < view.bottom)
}
