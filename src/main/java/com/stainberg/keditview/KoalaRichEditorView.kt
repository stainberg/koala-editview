package com.stainberg.keditview

import android.content.Context
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.support.v4.widget.NestedScrollView
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.item_view_edit_text.view.*
import java.lang.ref.*

import java.util.ArrayList

/**
 * Created by Stainberg on 7/5/17.
 */

class KoalaRichEditorView @JvmOverloads constructor(context : Context , attrs : AttributeSet? = null , defStyleAttr : Int = 0) : FrameLayout(context , attrs , defStyleAttr) {

    private lateinit var container : EditorContainer
    private var keyStatusListener : OnStatusListener? = null
    private var onEditTextChangedListener : OnEditTextChangedListener? = null

    val desc : String
        get() {
            if (container.childCount <= 0) {
                return ""
            }
            val builder = StringBuilder()
            for (i in 0 until container.childCount) {
                if (container.getChildAt(i) is KoalaBaseCellView) {
                    val data = container.getChildAt(i) as KoalaBaseCellView
                    if (data is KoalaEditTextView) {
                        builder.append(data.obtainText())
                    } else if (data is KoalaImageView) {
                        builder.append(resources.getString(R.string.draft_pic))
                    } else if (data is KoalaCardView) {
                        builder.append(resources.getString(R.string.draft_card))
                    } else if (data is KoalaFileView) {
                        builder.append(resources.getString(R.string.draft_file))
                    }

                    if (builder.length > 50) {
                        break
                    }
                }
            }
            return builder.toString()
        }

    val itemCount : Int
        get() = container.childCount

    val allViews : List<KoalaBaseCellView>
        get() {
            val list = ArrayList<KoalaBaseCellView>()
            for (i in 0 until container.childCount) {
                if (container.getChildAt(i) is KoalaBaseCellView) {
                    list.add(container.getChildAt(i) as KoalaBaseCellView)
                }
            }
            return list
        }

    val currentFocusEdit : View?
        get() {
            val v = container.focusedChild
            return if (v != null && v is KoalaEditTextView) {
                v.edit_text
            } else null
        }

    private val onPressEnterListener = object : KoalaEditTextView.Companion.OnEditListener {

        override fun insertEdit(v : KoalaBaseCellView) {
            if (v is KoalaEditTextView) {
                val index = container.indexOfChild(v)
                val editTextView = KoalaEditTextView(context , this , statusListener)
                val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT)
                container.addView(editTextView , index , lp)
                editTextView.requestFocus()
                editTextView.setSelection(0)
            }
            setHint()
        }

