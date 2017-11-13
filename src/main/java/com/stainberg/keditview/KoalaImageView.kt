package com.stainberg.keditview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.support.media.ExifInterface
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.*
import android.view.*
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.item_view_image.view.*

import java.lang.ref.*

/**
 * Created by Stainberg on 7/5/17.
 */

class KoalaImageView : FrameLayout, KoalaBaseCellView {
    var filePath: String = ""
    private var visible: Boolean = false
    private var imgWidth: Int = 0
    private var imgHeight: Int = 0
    private var bitmap: Bitmap? = null
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
        filePath = if (TextUtils.isEmpty(fileData.fileUrl)) fileData.filePath else fileData.fileUrl
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
        visible = false
        viewTreeObserver.addOnScrollChangedListener(onScrollChangedListener)
        imgWidth = fileData?.width ?: 0
        imgHeight = fileData?.height ?: 0
        icon.measure(MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED)
        val x: Float = context.screenWidth - context.dp2px(20f) * 2
        val y: Float = x / (imgWidth.toFloat() / imgHeight.toFloat())

        var lp = icon.layoutParams
        lp.width = x.toInt()
        lp.height = y.toInt()
        icon.layoutParams = lp

        lp = touch_container.layoutParams
        lp.width = x.toInt()
        lp.height = y.toInt()
        touch_container.layoutParams = lp
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

    override fun enableDrag(enable: Boolean) {
        if (enable) {
            content_bg.setBackgroundResource(R.drawable.widget_view_card_bg)
            icon_drag.visibility = View.VISIBLE
            touch_container.visibility = View.GONE
        } else {
            content_bg.setBackgroundResource(R.drawable.widget_view_card_bg)
            icon_drag.visibility = View.GONE
            touch_container.visibility = View.VISIBLE
        }
    }

    override fun release() {
        viewTreeObserver.removeOnScrollChangedListener(onScrollChangedListener)
        releaseImage()
    }

    private fun reloadImage() {
        println("reload bitmap")
        val w = imgWidth.toFloat()
        val h = imgHeight.toFloat()
        val rate = w / h
        if (icon.aspectRatio != rate) {
            icon.aspectRatio = w / h
        }
        if (filePath.startsWith("http")) {
            icon.setImageURI(filePath)
        } else {
            if (bitmap != null) {
                return
            }
            KoalaImageLoadPoll.getPoll().handle(Runnable {
                try {
                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeFile(filePath, options)
                    val w = options.outWidth
                    val scale = (w / 800).toFloat()
                    val s = Math.ceil(scale.toDouble()).toInt()
                    options.inJustDecodeBounds = false
                    options.inSampleSize = s
                    bitmap = BitmapFactory.decodeFile(filePath, options)
                    val digree: Int
                    val exif = ExifInterface(filePath)
                    val ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED)
                    when (ori) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> digree = 90
                        ExifInterface.ORIENTATION_ROTATE_180 -> digree = 180
                        ExifInterface.ORIENTATION_ROTATE_270 -> digree = 270
                        else -> digree = 0
                    }
                    if (digree != 0) {
                        val m = Matrix()
                        m.postRotate(digree.toFloat())
                        bitmap = Bitmap.createBitmap(bitmap!!, 0, 0, bitmap!!.width, bitmap!!.height, m, true)
                        icon.setImageBitmap(bitmap)
                    }
                    post { icon.setImageBitmap(bitmap) }
                } catch (e: Exception) {

                }
            })
        }
        visible = true
    }

    fun releaseImage() {
        visible = false
        icon.setImageBitmap(null)
        icon.setBackgroundColor(ContextCompat.getColor(context, R.color.gray_placeholder))
        if (bitmap != null) {
            bitmap!!.recycle()
            bitmap = null
        }
    }

    companion object {
        interface OnImageDeleteListener {
            fun delete(v: KoalaBaseCellView)
        }
    }
}
