package com.stainberg.keditview

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.TextWatcher
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.item_view_edit_text.view.*

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.xml.sax.XMLReader

import java.util.ArrayList

/**
 * Created by Stainberg on 7/5/17.
 */

class KoalaEditTextView : FrameLayout, KoalaBaseCellView {
    private var listener: OnEditListener? = null
    private var statusListener: OnEditTextStatusListener? = null
    private var onHintSetListener: OnHintSetListener? = null
    internal var style: Int = 0
    var gravity: Int = 0
    internal var section: Int = 0
    private var quote: Boolean = false
    private var code: Boolean = false
    private var singleHeight = 0
    private var sectionIndex = 0
    private var showHint = true
    private var hasFocus = false
    private var isDragging = false
    private var dragTouchToggled = false

    val selectionStart: Int
        get() = edit_text.selectionStart

    private val keyListener = OnKeyListener { _, keyCode, event ->
        if (keyCode == KeyEvent.KEYCODE_BACK || KeyEvent.isModifierKey(keyCode)) {
            false
        } else {
            val start = edit_text.selectionStart
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                if (listener != null) {
                    if (code) {
                        if (edit_text.selectionEnd == edit_text.text.length && edit_text.text.toString()[edit_text.text.toString().length - 1] == '\n') {
                            edit_text.setText(edit_text.text.subSequence(0, edit_text.text.toString().length - 1))
                            edit_text.setSelection(edit_text.length())
                            listener!!.pressEnter(this@KoalaEditTextView)
                            return@OnKeyListener true
                        }
                        return@OnKeyListener false
                    }
                    if (section == SECTION_NULL) {
                        if (!quote) {
                            listener!!.pressEnter(this@KoalaEditTextView)
                            return@OnKeyListener true
                        } else {
                            if (edit_text.selectionEnd == edit_text.text.length && edit_text.text.toString()[edit_text.text.toString().length - 1] == '\n') {
                                edit_text.setText(edit_text.text.subSequence(0, edit_text.text.toString().length - 1))
                                edit_text.setSelection(edit_text.length())
                                listener!!.pressEnter(this@KoalaEditTextView)
                                return@OnKeyListener true
                            }
                            if (edit_text.selectionStart == edit_text.selectionEnd && edit_text.selectionStart == 1
                                    && edit_text.text.toString()[0] == '\n') {
                                edit_text.setText(edit_text.text.subSequence(1, edit_text.text.toString().length))
                                listener!!.insertEdit(this@KoalaEditTextView)
                                return@OnKeyListener true
                            }
                        }
                    } else {
                        if (TextUtils.isEmpty(edit_text.text.toString().trim { it <= ' ' })) {
                            cleanSection(this@KoalaEditTextView)
                            resetNextSection(this@KoalaEditTextView)
                            return@OnKeyListener true
                        } else {
                            listener!!.pressEnter(this@KoalaEditTextView)
                            return@OnKeyListener true
                        }
                    }
                    return@OnKeyListener false
                }
            } else if (start == 0 && keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_UP) {
                val prev = KoalaRichEditorView.getNext(parent as ViewGroup, this@KoalaEditTextView)
                if (quote) {
                    if (prev == null) {
                        cleanAllQuote()
                    } else {
                        if (prev !is KoalaEditTextView) {
                            cleanAllQuote()
                        } else {
                            if (!prev.quote) {
                                cleanAllQuote()
                            }
                        }
                    }
                } else if (section != SECTION_NULL) {
                    if (prev == null) {
                        cleanSection(this@KoalaEditTextView)
                        resetNextSection(this@KoalaEditTextView)
                    } else {
                        if (prev !is KoalaEditTextView) {
                            cleanSection(this@KoalaEditTextView)
                            resetNextSection(this@KoalaEditTextView)
                        } else {
                            if (prev.section != section) {
                                cleanSection(this@KoalaEditTextView)
                                resetNextSection(this@KoalaEditTextView)
                            }
                        }
                    }
                }
                if (listener != null) {
                    listener!!.deleteSelf(this@KoalaEditTextView)
                }
            }
            if (KeyEvent.ACTION_DOWN == event.action && KeyEvent.KEYCODE_DEL == keyCode) {
                selectionStart == 0
            } else {
                true
            }
        }
    }

    private val textFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
        if (statusListener != null && v === edit_text) {
            this.hasFocus = hasFocus
            if (hasFocus) {
                notifyStatusChanged()
            }
        }
    }

    private val onSelectionChangedListener = object : KoalaEditText.Companion.OnSelectionChangedListener {

        override fun onSelectionChanged(selStart: Int, selEnd: Int) {
            if (statusListener != null) {
                var s = 0
                if (obtainStyle() == STYLE_H1) {
                    s = s or S_H1
                } else if (obtainStyle() == STYLE_H2) {
                    s = s or S_H2
                }
                var bold = false
                val ssb = edit_text.text
                val spans = ssb.getSpans(selStart, selEnd, StyleSpan::class.java)
                for (span in spans) {
                    if (span.style == Typeface.BOLD) {
                        val ss = ssb.getSpanStart(span)
                        val se = ssb.getSpanEnd(span)
                        if (selStart >= ss && (selStart < se || selStart == ssb.length && selStart == se)) {
                            bold = true
                            break
                        }
                    }
                }
                if (bold) {
                    s = s or S_B
                }
                if (gravity != Gravity.LEFT) {
                    s = s or S_G
                }
                if (quote) {
                    s = s or S_Q
                }
                if (section != 0) {
                    s = s or S_L
                }
                statusListener!!.onEditStatus(s)
            }
        }
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    constructor(context: Context, l: OnEditListener, sl: OnEditTextStatusListener) : super(context) {
        listener = l
        statusListener = sl
        init(context)
    }

    constructor(context: Context, l: OnEditListener, sl: OnEditTextStatusListener, hl: OnHintSetListener) : super(context) {
        listener = l
        statusListener = sl
        onHintSetListener = hl
        init(context)
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable) {
            notifyStatusChanged()
            if (s.length == 0) {
                showHint = true
                onHintSetListener?.onHintChanged()
            } else {
                if (showHint) {
                    showHint = false
                    onHintSetListener?.onHintChanged()
                }
            }
        }
    }

    private fun init(context: Context) {
        style = STYLE_NORMAL
        gravity = Gravity.LEFT
        section = SECTION_NULL
        quote = false
        code = false
        sectionIndex = 1
        LayoutInflater.from(context).inflate(R.layout.item_view_edit_text, this, true)
        edit_text.measure(View.MeasureSpec.getMode(0), View.MeasureSpec.getMode(0))
        singleHeight = edit_text.measuredHeight
        setStyleNormal()
        section_text.textSize = resources.getDimension(R.dimen.normal_text)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (edit_icon_drag.visibility == View.VISIBLE) {
            dragTouchToggled = eventInView(ev, edit_icon_drag)
            if (dragTouchToggled) {
                return dragTouchToggled
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (dragTouchToggled) {
            false
        } else super.onTouchEvent(event)
    }

    fun setText(p: String) {
        edit_text.setText(p)
    }

    fun setSelection(selection: Int) {
        edit_text.setSelection(selection)
    }

    fun length(): Int {
        return edit_text.length()
    }

    override fun resetMargin() {
        initMargin()
    }

    private fun initMargin() {
        val pr = parent as? ViewGroup ?: return
        val pre = KoalaRichEditorView.getPrev(pr, this)
        pre?.let {
            var margin = 0
            when (pre) {
                is KoalaImageView, is KoalaFileView -> {
                    margin = MARGIN_3
                }
                is KoalaEditTextView -> {
                    when (pre.style) {
                        STYLE_H1 -> {
                            margin = MARGIN_5
                        }
                        STYLE_H2 -> {
                            margin = MARGIN_4
                        }
                        else -> {
                            margin = if (section != SECTION_NULL) MARGIN_3 else MARGIN_9
                        }
                    }
                }
                else -> {
                    margin = MARGIN_5
                }
            }
            val lp = edit_content_bg.layoutParams as MarginLayoutParams
            if (lp.topMargin != margin) {
                lp.topMargin = margin
                edit_content_bg.layoutParams = lp
            }
        }
    }

    private val offset = context.screenHeight
    private val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        val height = edit_container.height
        if (height != 0) {
            val lp = edit_space.layoutParams
            if (lp.height != height) {
//                originalHeight = height
                lp.height = height
                edit_space.layoutParams = lp
            }
        }
    }
    private val scrollChangedListener = ViewTreeObserver.OnScrollChangedListener {
        val location = IntArray(2)
        getLocationInWindow(location)
        val y = location[1]
        if (y > offset * 2 || (y + height < -offset)) {
            if (edit_container.visibility != View.GONE && !hasFocus) {
                edit_container.visibility = View.GONE
            }
        } else {
            if (edit_container.visibility != View.VISIBLE) {
                edit_container.visibility = View.VISIBLE
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        edit_text.setOnKeyListener(keyListener)
        if (onHintSetListener != null) {
            edit_text.addTextChangedListener(textWatcher)
        }
        edit_text.setSelectionListener(onSelectionChangedListener)
        edit_text.onFocusChangeListener = textFocusChangeListener
        viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
        viewTreeObserver.addOnScrollChangedListener(scrollChangedListener)
        initMargin()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        edit_text.setOnKeyListener(null)
        edit_text.removeTextChangedListener(textWatcher)
        edit_text.setSelectionListener(null)
        edit_text.onFocusChangeListener = null
        viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
        viewTreeObserver.removeOnScrollChangedListener(scrollChangedListener)
    }

    fun setStyleH1() {
        if (quote) {
            cleanQuote(SECTION_NULL, STYLE_H1)
            return
        }
        cleanSection(this@KoalaEditTextView)
        if (style != STYLE_H1) {
            edit_text.textSize = resources.getDimension(R.dimen.large_text)
            edit_text.setTextColor(ContextCompat.getColor(context, R.color.black_text))
            edit_text.paint.typeface = Typeface.DEFAULT_BOLD
            style = STYLE_H1
        } else {
            setStyleNormal()
        }
        notifyStatusChanged()
        initMargin()
    }

    private fun notifyStatusChanged() {
        onSelectionChangedListener.onSelectionChanged(edit_text.selectionStart, edit_text.selectionEnd)
    }

    fun setStyleH2() {
        if (quote) {
            cleanQuote(SECTION_NULL, STYLE_H2)
            return
        }
        cleanSection(this@KoalaEditTextView)
        if (style != STYLE_H2) {
            edit_text.textSize = resources.getDimension(R.dimen.middle_text)
            edit_text.setTextColor(ContextCompat.getColor(context, R.color.black_text))
            edit_text.paint.typeface = Typeface.DEFAULT_BOLD
            style = STYLE_H2
        } else {
            setStyleNormal()
        }
        notifyStatusChanged()
        initMargin()
    }

    fun setStyleNormal() {
        if (quote) {
            cleanQuote(SECTION_NULL, STYLE_NORMAL)
            return
        }
        cleanSection(this@KoalaEditTextView)
        edit_text.textSize = resources.getDimension(R.dimen.normal_text)
        edit_text.paint.typeface = Typeface.DEFAULT
        edit_text.setTextColor(ContextCompat.getColor(context, R.color.gray_text))
        style = STYLE_NORMAL
        initMargin()
    }

    fun append(text: CharSequence, start: Int) {
        edit_text.append(text, 0, text.length)
        edit_text.setSelection(start)
    }

    fun setGravity() {
        if (quote || section != 0) {//不支持居中
            return
        }
        if (gravity == Gravity.LEFT) {//left
            edit_text.gravity = Gravity.CENTER
            gravity = Gravity.CENTER
        } /*else if (gravity == GRAVITY_CENTER) {//middle
            editText.setGravity(Gravity.END);
            gravity = GRAVITY_RIGHT;
        } */
        else {//right
            edit_text.gravity = Gravity.START or Gravity.CENTER_VERTICAL
            gravity = Gravity.LEFT
        }
        notifyStatusChanged()
    }

    fun setQuote() {
        if (quote) {
            val start = edit_text.selectionStart
            val end = edit_text.selectionEnd
            if (start != end) {
                cleanQuote(SECTION_NULL, style)
            } else {
                cleanAllQuote()
            }
        } else {
            markQuote()
        }
        notifyStatusChanged()
        initMargin()
    }

    fun setSection(st: Int) {
        if (quote) {
            cleanQuote(st, style)
            return
        }
        if (gravity != Gravity.LEFT) {
            setGravity()
        }
        if (section == st) {
            cleanSection(this@KoalaEditTextView)
        } else {
            if (st == SECTION_NUMBER) {//normal to No.
                setNumberSection(this@KoalaEditTextView)
            } else if (st == SECTION_DOT) {//No. to dot
                setDotSection(this@KoalaEditTextView)
            } else {//dot to normal
                cleanSection(this@KoalaEditTextView)
            }
        }
        val pr = parent as? ViewGroup
        pr?.let {
            val next = KoalaRichEditorView.getNext(pr, this@KoalaEditTextView)
            if (next != null && next is KoalaEditTextView && next.section != SECTION_NULL) {
                resetNextSection(this@KoalaEditTextView)
            }
        }
        notifyStatusChanged()
        initMargin()
    }

    fun setBold() {
        val start = edit_text.selectionStart
        val end = edit_text.selectionEnd
        if (start < end) {
            var bold = false
            val ssb = SpannableString(edit_text.text)
            val spans = ssb.getSpans(start, end, StyleSpan::class.java)
            for (span in spans) {
                if (span.style == Typeface.BOLD) {
                    if (ssb.getSpanStart(span) == start) {
                        bold = true
                    }
                    ssb.removeSpan(span)
                }
            }
            if (!bold) {
                ssb.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            edit_text.setText(ssb)
            edit_text.setSelection(start, end)
        } else {
            var hasNormal = false
            val s = edit_text.text
            val ssb = SpannableStringBuilder(s)
            val spans = edit_text.editableText.getSpans(0, edit_text.text.length, StyleSpan::class.java)
            if (spans.size == 0) {
                hasNormal = true
            }
            for (sp in spans) {
                if (sp.style == Typeface.NORMAL) {
                    ssb.removeSpan(sp)
                    hasNormal = true
                }
                if (sp.style == Typeface.BOLD) {
                    ssb.removeSpan(sp)
                }
            }
            if (hasNormal) {
                ssb.setSpan(StyleSpan(Typeface.BOLD), 0, s.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else {
                ssb.setSpan(StyleSpan(Typeface.NORMAL), 0, s.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            edit_text.text = ssb
            edit_text.setSelection(start)
        }
        notifyStatusChanged()
    }

    fun setItalic() {
        val start = edit_text.selectionStart
        val end = edit_text.selectionEnd
        if (start < end) {
            var strike = false
            val ssb = SpannableString(edit_text.text)
            val spans = ssb.getSpans(start, end, StyleSpan::class.java)
            for (span in spans) {
                if (span.style == Typeface.ITALIC) {
                    if (ssb.getSpanStart(span) == start) {
                        strike = true
                    }
                    ssb.removeSpan(span)
                }
            }
            if (!strike) {
                ssb.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            edit_text.setText(ssb)
            edit_text.setSelection(start, end)
        }
        notifyStatusChanged()
    }

    fun setStrike() {
        var styleSpan = 0
        var strike = false
        val start = edit_text.selectionStart
        val end = edit_text.selectionEnd
        if (start < end) {
            val t1 = edit_text.text.subSequence(0, start)
            val t2 = edit_text.text.subSequence(start, end)
            val t3 = edit_text.text.subSequence(end, edit_text.length())
            val spans = edit_text.editableText.getSpans(start, end, StyleSpan::class.java)
            val spans2 = edit_text.editableText.getSpans(start, end, StrikethroughSpan::class.java)
            if (spans.size > 0) {
                styleSpan = spans[0].style
            }
            if (spans2.size > 0) {
                strike = true
            }
            val ssb = SpannableStringBuilder(t2)
            ssb.clearSpans()
            ssb.setSpan(StyleSpan(styleSpan), 0, t2.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (!strike) {
                ssb.setSpan(StrikethroughSpan(), 0, t2.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            edit_text.setText(t1)
            edit_text.append(ssb)
            edit_text.append(t3)
            edit_text.setSelection(start, end)
        }
        notifyStatusChanged()
    }

    fun setText(sequence: CharSequence) {
        edit_text.setText(sequence)
    }

    fun obtainText(): CharSequence {
        return edit_text.text
    }

    fun obtainHtmlText(): List<String> {
        val result = ArrayList<String>()
        var s = Html.toHtml(edit_text.text)
        s = s.replace("<strike>".toRegex(), "<del>")
        s = s.replace("</strike>".toRegex(), "</del>")
        if (TextUtils.isEmpty(s)) {
            if (result.isEmpty()) {
                result.add("")
            }
            return result
        }
        if (s.substring(s.length - 1, s.length) == "\n") {
            s = s.substring(0, s.length - 1)
        }
        val doc = Jsoup.parseBodyFragment(s)
        val body = doc.body()
        val allElements = body.children()
        for (i in allElements.indices) {
            val e = allElements[i]
            if (e.tagName() == "p") {
                result.add(e.html())
            } else if (e.tagName() == "div") {
                result.addAll(getText(e))
            }
        }
        if (result.isEmpty()) {
            result.add("")
        }
        return result
    }

    private fun getText(element: Element): List<String> {
        val result = ArrayList<String>()
        val allElements = element.children()
        for (i in allElements.indices) {
            val e = allElements[i]
            if (e.tagName() == "p") {
                result.add(e.html())
            } else if (e.tagName() == "div") {
                result.addAll(getText(e))
            }
        }
        return result
    }

    fun setHint(hint: String) {
        edit_text.hint = hint
    }

    fun setHtmlText(html: String) {
        val spanned = Html.fromHtml(html, null, HtmlTagHandler())
        edit_text.setText(spanned)
    }

    fun ifQuote(): Boolean {
        return quote
    }

    fun ifCode(): Boolean {
        return code
    }

    fun obtainStyle(): Int {
        return style
    }

    fun obtainSection(): Int {
        return section
    }

    fun setTextStyle(syl: Int) {
        if (syl == STYLE_H1) {
            setStyleH1()
        } else if (syl == STYLE_H2) {
            setStyleH2()
        } else {
            setStyleNormal()
        }
    }

    fun setCode() {
        setBackgroundResource(R.drawable.shape_edit_code_bg)
        code = true
        cleanSection(this)
        resetNextSection(this)
    }

    internal fun setNumberSection(v: KoalaEditTextView) {
        if (quote) {
            cleanQuote(SECTION_NUMBER, STYLE_NORMAL)
            return
        }
        v.setStyleNormal()
        if (v.section_text.visibility != View.VISIBLE) {
            v.section_text.visibility = View.VISIBLE
        }
        val prev = KoalaRichEditorView.getPrev(parent as ViewGroup, v)
        if (prev != null) {
            if (prev is KoalaEditTextView) {
                if (prev.section == 1) {
                    if (prev.quote) {
                        v.sectionIndex = 1
                    } else {
                        v.sectionIndex = prev.sectionIndex + 1
                    }
                } else {
                    v.sectionIndex = 1
                }
            }
        }
        v.section = 1
        v.section_text.text = v.sectionIndex.toString() + ". "
        initMargin()
    }

    internal fun setDotSection(v: KoalaEditTextView) {
        if (quote) {
            cleanQuote(SECTION_DOT, STYLE_NORMAL)
            return
        }
        v.setStyleNormal()
        v.section_text.text = "• "
        v.section_text.visibility = View.VISIBLE
        v.sectionIndex = 1
        v.section = 2
        initMargin()
    }

    internal fun cleanSection(v: KoalaEditTextView) {
        if (v.section_text.visibility != View.GONE) {
            v.section_text.visibility = View.GONE
        }
        v.section = SECTION_NULL
        v.sectionIndex = 1
        val pr = parent as? ViewGroup ?: return
        val next = KoalaRichEditorView.getNext(pr, this@KoalaEditTextView)
        if (next != null && next is KoalaEditTextView && next.section != SECTION_NULL) {
            resetNextSection(this@KoalaEditTextView)
        }
    }

    internal fun setNextSection(v: KoalaEditTextView?) {
        if (null == v) return
        val pr = parent as? ViewGroup ?: return
        val prev = KoalaRichEditorView.getPrev(pr, v)
        val next = KoalaRichEditorView.getNext(pr, v)
        if (prev != null && prev is KoalaEditTextView) {
            if (prev.section == v.section && v.section == SECTION_NUMBER && !v.quote) {
                v.setNumberSection(v)
            } else if (prev.section == v.section && v.section == SECTION_DOT && !v.quote) {
                v.setDotSection(v)
            }
            if (next is KoalaEditTextView && v.section == next.section) {
                v.setNextSection(next)
            }
        }
    }

    fun resetPosition(): Boolean {
        if (section != SECTION_NUMBER) return false
        val pr = parent as? ViewGroup ?: return false
        val pre = KoalaRichEditorView.getPrev(pr, this)
        val preIndex = if (pre == null || pre !is KoalaEditTextView) {
            0
        } else {
            pre.sectionIndex
        }
        sectionIndex = preIndex + 1
        section_text.text = "$sectionIndex. "
        resetNextSection(this)
        return true
    }

    fun resetNextSection(v: KoalaEditTextView?) {
        if (v != null) {
            val pr = parent as? ViewGroup ?: return
            val next = KoalaRichEditorView.getNext(pr, v)
            if (next != null && next is KoalaEditTextView) {
                if (!v.quote) {
                    if (next.section == v.section) {
                        if (v.section == SECTION_NUMBER) {
                            next.setNumberSection(next)
                        } else if (v.section == SECTION_DOT) {
                            next.setDotSection(next)
                        } else {
                            next.cleanSection(next)
                        }
                    } else {
                        if (next.section == SECTION_NUMBER) {
                            next.setNumberSection(next)
                        } else if (next.section == SECTION_DOT) {
                            next.setDotSection(next)
                        } else {
                            next.cleanSection(next)
                        }
                    }
                } else {
                    if (next.section == SECTION_NUMBER) {
                        next.setNumberSection(next)
                    } else if (next.section == SECTION_DOT) {
                        next.setDotSection(next)
                    } else {
                        next.cleanSection(next)
                    }
                }
                v.resetNextSection(next)
            }
            v.initMargin()
        }
    }

    private val quotePadding = context.dp2px(10f).toInt()
    private fun cleanQuote(section: Int, setStyle: Int) {
        val p: CharSequence?
        val n: CharSequence?
        val s: CharSequence
        val v = this
        edit_content_bg.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
        if (v.edit_content_bg.paddingLeft != 0) {
            v.edit_content_bg.setPadding(0, 0, 0, 0)
        }
        v.quote = false
        if (listener != null) {
            val start = v.edit_text.selectionStart
            val end = v.edit_text.selectionEnd
            if (start == end) {
                val f = v.edit_text.text.subSequence(0, start).toString().lastIndexOf('\n')
                var l = v.edit_text.text.subSequence(end, edit_text.text.length).toString().indexOf('\n')
                if (l != -1) {
                    l += end
                } else {
                    l = edit_text.text.length
                }
                p = if (f != -1) {
                    v.edit_text.text.subSequence(0, f)
                } else {
                    null
                }
                n = if (l < edit_text.text.length) {
                    v.edit_text.text.subSequence(l + 1, edit_text.text.length)
                } else {
                    null
                }
                s = v.edit_text.text.subSequence(f + 1, l)
            } else {
                p = v.edit_text.text.subSequence(0, start)
                s = v.edit_text.text.subSequence(start, end)
                n = v.edit_text.text.subSequence(end, edit_text.text.length)
            }

            listener!!.splitSelf(v, p, s, n, section, setStyle)
        }
        initMargin()
    }

    private fun cleanAllQuote() {
        edit_content_bg.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
        edit_content_bg.setPadding(0, 0, 0, 0)
        quote = false
        listener!!.splitSelf(this, null, edit_text.text, null, SECTION_NULL, STYLE_NORMAL)
    }

    private fun markQuote() {
        edit_content_bg.setBackgroundResource(R.drawable.shape_edit_quote_bg)
        quote = true
        if (edit_content_bg.paddingLeft != quotePadding) {
            edit_content_bg.setPadding(quotePadding, quotePadding, quotePadding, quotePadding)
        }
        cleanSection(this)
        resetNextSection(this)
    }

    private val defaultPadding = context.dp2px(10f).toInt()
    private val paddingAnim: PaddingAnim by lazy { PaddingAnim(edit_content_bg) }
    //    private val spaceHeightAnim: HeightAnim by lazy { HeightAnim(edit_space) }
//    private var originalHeight = 0
    override fun enableDrag(enable: Boolean) {
        edit_container.showShadow(enable)
        isDragging = enable
        if (enable) {
//            viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
            if (!ifQuote()) {
                ObjectAnimator.ofInt(paddingAnim, "padding", edit_content_bg.paddingLeft, defaultPadding).setDuration(animTime).start()
//                ObjectAnimator.ofInt(spaceHeightAnim, "x", edit_space.height, edit_space.height + 2 * defaultPadding).setDuration(animTime).start()
            }
            edit_text.isCursorVisible = false
            edit_text.isFocusable = false
            edit_text.isFocusableInTouchMode = false
            edit_text.isEnabled = false
            edit_icon_drag.visibility = View.VISIBLE
        } else {
            if (!ifQuote()) {
                ObjectAnimator.ofInt(paddingAnim, "padding", edit_content_bg.paddingLeft, 0).setDuration(animTime).start()
//                val anim = ObjectAnimator.ofInt(spaceHeightAnim, "x", edit_space.height, originalHeight)
//                anim.addListener(object : Animator.AnimatorListener {
//                    override fun onAnimationRepeat(animation: Animator?) {
//                    }
//
//                    override fun onAnimationEnd(animation: Animator?) {
//                        if (!isDragging) {
//                            viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
//                        }
//                    }
//
//                    override fun onAnimationCancel(animation: Animator?) {
//                    }
//
//                    override fun onAnimationStart(animation: Animator?) {
//                    }
//                })
//                anim.setDuration(animTime).start()
            }
            edit_text.isCursorVisible = true
            edit_text.isFocusable = true
            edit_text.isFocusableInTouchMode = true
            edit_text.isEnabled = true
            edit_icon_drag.visibility = View.GONE
        }
    }

    fun actionDown() {
        edit_icon_drag.setImageResource(R.drawable.svg_drag_icon_selected)
        edit_container.showHighLight(true)
    }

    fun actionUp() {
        edit_icon_drag.setImageResource(R.drawable.svg_drag_icon)
        edit_container.showHighLight(false)
    }

    companion object {

        val S_H1 = 1
        val S_H2 = 1 shl 1
        val S_G = 1 shl 2
        val S_B = 1 shl 3
        val S_Q = 1 shl 4
        val S_L = 1 shl 5
        val SECTION_NULL = 0
        val SECTION_NUMBER = 1
        val SECTION_DOT = 2
        val STYLE_NORMAL = 0
        val STYLE_H1 = 1
        val STYLE_H2 = 2

        interface OnEditListener {
            fun insertEdit(v: KoalaEditTextView)

            fun pressEnter(v: KoalaEditTextView)

            fun deleteSelf(v: KoalaEditTextView)

            fun splitSelf(v: KoalaEditTextView, p: CharSequence?, s: CharSequence, n: CharSequence?, section: Int, style: Int)
        }

        interface OnEditTextStatusListener {
            fun onEditStatus(status: Int)
        }

        interface OnHintSetListener {
            fun onHintChanged()
        }

        private class HtmlTagHandler : Html.TagHandler {

            override fun handleTag(opening: Boolean, tag: String, output: Editable, xmlReader: XMLReader) {
                if (tag == "strike" || tag == "del") {//自定义解析<strike></strike>标签
                    val len = output.length
                    if (opening) {//开始解析该标签，打一个标记
                        output.setSpan(StrikethroughSpan(), len, len, Spannable.SPAN_MARK_MARK)
                    } else {//解析结束，读出所有标记，取最后一个标记为当前解析的标签的标记（因为解析方式是便读便解析）
                        val spans = output.getSpans(0, len, StrikethroughSpan::class.java)
                        if (spans.size > 0) {
                            for (i in spans.indices) {
                                if (output.getSpanFlags(spans[i]) == Spannable.SPAN_MARK_MARK) {
                                    val start = output.getSpanStart(spans[i])
                                    output.removeSpan(spans[i])
                                    if (start != len) {
                                        output.setSpan(StrikethroughSpan(), start, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                                    }
                                    break
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
