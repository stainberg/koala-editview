package com.stainberg.keditview

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView

import com.facebook.drawee.view.SimpleDraweeView

/**
 * Created by Stainberg on 7/5/17.
 */

class KoalaCardView : FrameLayout , KoalaBaseCellView {

    private var move : View? = null
    var data : UrlCard? = null

    constructor(context : Context) : super(context) {
        init(context)
    }

    constructor(context : Context , attrs : AttributeSet?) : super(context , attrs) {
        init(context)
    }

    constructor(context : Context , attrs : AttributeSet? , defStyleAttr : Int) : super(context , attrs , defStyleAttr) {
        init(context)
    }

    constructor(context : Context , d : UrlCard) : super(context) {
        data = d
        init(context)
    }


    private fun init(context : Context) {
        val data = data ?: return
        val v : View
        val cardHeight : Int
        when (CardInfo.getTypeByCode(data.type)) {
            KoalaBaseCellView.CARD_APP_VIEW -> {
                cardHeight = resources.getDimension(R.dimen.card_height).toInt()
                v = View.inflate(context , R.layout.item_view_card_url_app , null)
                val titleApp = v.findViewById<TextView>(R.id.url_title)
                titleApp.text = data.title
                val descApp = v.findViewById<TextView>(R.id.url_desc)
                descApp.text = data.short_desc
                val urlApp = v.findViewById<TextView>(R.id.url)
                urlApp.text = data.sitename
                val imageApp = v.findViewById<SimpleDraweeView>(R.id.url_thumb)
                imageApp.setImageURI(getThumbnailUrl(data.thumbnail))
                val iconApp = v.findViewById<SimpleDraweeView>(R.id.card_type_image)
                iconApp.setImageURI(CardInfo.getIconByCode(data.typeI))
                if (!TextUtils.isEmpty(data.typeI)) {
                    iconApp.visibility = View.VISIBLE
                }
                iconApp.visibility = View.VISIBLE
            }
            KoalaBaseCellView.CARD_VIDEO_VIEW -> {
                cardHeight = resources.getDimension(R.dimen.card_height).toInt()
                v = View.inflate(context , R.layout.item_view_card_video , null)
                val titleVideo = v.findViewById<TextView>(R.id.url_title)
                titleVideo.text = data.title
                val descVideo = v.findViewById<TextView>(R.id.url_desc)
                descVideo.text = data.short_desc
                val urlVideo = v.findViewById<TextView>(R.id.url)
                urlVideo.text = data.sitename
                val imageVideo = v.findViewById<SimpleDraweeView>(R.id.url_thumb)
                imageVideo.setImageURI(getThumbnailUrl(data.thumbnail))
                val iconVideo = v.findViewById<SimpleDraweeView>(R.id.card_type_image)
                iconVideo.setImageURI(CardInfo.getIconByCode(data.typeI))
                if (!TextUtils.isEmpty(data.typeI)) {
                    iconVideo.visibility = View.VISIBLE
                }
            }
            KoalaBaseCellView.CARD_MUSIC_VIEW -> {
                cardHeight = resources.getDimension(R.dimen.card_height).toInt()
                v = View.inflate(context , R.layout.item_view_card_music , null)
                val titleMusic = v.findViewById<TextView>(R.id.url_title)
                titleMusic.text = data.title
                val descMusic = v.findViewById<TextView>(R.id.url_desc)
                descMusic.text = data.short_desc
                val urlMusic = v.findViewById<TextView>(R.id.url)
                urlMusic.text = data.sitename
                val imageMusic = v.findViewById<SimpleDraweeView>(R.id.url_thumb)
                imageMusic.setImageURI(getThumbnailUrl(data.thumbnail))
                val iconMusic = v.findViewById<SimpleDraweeView>(R.id.card_type_image)
                iconMusic.setImageURI(CardInfo.getIconByCode(data.typeI))
                if (!TextUtils.isEmpty(data.typeI)) {
                    iconMusic.visibility = View.VISIBLE
                }
            }
            KoalaBaseCellView.CARD_FILE_VIEW -> {
                cardHeight = resources.getDimension(R.dimen.card_file_height).toInt()
                v = View.inflate(context , R.layout.item_view_card_file , null)
                val titleFile = v.findViewById<TextView>(R.id.url_title)
                titleFile.text = data.title
                val descFile = v.findViewById<TextView>(R.id.url_desc)
                descFile.text = data.short_desc
                val imageFile = v.findViewById<SimpleDraweeView>(R.id.url_thumb)
                imageFile.setImageURI(getThumbnailUrl(data.thumbnail))
            }
            KoalaBaseCellView.CARD_SHOP_VIEW -> {
                cardHeight = resources.getDimension(R.dimen.card_height).toInt()
                v = View.inflate(context , R.layout.item_view_card_shop , null)
                val titleShop = v.findViewById<TextView>(R.id.url_title)
                titleShop.text = data.title
                val descShop = v.findViewById<TextView>(R.id.url_desc)
                descShop.text = data.short_desc
                val urlShop = v.findViewById<TextView>(R.id.url)
                urlShop.text = data.sitename
                val imageShop = v.findViewById<SimpleDraweeView>(R.id.url_thumb)
                imageShop.setImageURI(getThumbnailUrl(data.thumbnail))
                val iconShop = v.findViewById<SimpleDraweeView>(R.id.card_type_image)
                iconShop.setImageURI(CardInfo.getIconByCode(data.typeI))
                if (!TextUtils.isEmpty(data.typeI)) {
                    iconShop.visibility = View.VISIBLE
                }
            }
            else -> {
                cardHeight = resources.getDimension(R.dimen.card_height).toInt()
                v = View.inflate(context , R.layout.item_view_card_url_app , null)
                val titleLink = v.findViewById<TextView>(R.id.url_title)
                titleLink.text = data.title
                val descLink = v.findViewById<TextView>(R.id.url_desc)
                descLink.text = data.short_desc
                val urlLink = v.findViewById<TextView>(R.id.url)
                urlLink.text = data.sitename
                val imageLink = v.findViewById<SimpleDraweeView>(R.id.url_thumb)
                imageLink.setImageURI(getThumbnailUrl(data.thumbnail))
                val iconLink = v.findViewById<SimpleDraweeView>(R.id.card_type_image)
                iconLink.setImageURI(CardInfo.getIconByCode(data.typeI))
                if (!TextUtils.isEmpty(data.typeI)) {
                    iconLink.visibility = View.VISIBLE
                }
            }
        }
        val lp = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , cardHeight)
        lp.topMargin = resources.getDimension(R.dimen.cell_top_margin).toInt()
        lp.bottomMargin = resources.getDimension(R.dimen.cell_bottom_margin).toInt()
        v.setBackgroundResource(R.drawable.shape_card_bg)
        addView(v , lp)
        move = View(context)
        move!!.id = R.id.edit_icon_drag
        val l0 = FrameLayout.LayoutParams(120 , 60)
        l0.gravity = Gravity.END
        move!!.setBackgroundColor(Color.parseColor("#00FF00"))
        addView(move , l0)
        move!!.visibility = View.GONE
    }

    private fun getThumbnailUrl(url : String) : String {
        return if (TextUtils.isEmpty(url)) CardInfo.getIconByCode("default") else url
    }

    override fun enableDrag(enable : Boolean) {
        if (enable) {
            move!!.visibility = View.VISIBLE
        } else {
            move!!.visibility = View.GONE
        }
    }

    @Deprecated("")
    override fun resetMargin() {

    }
}
