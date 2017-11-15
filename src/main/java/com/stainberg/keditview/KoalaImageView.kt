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
import kotlinx.android.synthetic.main.item_view_image.view.*
import java.io.File
import java.lang.ref.SoftReference


/**
 * Created by Stainberg on 7/5/17.
 */

class KoalaImageView : FrameLayout, KoalaBaseCellView {
    var filePath: String = ""
    private var visible: Boolean = false
    private var imgWidth: Int = 0
    private var imgHeight: Int = 0
    private val bound = resources.displayMetrics.heightPixels
    private val request: ImageRequest
    lateinit var fileData: FileData

    private var isDragEnabled = false

    private var onScrollChangedListener: ViewTreeObserver.OnScrollChangedListener = ViewTreeObserver.OnScrollChangedListener {
        val location = IntArray(2)
        icon.getLocationInWindow(location)
        if (location[1] < 0) {
            if (location[1] < -(imgHeight + bound) && visible) {
                releaseImage()
                return@OnScrollChangedListener
            }
            if (location[1] > -(imgHeight + bound) && !visible) {
                reloadImage()
            }
        } else {
            if (location[1] > resources.displayMetrics.heightPixels + bound && visible) {
                releaseImage()
                return@OnScrollChangedListener
            }
            if (location[1] < resources.displayMetrics.heightPixels + bound && !visible) {
                reloadImage()
            }
        }
    }

    @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, request: ImageRequest) : super(context, attrs, defStyleAttr) {
        init()
        this.request = request
    }

    private lateinit var sr: SoftReference<KoalaRichEditorView.Companion.IOnImageClickListener?>
//    private lateinit var iconObserver: IconObserver

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
        if (icon_drag.visibility == View.VISIBLE) {
            isDragEnabled = eventInView(ev, icon_drag)
            if (isDragEnabled) {
                return isDragEnabled
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (isDragEnabled) {
            false
        } else super.onTouchEvent(event)
    }

    private fun init() {
        val v = LayoutInflater.from(context).inflate(R.layout.item_view_image, this, true)
        v.findViewById<View>(R.id.left).setOnClickListener {
            Log.e("ABCDEFG", "Left")
        }
        v.findViewById<View>(R.id.right).setOnClickListener {
            Log.e("ABCDEFG", "Right")
        }
        v.findViewById<View>(R.id.center).setOnClickListener {
            sr.get()?.onImageClick((parent as ViewGroup).indexOfChild(this))
        }
        visible = false
        viewTreeObserver.addOnScrollChangedListener(onScrollChangedListener)
        imgWidth = fileData?.width ?: 0
        imgHeight = fileData?.height ?: 0
        icon.measure(MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED)
        val whRate = imgWidth.toFloat() / imgHeight.toFloat()
        val x: Float = context.screenWidth - context.dp2px(20f) * 2
        val y: Float = x / whRate

        var lp = content_bg.layoutParams
        lp.height = y.toInt()
        content_bg.layoutParams = lp
    }

    override fun obtainUrl(): String {
        return filePath
    }

    override fun reload() {
        val location = IntArray(2)
        icon.getLocationInWindow(location)
        if (location[1] < 0) {
            if (location[1] < -(imgHeight + bound) && visible) {
                releaseImage()
                return
            }
            if (location[1] > -(imgHeight + bound) && !visible) {
                reloadImage()
            }
        } else {
            if (location[1] > resources.displayMetrics.heightPixels + bound && visible) {
                releaseImage()
                return
            }
            if (location[1] < resources.displayMetrics.heightPixels + bound && !visible) {
                reloadImage()
            }
        }
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
    override fun setSection(st: Int) {

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
    override fun setText(sequence: CharSequence) {

    }

    @Deprecated("")
    override fun obtainText(): CharSequence {
        return ""
    }

    @Deprecated("")
    override fun obtainHtmlText(): List<String> {
        return emptyList()
    }

    override fun setHtmlText(html: String) {

    }

    @Deprecated("")
    override fun ifQuote(): Boolean {
        return false
    }

    @Deprecated("")
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

    }

    private val defaultPadding = context.dp2px(4f).toInt()
    private val paddingAnim: PaddingAnim by lazy { PaddingAnim(content_bg) }
    override fun enableDrag(enable: Boolean) {
        container.showShadow(enable)
        if (enable) {
            ObjectAnimator.ofInt(paddingAnim, "padding", content_bg.paddingLeft, defaultPadding).setDuration(animTime).start()
            icon_drag.visibility = View.VISIBLE
            touch_container.visibility = View.GONE
        } else {
            ObjectAnimator.ofInt(paddingAnim, "padding", content_bg.paddingLeft, 0).setDuration(animTime).start()
            icon_drag.visibility = View.GONE
            touch_container.visibility = View.VISIBLE
        }
    }

    fun actionDown() {
        icon_drag.setImageResource(R.drawable.svg_drag_icon_selected)
        container.showHighLight(true)
    }

    fun actionUp() {
        icon_drag.setImageResource(R.drawable.svg_drag_icon)
        container.showHighLight(false)
    }

    override fun release() {
        viewTreeObserver.removeOnScrollChangedListener(onScrollChangedListener)
        releaseImage()
    }

    private fun reloadImage() {
        icon.controller = Fresco.newDraweeControllerBuilder().setImageRequest(request).build()
        visible = true
    }

    fun releaseImage() {
        visible = false
        icon.setImageBitmap(null)
        icon.setBackgroundColor(ContextCompat.getColor(context, R.color.gray_placeholder))
        val imagePipeline = Fresco.getImagePipeline()
        val uri: Uri = Uri.fromFile(File(filePath))
        imagePipeline.evictFromMemoryCache(uri)
        imagePipeline.evictFromDiskCache(uri)
    }
}
