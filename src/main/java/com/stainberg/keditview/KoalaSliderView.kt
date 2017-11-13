package com.stainberg.keditview

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

/**
 * Created by Stainberg on 7/5/17.
 */

class KoalaSliderView : FrameLayout , KoalaBaseCellView {

    constructor(context : Context) : super(context) {
        init(context)
    }

    constructor(context : Context , attrs : AttributeSet?) : super(context , attrs) {
        init(context)
    }

    constructor(context : Context , attrs : AttributeSet? , defStyleAttr : Int) : super(context , attrs , defStyleAttr) {
        init(context)
    }

    private fun init(context : Context) {
        val v = View(context)
        val lp = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , resources.getDimension(R.dimen.slider_heigh).toInt())
        v.setBackgroundColor(ContextCompat.getColor(getContext() , R.color.gray_placeholder_bg))
        addView(v , lp)
    }

    @Deprecated("")
    override fun obtainUrl() : String {
        return ""
    }

    @Deprecated("")
    override fun reload() {

    }

    @Deprecated("")
    override fun setStyleH1() {

    }

    @Deprecated("")
    override fun setStyleH2() {

    }

    @Deprecated("")
    override fun setStyleNormal() {

    }

    @Deprecated("")
    override fun setGravity() {

    }

    @Deprecated("")
    override fun setQuote() {

    }

    @Deprecated("")
    override fun setSection(st : Int) {

    }

    @Deprecated("")
    override fun setBold() {

    }

    @Deprecated("")
    override fun setItalic() {

    }

    @Deprecated("")
    override fun setStrike() {

    }

    @Deprecated("")
    override fun addCode() {

    }

    @Deprecated("")
    override fun ifQuote() : Boolean {
        return false
    }

    @Deprecated("")
    override fun ifCode() : Boolean {
        return false
    }

    override fun obtainStyle() : Int {
        return 0
    }

    override fun obtainSection() : Int {
        return 0
    }

    override fun setEditable(enable : Boolean) {

    }

    override fun enableDrag(enable : Boolean) {

    }

    override fun release() {

    }

    @Deprecated("")
    override fun setText(sequence : CharSequence) {

    }

    @Deprecated("")
    override fun obtainText() : CharSequence {
        return ""
    }

    @Deprecated("")
    override fun obtainHtmlText() : List<String> {
        return emptyList()
    }

    override fun setHtmlText(html : String) {

    }
}
