package com.stainberg.keditview

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
    private var rateImageWidth: Int = 0
    private var rateImageHeight: Int = 0
    private var defaultImageViewWidth: Int = 0
    private var defaultImageViewHeight: Int = 0
    private val bound = resources.displayMetrics.heightPixels

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

    @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        init()
    }

    private lateinit var sr: SoftReference<KoalaRichEditorView.Companion.IOnImageClickListener?>

    constructor(context: Context, fileData: FileData, lis: KoalaRichEditorView.Companion.IOnImageClickListener?) : super(context) {
        this.sr = SoftReference(lis)
        this.fileData = fileData
        filePath = if (TextUtils.isEmpty(fileData.fileUrl)) Uri.fromFile(File(fileData.filePath)).toString() else fileData.fileUrl
        init()
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
        icon.viewTreeObserver.addOnGlobalLayoutListener {
            val lp = touch_container.layoutParams
            lp.width = icon.width
            lp.height = icon.height
            touch_container.layoutParams = lp
        }
        visible = false
        viewTreeObserver.addOnScrollChangedListener(onScrollChangedListener)
        imgWidth = fileData?.width ?: 0
        imgHeight = fileData?.height ?: 0
        icon.measure(MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED)
        val whRate = imgWidth.toFloat() / imgHeight.toFloat()
        val x: Float = context.screenWidth - context.dp2px(20f) * 2
        val y: Float = x / whRate

        defaultImageViewWidth = x.toInt()
        defaultImageViewHeight = y.toInt()
        rateImageWidth = (defaultImageViewWidth - defaultPadding)
        rateImageHeight = (rateImageWidth / whRate).toInt()

        var lp = icon.layoutParams
        lp.width = defaultImageViewWidth
        lp.height = defaultImageViewHeight
        icon.layoutParams = lp

        val request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(filePath))
                .setResizeOptions(ResizeOptions(100, (imgHeight/whRate).toInt()))
                .build()
        icon.controller = Fresco.newDraweeControllerBuilder()
//                .setOldController(mDraweeView.getController())
                .setImageRequest(request)
                .build()
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
    override fun enableDrag(enable: Boolean) {
        if (enable) {
            content_bg.setPadding(defaultPadding, defaultPadding, defaultPadding, defaultPadding)
            val lp = icon.layoutParams
            lp.height = rateImageHeight
            lp.width = rateImageWidth
            icon.layoutParams = lp
            icon_drag.visibility = View.VISIBLE
            touch_container.visibility = View.GONE
            invalidate()
        } else {
            content_bg.setPadding(0, 0, 0, 0)
            val lp = icon.layoutParams
            lp.height = defaultImageViewHeight
            lp.width = defaultImageViewWidth
            icon.layoutParams = lp
            icon_drag.visibility = View.GONE
            touch_container.visibility = View.VISIBLE
            invalidate()
        }
    }

    override fun release() {
        viewTreeObserver.removeOnScrollChangedListener(onScrollChangedListener)
        releaseImage()
    }

    private fun reloadImage() {
        icon.setImageURI(filePath)
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