        override fun pressEnter(v : KoalaBaseCellView) {
            if (v is KoalaEditTextView) {
                val p : CharSequence
                val n : CharSequence
                val index = container.indexOfChild(v)
                val start = v.selectionStart
                if (start < v.obtainText().length) {
                    p = v.obtainText().subSequence(0 , start)
                    n = v.obtainText().subSequence(start , v.obtainText().length)
                } else {
                    p = v.obtainText()
                    n = ""
                }
                val editTextView = KoalaEditTextView(context , this , statusListener)
                v.setText(p)
                editTextView.setText(n)
                val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT)
                container.addView(editTextView , index + 1 , lp)
                editTextView.requestFocus()
                editTextView.setSelection(0)
                if (v.obtainSection() === 1) {
                    editTextView.setNumberSection(editTextView)
                } else if (v.obtainSection() === 2) {
                    editTextView.setDotSection(editTextView)
                }
                editTextView.setNextSection(editTextView)
            }
            setHint()
        }

        override fun deleteSelf(v : KoalaBaseCellView) {
            if (container.childCount > 1) {
                val index = container.indexOfChild(v as View)
                val lastStr = v.obtainText()
                if (index > 0) {
                    val view = container.getChildAt(index - 1)
                    if (view is KoalaEditTextView) {
                        container.removeView(v as View)
                        view.append(lastStr , view.length())
                        view.requestFocus()
                        view.setNextSection(view)
                    } else if (view is KoalaImageView) {
                        container.removeView(v as View)
                        container.removeView(view)
                        if (index > 1) {
                            val pprev = container.getChildAt(index - 2)
                            if (pprev is KoalaEditTextView) {
                                pprev.append(lastStr , pprev.length())
                                pprev.requestFocus()
                                pprev.setNextSection(pprev)
                            }
                        }

                    } else if (view is KoalaSliderView) {
                        container.removeView(v as View)
                        container.removeView(view)
                        if (index > 1) {
                            val pprev = container.getChildAt(index - 2)
                            if (pprev is KoalaEditTextView) {
                                pprev.append(lastStr , pprev.length())
                                pprev.requestFocus()
                                pprev.setNextSection(pprev)
                            }
                        }
                    } else if (view is KoalaBaseCellView) {
                        container.removeView(v as View)
                        container.removeView(view)
                        if (index > 1) {
                            val pprev = container.getChildAt(index - 2)
                            if (pprev is KoalaEditTextView) {
                                pprev.append(lastStr , pprev.length())
                                pprev.requestFocus()
                                pprev.setNextSection(pprev)
                            }
                        }
                    }
                }
            }
            setHint()
        }

        override fun splitSelf(v : KoalaBaseCellView , p : CharSequence? , s : CharSequence , n : CharSequence? , section : Int , style : Int) {
            var index = container.indexOfChild(v as View)
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT)
            val ss = s.toString().split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (!TextUtils.isEmpty(p)) {
                v.setText(p!!)
                v.setQuote()
                for (i in 1 until ss.size + 1) {
                    val c = KoalaEditTextView(context , this , statusListener)
                    container.addView(c , index + i , lp)
                    c.setText(ss[i - 1])
                    c.setSection(section)
                    if (i == 1) {
                        c.requestFocus()
                        c.setSelection(0)
                    }
                    c.setTextStyle(style)
                }
                index = index + ss.size + 1
            } else {
                for (i in ss.indices) {
                    if (i == 0) {
                        v.setText(ss[i])
                        (v as KoalaEditTextView).requestFocus()
                        v.setSelection(0)
                        v.setSection(section)
                        v.setTextStyle(style)
                    } else {
                        val c = KoalaEditTextView(context , this , statusListener)
                        container.addView(c , index + i , lp)
                        c.setText(ss[i])
                        c.setSection(section)
                        c.setTextStyle(style)
                    }
                }
                index = index + ss.size
            }
            if (n != null) {
                val ne = KoalaEditTextView(context , this , statusListener)
                container.addView(ne , index)
                ne.setText(n)
                ne.setQuote()
            }
            setHint()
        }
    }

    private val onHintSetListener = object : KoalaEditTextView.Companion.OnHintSetListener {

        override fun onHintChanged() {
            setHint()
        }
    }

    private val statusListener = object : KoalaEditTextView.Companion.OnEditTextStatusListener {

        override fun setEnableKeyBoard(enable : Boolean) {
            if (keyStatusListener == null) {
                return
            }
            if (enable) {
                keyStatusListener!!.setEnableKeyBoard(true)
            } else {
                keyStatusListener!!.setEnableKeyBoard(false)
            }
        }

        override fun setEnableFocus(enable : Boolean) {
            for (i in 0 until container.childCount) {
                if (container.getChildAt(i) is KoalaEditTextView) {
                    val v = container.getChildAt(i) as KoalaEditTextView
                    v.setEditable(false)
                }
            }
        }

        override fun onEditStatus(status : Int) {
            if (onEditTextChangedListener != null) {
                onEditTextChangedListener!!.onEditTextTextChanged(status)
            }
        }
    }

    init {
        init()
    }

    private fun init() {
        val root = View.inflate(context , R.layout.layout_koala_rich_editor , this)
        val scrollView = root.findViewById<NestedScrollView>(R.id.koala_rich_editor_srollview)
        container = root.findViewById(R.id.koala_rich_editor_container)
        scrollView.isSmoothScrollingEnabled = true
        val editTextView = KoalaEditTextView(context!! , onPressEnterListener , statusListener , onHintSetListener)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT)
        container.addView(editTextView , lp)
        setOnClickListener {
            val edit = container.getChildAt(container.childCount - 1) as KoalaBaseCellView
            if (edit is KoalaEditTextView) {
                edit.edit_text!!.requestFocus()
                edit.edit_text!!.setSelection(edit.edit_text!!.length())
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(edit.edit_text , InputMethodManager.SHOW_FORCED)
            }
        }
        setHint()
    }

    fun swapViewGroupChildren(viewGroup : ViewGroup , firstView : View , secondView : View) {
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT)
        val lp1 = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , resources.getDimension(R.dimen.card_file_height).toInt())
        val firstIndex = viewGroup.indexOfChild(firstView)
        val secondIndex = viewGroup.indexOfChild(secondView)
        if (firstIndex < secondIndex) {
            viewGroup.removeViewAt(secondIndex)
            viewGroup.removeViewAt(firstIndex)
            viewGroup.addView(secondView , firstIndex , lp1)
            secondView.visibility = View.VISIBLE
            viewGroup.addView(firstView , secondIndex , lp)
            firstView.visibility = View.VISIBLE
        } else {
            viewGroup.removeViewAt(firstIndex)
            viewGroup.removeViewAt(secondIndex)
            viewGroup.addView(firstView , secondIndex , lp)
            firstView.visibility = View.VISIBLE
            viewGroup.addView(secondView , firstIndex , lp1)
            secondView.visibility = View.VISIBLE
        }
    }

    fun setOnEditTextChangedListener(listener : OnEditTextChangedListener) {
        onEditTextChangedListener = listener
    }

    fun setKeyStatusListener(listener : OnStatusListener) {
        keyStatusListener = listener
    }

    fun resetEditor() {
        container.removeAllViews()
    }

    fun getItem(index : Int) : KoalaBaseCellView? {
        return if (container.getChildAt(index) is KoalaBaseCellView) {
            container.getChildAt(index) as KoalaBaseCellView
        } else null
    }

    fun setStyleH1() {
        val v = container.focusedChild as KoalaBaseCellView?
        v?.setStyleH1()
    }

    fun setStyleH2() {
        val v = container.focusedChild as KoalaBaseCellView?
        v?.setStyleH2()
    }

    fun setGravity() {
        val v = container.focusedChild as KoalaBaseCellView?
        v?.setGravity()
    }

    fun setQuote() {
        val v = container.focusedChild as KoalaBaseCellView?
        v?.setQuote()
    }

    fun setSection(type : Int) {
        val v = container.focusedChild as KoalaBaseCellView?
        v?.setSection(type)
    }

    fun setSection() {
        val v = container.focusedChild as KoalaBaseCellView?
        if (v != null) {
            when (v.obtainSection()) {
                1 -> v.setSection(2)
                2 -> v.setSection(0)
                else -> v.setSection(1)
            }
        }
    }

    fun setBold() {
        val v = container.focusedChild as KoalaBaseCellView?
        v?.setBold()
    }

    fun setItalic() {
        val v = container.focusedChild as KoalaBaseCellView?
        v?.setItalic()
    }

    fun setStrike() {
        val v = container.focusedChild as KoalaBaseCellView?
        v?.setStrike()
    }

    fun addKoalaView(koalaBaseCellView : KoalaBaseCellView , h : Int) {
        if (koalaBaseCellView !is View) {
            throw IllegalArgumentException("view is not Extends View Class")
        }
        var index = container.childCount
        val v = container.focusedChild
        if (v != null) {
            index = container.indexOfChild(v)
        }
        val lpslider = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , h)
        container.addView(koalaBaseCellView as View , index + 1 , lpslider)
        val editTextView = KoalaEditTextView(context , onPressEnterListener , statusListener)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT)
        container.addView(editTextView , index + 2 , lp)
        editTextView.requestFocus()
        if (v is KoalaEditTextView) {
            val p : CharSequence
            val n : CharSequence
            val start = v.selectionStart
            if (start < v.obtainText().length) {
                p = v.obtainText().subSequence(0 , start)
                n = v.obtainText().subSequence(start , v.obtainText().length)
            } else {
                p = v.obtainText()
                n = ""
            }
            v.setText(p)
            editTextView.setText(n)
            editTextView.setSelection(0)
        }
    }

    fun enableDrag(enable : Boolean) {
        container.setDragEnabled(enable)
        for (i in 0 until container.childCount) {
            if (container.getChildAt(i) is KoalaBaseCellView) {
                val v = container.getChildAt(i) as KoalaBaseCellView
                v.enableDrag(enable)
            }
        }
    }

    @JvmOverloads
    fun addCellText(sequence : String , center : Boolean = false , isAddLast : Boolean = false) {
        val index = getNextIndex(isAddLast)
        val editTextView = KoalaEditTextView(context , onPressEnterListener , statusListener)
        if (center) {
            editTextView.setGravity()
        }
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT)
        container.addView(editTextView , index , lp)
        editTextView.setHtmlText(sequence)
    }

    private fun addCellTextLast(sequence : String) {
        val index = container.childCount
        val editTextView = KoalaEditTextView(context , onPressEnterListener , statusListener)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT)
        container.addView(editTextView , index , lp)
        editTextView.setHtmlText(sequence)
        editTextView.requestFocus()
    }

    fun addCellQuote(sequence : String , isAddLast : Boolean) {
        val index = getNextIndex(isAddLast)
        val editTextView = KoalaEditTextView(context , onPressEnterListener , statusListener)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT)
        container.addView(editTextView , index , lp)
        editTextView.setHtmlText(sequence)
        editTextView.setQuote()
        println(sequence)
        println(editTextView.obtainText().toString())
    }

    @JvmOverloads
    fun addCellH1(sequence : String , center : Boolean = false , isAddLast : Boolean = false) {
        val index = getNextIndex(isAddLast)
        val editTextView = KoalaEditTextView(context , onPressEnterListener , statusListener)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT)
        if (center) {
            editTextView.setGravity()
        }
        container.addView(editTextView , index , lp)
        editTextView.setHtmlText(sequence)
        editTextView.setStyleH1()
    }

    @JvmOverloads
    fun addCellH2(sequence : String , center : Boolean = false , isAddLast : Boolean = false) {
        val index = getNextIndex(isAddLast)
        val editTextView = KoalaEditTextView(context , onPressEnterListener , statusListener)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT)
        if (center) {
            editTextView.setGravity()
        }
        container.addView(editTextView , index , lp)
        editTextView.setHtmlText(sequence)
        editTextView.setStyleH2()
    }

    fun addCellList1(sequence : String , isAddLast : Boolean) {
        val index = getNextIndex(isAddLast)
        val editTextView = KoalaEditTextView(context , onPressEnterListener , statusListener)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT)
        container.addView(editTextView , index , lp)
        editTextView.setHtmlText(sequence)
        editTextView.setSection(KoalaEditTextView.SECTION_NUMBER)
        println(sequence)
        println(editTextView.obtainText().toString())
    }

    fun addCellList2(sequence : String , isAddLast : Boolean) {
        val index = getNextIndex(isAddLast)
        val editTextView = KoalaEditTextView(context , onPressEnterListener , statusListener)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT)
        container.addView(editTextView , index , lp)
        editTextView.setHtmlText(sequence)
        editTextView.setSection(KoalaEditTextView.SECTION_DOT)
        println(sequence)
    }

    private fun getNextIndex(isAddLast : Boolean) : Int {
        var index = container.childCount
        if (!isAddLast) {
            val v = container.focusedChild
            if (v != null) {
                index = container.indexOfChild(v) + 1
            }
        }
        return index
    }

    fun addFile(data : FileData? , addEmptyAfter : Boolean , addLast : Boolean) {
        if (null == data) {
            return
        }
        data.type = 1
        val index = getNextIndex(addLast)
        val cardView = KoalaFileView(context!! , data , itemFileListener.get())
        val lpCard = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT)
        container.addView(cardView , index , lpCard)
        if (addEmptyAfter && index == container.childCount - 1) {
            addCellTextLast("")
        }
    }

    fun deleteFile(position : Int) {
        val v = container.getChildAt(position)
        if (v is KoalaFileView) {
            container.removeView(v)
            for (i in 0 until container.childCount) {
                if (container.getChildAt(i) is KoalaImageView) {
                    val kcv = container.getChildAt(i) as KoalaImageView
                    kcv.reload()
                }
            }
        }
    }

    fun deleteImage(position : Int) {
        val v = container.getChildAt(position)
        if (v is KoalaImageView) {
            container.removeView(v)
            for (i in 0 until container.childCount) {
                if (container.getChildAt(i) is KoalaImageView) {
                    val kcv = container.getChildAt(i) as KoalaImageView
                    kcv.reload()
                }
            }
        }
    }

    private var itemImageListener : SoftReference<IOnImageClickListener?> = SoftReference(null)
    fun setOnImageClickListener(listener : IOnImageClickListener) {
        itemImageListener = SoftReference(listener)
    }

    private var itemFileListener : SoftReference<IOnFileClickListener?> = SoftReference(null)
    fun setOnFileClickListener(listener : IOnFileClickListener) {
        itemFileListener = SoftReference(listener)
    }

    fun addImage(fileData : FileData? , addEmptyAfter : Boolean , addLast : Boolean) {
        if (null == fileData) {
            return
        }
        fileData.type = 0
        val index = getNextIndex(addLast)
        val imageView : KoalaImageView
        if (TextUtils.isEmpty(fileData.filePath)) {
            //网络图片
            imageView = KoalaImageView(context , fileData , itemImageListener.get())
        } else {
            val h : Int
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(fileData.filePath , options)
            fileData.width = options.outWidth
            fileData.height = options.outHeight
            //本地图片
            val c : Boolean
            try {
                val exif = ExifInterface(fileData.filePath)
                val ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION ,
                                               ExifInterface.ORIENTATION_UNDEFINED)
                when (ori) {
                    ExifInterface.ORIENTATION_ROTATE_90 , ExifInterface.ORIENTATION_ROTATE_270 -> c = true
                    else -> c = false
                }
                if (c) {
                    val w = fileData.width
                    fileData.width = fileData.height
                    fileData.height = w
                }
            } catch (e : Exception) {

            }

            imageView = KoalaImageView(context , fileData , itemImageListener.get())
        }
        val lpimage = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT)
        container.addView(imageView , index , lpimage)
        if (addEmptyAfter && index == container.childCount - 1) {
            addCellTextLast("")
        }
    }

    fun addSlider() {
        var index = container.childCount
        val v = container.focusedChild
        if (v != null) {
            index = container.indexOfChild(v)
        }
        val sliderView = KoalaSliderView(context)
        val lpslider = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT)
        container.addView(sliderView , index + 1 , lpslider)
        val editTextView = KoalaEditTextView(context , onPressEnterListener , statusListener)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT)
        container.addView(editTextView , index + 2 , lp)
        editTextView.requestFocus()
        if (v is KoalaEditTextView) {
            val p : CharSequence
            val n : CharSequence
            val start = v.selectionStart
            if (start < v.obtainText().length) {
                p = v.obtainText().subSequence(0 , start)
                n = v.obtainText().subSequence(start , v.obtainText().length)
            } else {
                p = v.obtainText()
                n = ""
            }
            v.setText(p)
            editTextView.setText(n)
            editTextView.setSelection(0)
            editTextView.resetNextSection(editTextView)
        }
    }

    fun addCode() {
        var index = container.childCount
        val v = container.focusedChild
        if (v != null) {
            index = container.indexOfChild(v)
        }
        val codeView = KoalaEditTextView(context , onPressEnterListener , statusListener)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT)
        container.addView(codeView , index + 1 , lp)
        codeView.requestFocus()
        codeView.addCode()
    }

    override fun onDetachedFromWindow() {
        for (i in 0 until container.childCount) {
            if (container.getChildAt(i) is KoalaBaseCellView) {
                val v = container.getChildAt(i) as KoalaBaseCellView
                v.release()
            }
        }
        super.onDetachedFromWindow()
    }

    fun setHint() {
        if (container.getChildAt(0) is KoalaEditTextView) {
            val v = container.getChildAt(0) as KoalaEditTextView
            if (container.childCount == 1 && v.edit_text!!.length() == 0) {
                v.edit_text!!.hint = "输入内容"
            } else {
                v.edit_text!!.hint = ""
            }
        }
    }

    fun clearHint() {
        if (container.getChildAt(0) is KoalaEditTextView) {
            val v = container.getChildAt(0) as KoalaEditTextView
            v.edit_text!!.hint = ""
        }
    }

    interface OnStatusListener {
        fun setEnableKeyBoard(enableKeyBoard : Boolean)
    }

    interface OnEditTextChangedListener {
        fun onEditTextTextChanged(status : Int)
    }

    companion object {

        internal fun getPrev(parent : ViewGroup , v : View) : KoalaBaseCellView? {
            val index = parent.indexOfChild(v)
            if (index > 0) {
                val prev = parent.getChildAt(index - 1)
                if (prev is KoalaBaseCellView) {
                    return prev
                }
            }
            return null
        }

        internal fun getNext(parent : ViewGroup , v : View) : KoalaBaseCellView? {
            val index = parent.indexOfChild(v)
            if (index < parent.childCount - 1) {
                val next = parent.getChildAt(index + 1)
                if (next is KoalaBaseCellView) {
                    return next
                }
            }
            return null
        }

        interface IOnImageClickListener {
            fun onImageClick(position : Int)
        }

        interface IOnFileClickListener {
            fun onFileClick(position : Int)
        }

    }
}
