package com.stainberg.keditview

import android.content.Context
import android.os.Build
import android.support.annotation.AttrRes
import android.support.annotation.RequiresApi
import android.support.annotation.StyleRes
import android.text.TextUtils
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.item_view_edit_text.view.edit_text
import kotlinx.android.synthetic.main.item_view_file.view.*
import kotlinx.android.synthetic.main.item_view_image.view.image_right_area
import java.lang.ref.SoftReference

/**
 * Created by Stainberg on 7/5/17.
 */

class KoalaFileView : FrameLayout, KoalaBaseCellView {

    lateinit var fileData: FileData
    private var dragTouchToggled = false
    private var textStatus = 0

    @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        init()
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int, @StyleRes defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private var listener: SoftReference<KoalaRichEditorView.Companion.IOnFileClickListener?> = SoftReference(null)

    constructor(context: Context, fileData: FileData, lis: KoalaRichEditorView.Companion.IOnFileClickListener?) : this(context) {
        this.fileData = fileData
        listener = SoftReference(lis)
        if (childCount == 1) {
            var colorId = R.color.color_unknwon
            if (fileData != null) {
                if (fileData.iconResId != 0) {
                    file_icon.setImageResource(fileData.iconResId)
                    file_icon_text.background = null
                } else if (!TextUtils.isEmpty(fileData.iconUrl)) {
                    file_icon.setImageURI(fileData.iconUrl)
                    file_icon_text.background = null
                } else {
                    val type = fileData.fileType
                    when (type) {
                        FileData.DOC, FileData.DOCX -> file_icon_text.setBackgroundResource(R.drawable.svg_file_doc)
                        FileData.PDF -> file_icon_text.setBackgroundResource(R.drawable.svg_file_pdf)
                        FileData.PPT, FileData.PPTX -> file_icon_text.setBackgroundResource(R.drawable.svg_file_ppt)
                        FileData.EPUB -> file_icon_text.setBackgroundResource(R.drawable.svg_file_epub)
                        FileData.TXT -> file_icon_text.setBackgroundResource(R.drawable.svg_file_txt)
                        FileData.XLS, FileData.XLSX -> file_icon_text.setBackgroundResource(R.drawable.svg_file_xls)
                        else -> file_icon_text.setBackgroundResource(R.drawable.svg_file_unknown)
                    }
                    colorId = R.color.white_card_bg
                }
                file_icon_text.text = if (TextUtils.isEmpty(fileData.fileType)) "" else fileData.fileType
                file_title.text = if (TextUtils.isEmpty(fileData.fileName)) "" else fileData.fileName
                file_desc.text = if (TextUtils.isEmpty(fileData.desc)) "" else fileData.desc
            } else {
                file_icon_text.setBackgroundResource(R.drawable.svg_file_unknown)
                file_icon_text.text = if (TextUtils.isEmpty(fileData.fileType)) "" else fileData.fileType
                file_title.text = if (TextUtils.isEmpty(fileData.fileName)) "" else fileData.fileName
                file_desc.text = if (TextUtils.isEmpty(fileData.desc)) "" else fileData.desc
            }
            file_icon_text.setTextColor(getContext().resources.getColor(colorId))
        }
    }

