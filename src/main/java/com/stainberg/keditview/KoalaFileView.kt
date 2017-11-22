package com.stainberg.keditview

import android.content.Context
import android.os.Build
import android.support.annotation.AttrRes
import android.support.annotation.RequiresApi
import android.support.annotation.StyleRes
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
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

    constructor(context: Context, fileData: FileData) : this(context) {
        this.fileData = fileData
        reloadData()
    }

    fun reloadData() {
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

    fun setOnFileClickListener(lis: KoalaRichEditorView.Companion.IOnFileClickListener?) {
        listener = SoftReference(lis)
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
            textStatus = 0
            updateTextStatus()
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
        contentContainer = file_container
    }

    private fun initMargin() {
        val pre = KoalaRichEditorView.getPrev(parent as ViewGroup, this)
        pre?.let {
            val lp = file_content_bg.layoutParams as MarginLayoutParams
            when (pre) {
                is KoalaImageView, is KoalaFileView -> {
                    lp.topMargin = MARGIN_4
                }
                is KoalaEditTextView -> {
                    lp.topMargin = MARGIN_3
                }
                else -> {
                    lp.topMargin = MARGIN_5
                }
            }
            file_content_bg.layoutParams = lp
        }
    }

    private val offset = context.screenHeight
    private val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        val height = file_container.height
        if (height != 0) {
            val lp = file_space.layoutParams
            if (lp.height != height) {
                lp.height = height
                file_space.layoutParams = lp
            }
        }
    }
    private lateinit var contentContainer: View
    private val scrollChangedListener = ViewTreeObserver.OnScrollChangedListener {
        val location = IntArray(2)
        getLocationInWindow(location)
        val y = location[1]
        if (y > offset * 2 || (y + height < -offset)) {
            if (contentContainer.parent != null) {
                removeView(contentContainer)
            }
        } else {
            if (contentContainer.parent == null) {
                addView(contentContainer)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
        viewTreeObserver.addOnScrollChangedListener(scrollChangedListener)
        initMargin()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
        viewTreeObserver.removeOnScrollChangedListener(scrollChangedListener)
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

    override fun resetMargin() {
        initMargin()
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
            file_container.isFocusable = false
            file_container.isFocusableInTouchMode = false
            file_icon_drag.visibility = View.VISIBLE
        } else {
            file_container.isFocusable = true
            file_container.isFocusableInTouchMode = true
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