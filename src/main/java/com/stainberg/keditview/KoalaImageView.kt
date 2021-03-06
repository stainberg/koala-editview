package com.stainberg.keditview

import android.animation.ObjectAnimator
import android.content.Context
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import kotlinx.android.synthetic.main.item_view_edit_text.view.edit_text
import kotlinx.android.synthetic.main.item_view_file.view.*
import kotlinx.android.synthetic.main.item_view_image.view.*
import java.io.File
import java.lang.ref.SoftReference


/**
 * Created by Stainberg on 7/5/17.
 */

class KoalaImageView : FrameLayout, KoalaBaseCellView {
    var filePath: String = ""
    private var imgWidth: Int = 0
    private var imgHeight: Int = 0
    private val request: ImageRequest
    var fileData: FileData
    private var dragTouchToggled = false
    private var textStatus = 0
    private var isDragging = false
    private var isImageReleased = true

    private var sr: SoftReference<KoalaRichEditorView.Companion.IOnImageClickListener?>

    constructor(context: Context, fileData: FileData, lis: KoalaRichEditorView.Companion.IOnImageClickListener?) : super(context) {
        this.sr = SoftReference(lis)
        this.fileData = fileData
        filePath = if (TextUtils.isEmpty(fileData.fileUrl)) Uri.fromFile(File(fileData.filePath)).toString() else fileData.fileUrl
        init()

        var w: Float
        var h: Float
        if (fileData.width < fileData.height) {
            w = fileData.width.toFloat()
            while (w > 1000) {
                w -= 200
            }
            h = fileData.height / (fileData.width / w)
        } else {
            h = fileData.height.toFloat()
            while (h > 1000) {
                h -= 200
            }
            w = fileData.width.toFloat() / fileData.height.toFloat() * h
        }

        request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(filePath))
                .setResizeOptions(ResizeOptions(w.toInt(), h.toInt()))
                .build()
        reloadImage()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (image_icon_drag.visibility == View.VISIBLE) {
            dragTouchToggled = eventInView(ev, image_icon_drag)
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

    private fun init() {
        LayoutInflater.from(context).inflate(R.layout.item_view_image, this, true)
        image_left_area.setOnClickListener {
            if (!isDragging) {
                image_container.requestFocus()
                textStatus = 1
                updateTextStatus()
            }
        }
        image_right_area.setOnClickListener {
            if (!isDragging) {
                image_container.requestFocus()
                textStatus = 2
                updateTextStatus()
            }
        }
        image_center_area.setOnClickListener {
            textStatus = 0
            updateTextStatus()
            sr.get()?.onImageClick((parent as ViewGroup).indexOfChild(this))
        }
        image_container.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus || isDragging) {
                textStatus = 0
                updateTextStatus()
            }
        }
        imgWidth = fileData.width
        imgHeight = fileData.height
        icon.measure(MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED)
        val whRate = imgWidth.toFloat() / imgHeight.toFloat()
        val x: Float = context.screenWidth - context.dp2px(20f) * 2
        val y: Float = x / whRate

        var lp = image_content_bg.layoutParams
        lp.height = y.toInt()
        image_content_bg.layoutParams = lp

        image_container.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK || KeyEvent.isModifierKey(keyCode)) {
                false
            } else {
                if (textStatus != 0 && !isDragging) {
                    if (event.action == KeyEvent.ACTION_UP || event.action == KeyEvent.ACTION_MULTIPLE) {
                        val index = (parent as ViewGroup).indexOfChild(this@KoalaImageView)
                        if (keyCode == KeyEvent.KEYCODE_DEL) {
                            if (textStatus == 1) {
                                val pre = KoalaRichEditorView.getPrev(parent as ViewGroup, this@KoalaImageView)
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
        if (image_left_line.visibility != leftVisibility) {
            image_left_line.visibility = leftVisibility
        }
        val rightVisibility = if (textStatus == 2) VISIBLE else GONE
        if (image_right_line.visibility != rightVisibility) {
            image_right_line.visibility = rightVisibility
        }
        if (textStatus != 0) {
            post { showSoft() }
        }
    }

    override fun resetMargin() {
        initMargin()
    }

    private val offset = context.screenHeight
    private val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        val height = image_container.height
        if (height != 0) {
            val lp = image_space.layoutParams
            if (lp.height != height) {
                lp.height = height
                image_space.layoutParams = lp
            }
        }
    }
    private val scrollChangedListener = ViewTreeObserver.OnScrollChangedListener {
        val location = IntArray(2)
        getLocationInWindow(location)
        val y = location[1]
        if (y > offset * 2 || (y + height < -offset)) {
            if (image_container.visibility != View.GONE) {
                image_container.visibility = View.GONE
            }
            releaseImage()
        } else {
            if (image_container.visibility != View.VISIBLE) {
                image_container.visibility = View.VISIBLE
            }
            reloadImage()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
        viewTreeObserver.addOnScrollChangedListener(scrollChangedListener)
        reloadImage()
        initMargin()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
        viewTreeObserver.removeOnScrollChangedListener(scrollChangedListener)
        releaseImage()
    }

    private fun initMargin() {
        val pre = KoalaRichEditorView.getPrev(parent as ViewGroup, this)
        pre?.let {
            val lp = image_content_bg.layoutParams as MarginLayoutParams
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
            image_content_bg.layoutParams = lp
        }
    }

    private val defaultPadding = context.dp2px(4f).toInt()
    private val paddingAnim: PaddingAnim by lazy { PaddingAnim(image_content_bg) }
    override fun enableDrag(enable: Boolean) {
        isDragging = enable
        textStatus = 0
        updateTextStatus()
        image_container.showShadow(enable)
        if (enable) {
            image_container.isFocusable = false
            image_container.isFocusableInTouchMode = false
            ObjectAnimator.ofInt(paddingAnim, "padding", image_content_bg.paddingLeft, defaultPadding).setDuration(animTime).start()
            image_icon_drag.visibility = View.VISIBLE
            image_touch_container.visibility = View.GONE
        } else {
            image_container.isFocusable = true
            image_container.isFocusableInTouchMode = true
            ObjectAnimator.ofInt(paddingAnim, "padding", image_content_bg.paddingLeft, 0).setDuration(animTime).start()
            image_icon_drag.visibility = View.GONE
            image_touch_container.visibility = View.VISIBLE
        }
    }

    fun actionDown() {
        image_icon_drag.setImageResource(R.drawable.svg_drag_icon_selected)
        image_container.showHighLight(true)
    }

    fun actionUp() {
        image_icon_drag.setImageResource(R.drawable.svg_drag_icon)
        image_container.showHighLight(false)
    }

    private fun reloadImage() {
        if (!isImageReleased) return
        isImageReleased = !isImageReleased
        icon.controller = Fresco.newDraweeControllerBuilder().setImageRequest(request).build()
    }

    private fun releaseImage() {
        if (isImageReleased) return
        isImageReleased = !isImageReleased
        icon.setImageBitmap(null)
        val imagePipeline = Fresco.getImagePipeline()
        val uri: Uri = Uri.fromFile(File(filePath))
        imagePipeline.evictFromMemoryCache(uri)
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
            krev.deleteImage(index)
            krev.addCellText("", index)
        }
    }
}