    private fun init() {
        LayoutInflater.from(context).inflate(R.layout.item_view_file, this, true)
        file_left_area.setOnClickListener {
            if (!isDragging) {
                file_container.requestFocus()
                textStatus = 1
                updateTextStatus()
            }
        }
        file_right_area.setOnClickListener {
            if (!isDragging) {
                file_container.requestFocus()
                textStatus = 2
                updateTextStatus()
            }
        }
        file_center_area.setOnClickListener {
            listener.get()?.onFileClick((parent as ViewGroup).indexOfChild(this))
        }
        file_container.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus || isDragging) {
                textStatus = 0
                updateTextStatus()
            }
        }
        file_container.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK || KeyEvent.isModifierKey(keyCode)) {
                false
            } else {
                if (textStatus != 0 && !isDragging) {
                    if (event.action == KeyEvent.ACTION_UP || event.action == KeyEvent.ACTION_MULTIPLE) {
                        val index = (parent as ViewGroup).indexOfChild(this@KoalaFileView)
                        if (keyCode == KeyEvent.KEYCODE_DEL) {
                            if (textStatus == 1) {
                                val pre = KoalaRichEditorView.getPrev(parent as ViewGroup, this@KoalaFileView)
                                if (null != pre) {
                                    if (pre is KoalaEditTextView) {
                                        pre.edit_text.requestFocus()
                                        pre.edit_text.setSelection(pre.edit_text.length())
                                    } else if (pre is KoalaImageView) {
                                        pre.image_right_area.performClick()
                                    } else if (pre is KoalaFileView) {
                                        pre.file_right_area.performClick()
                                    }
                                }
                            } else if (textStatus == 2) {
                                deleteCurrentItem(index)
                            }
                        } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
                            if (textStatus == 1) {
                                insertNewLine("", index)
                            } else if (textStatus == 2) {
                                insertNewLine("", index + 1)
                            }
                        } else {
                            val s: String? = if (keyCode != 0) {
                                if (event != null && event.unicodeChar != null) {
                                    event.unicodeChar.toChar().toString()
                                } else {
                                    null
                                }
                            } else {
                                if (null != event && null != event.characters) {
                                    event.characters.toString()
                                } else {
                                    null
                                }
                            }
                            s?.let {
                                if (textStatus == 1) {
                                    insertNewLine(s, index)
                                } else if (textStatus == 2) {
                                    insertNewLine(s, index + 1)
                                }
                            }
                        }
                    }
                }
                true
            }
        }
    }

    private fun updateTextStatus() {
        val leftVisibility = if (textStatus == 1) VISIBLE else GONE
        if (file_left_line.visibility != leftVisibility) {
            file_left_line.visibility = leftVisibility
        }
        val rightVisibility = if (textStatus == 2) VISIBLE else GONE
        if (file_right_line.visibility != rightVisibility) {
            file_right_line.visibility = rightVisibility
        }
        if (textStatus != 0) {
            post { showSoft() }
        }
    }

    override fun obtainUrl(): String {
        return ""
    }

    override fun reload() {

    }

    override fun setStyleH1() {

    }

    override fun setStyleH2() {

    }

    override fun setStyleNormal() {

    }

    override fun setGravity() {

    }

    override fun setQuote() {

    }

    override fun setSection(st: Int) {

    }

    override fun setBold() {

    }

    override fun setItalic() {

    }

    override fun setStrike() {

    }

    override fun addCode() {

    }

    override fun setText(sequence: CharSequence) {

    }

    override fun obtainText(): CharSequence {
        return ""
    }

    override fun obtainHtmlText(): List<String> {
        return emptyList()
    }

    override fun setHtmlText(html: String) {

    }

    override fun ifQuote(): Boolean {
        return false
    }

    override fun ifCode(): Boolean {
        return false
    }

    override fun obtainStyle(): Int {
        return 0
    }

    override fun obtainSection(): Int {
        return 0
    }

    override fun setEditable(enable: Boolean) {
        if (!enable) {
            file_icon_drag.visibility = View.GONE
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (file_icon_drag.visibility == View.VISIBLE) {
            dragTouchToggled = eventInView(ev, file_icon_drag)
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

    private var isDragging = false
    override fun enableDrag(enable: Boolean) {
        isDragging = enable
        textStatus = 0
        updateTextStatus()
        file_container.showShadow(enable)
        if (enable) {
            file_icon_drag.visibility = View.VISIBLE
        } else {
            file_icon_drag.visibility = View.GONE
        }
    }

    fun actionDown() {
        file_icon_drag.setImageResource(R.drawable.svg_drag_icon_selected)
        file_container.showHighLight(true)
    }

    fun actionUp() {
        file_icon_drag.setImageResource(R.drawable.svg_drag_icon)
        file_container.showHighLight(false)
    }

    override fun release() {

    }

    private fun insertNewLine(text: String, index: Int) {
        val krev = parent?.parent?.parent?.parent ?: return
        if (krev is KoalaRichEditorView) {
            krev.addCellText(text, index)
        }
    }

    private fun deleteCurrentItem(index: Int) {
        val krev = parent?.parent?.parent?.parent ?: return
        if (krev is KoalaRichEditorView) {
            krev.deleteFile(index)
            krev.addCellText("", index)
        }
    }
}