package com.stainberg.keditview

import android.content.Context
import android.os.Build
import android.support.annotation.AttrRes
import android.support.annotation.RequiresApi
import android.support.annotation.StyleRes
import android.text.TextUtils
import android.util.*
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView

import com.facebook.drawee.view.SimpleDraweeView
import kotlinx.android.synthetic.main.item_view_file.view.*
import java.lang.ref.*

/**
 * Created by Stainberg on 7/5/17.
 */

class KoalaFileView : FrameLayout, KoalaBaseCellView {

    lateinit var fileData: FileData
    private var isDragEnabled = false

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
            val container = getChildAt(0)
            val icon = container.findViewById<SimpleDraweeView>(R.id.icon)
            val iconText = container.findViewById<TextView>(R.id.icon_text)
            val title = container.findViewById<TextView>(R.id.title)
            val desc = container.findViewById<TextView>(R.id.desc)
            var colorId = R.color.color_unknwon
            if (fileData != null) {
                if (fileData.iconResId != 0) {
                    icon.setImageResource(fileData.iconResId)
                    iconText.background = null
                } else if (!TextUtils.isEmpty(fileData.iconUrl)) {
                    icon.setImageURI(fileData.iconUrl)
                    iconText.background = null
                } else {
                    val type = fileData.fileType
                    when (type) {
                        FileData.DOC, FileData.DOCX -> iconText.setBackgroundResource(R.drawable.svg_file_doc)
                        FileData.PDF -> iconText.setBackgroundResource(R.drawable.svg_file_pdf)
                        FileData.PPT, FileData.PPTX -> iconText.setBackgroundResource(R.drawable.svg_file_ppt)
                        FileData.EPUB -> iconText.setBackgroundResource(R.drawable.svg_file_epub)
                        FileData.TXT -> iconText.setBackgroundResource(R.drawable.svg_file_txt)
                        FileData.XLS, FileData.XLSX -> iconText.setBackgroundResource(R.drawable.svg_file_xls)
                        else -> iconText.setBackgroundResource(R.drawable.svg_file_unknown)
                    }//                            colorId = R.color.color_word;
                    //                            colorId = R.color.color_pdf;
                    //                            colorId = R.color.color_ppt;
                    //                            colorId = R.color.color_epub;
                    //                            colorId = R.color.color_txt;
                    //                            colorId = R.color.color_excel;
                    //                            colorId = R.color.color_unknwon;
                    colorId = R.color.white_card_bg
                }
                iconText.text = if (TextUtils.isEmpty(fileData.fileType)) "" else fileData.fileType
                title.text = if (TextUtils.isEmpty(fileData.fileName)) "" else fileData.fileName
                desc.text = if (TextUtils.isEmpty(fileData.desc)) "" else fileData.desc
            } else {
                iconText.setBackgroundResource(R.drawable.svg_file_unknown)
                iconText.text = if (TextUtils.isEmpty(fileData!!.fileType)) "" else fileData!!.fileType
                title.text = if (TextUtils.isEmpty(fileData.fileName)) "" else fileData.fileName
                desc.text = if (TextUtils.isEmpty(fileData.desc)) "" else fileData.desc
            }
            iconText.setTextColor(getContext().resources.getColor(colorId))
        }
    }

    private fun init() {
        LayoutInflater.from(context).inflate(R.layout.item_view_file, this, true)
        findViewById<View>(R.id.left).setOnClickListener {
            Log.e("ABCDEFG", "Left")
        }
        findViewById<View>(R.id.right).setOnClickListener {
            Log.e("ABCDEFG", "Right")
        }
        findViewById<View>(R.id.center).setOnClickListener {
            listener.get()?.onFileClick((parent as ViewGroup).indexOfChild(this))
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

    override fun enableDrag(enable: Boolean) {
        if (enable) {
            icon_drag.visibility = View.VISIBLE
        } else {
            icon_drag.visibility = View.GONE
        }
    }

    override fun release() {

    }
}