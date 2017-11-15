package com.stainberg.keditview

import android.view.View
import java.lang.ref.SoftReference

/**
 * Created by Lynn.
 */

internal class PaddingAnim(view: View) {
    val sr = SoftReference(view)
    fun setPadding(padding: Int) {
        sr.get()?.setPadding(padding, padding, padding, padding)
    }
}
