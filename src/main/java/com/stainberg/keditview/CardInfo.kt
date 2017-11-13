package com.stainberg.keditview

import android.view.View
import android.view.ViewGroup

import java.util.HashMap

/**
 * Created by wanbo on 2017/6/13.
 */

object CardInfo {

    private var typeMap : MutableMap<String , Int>? = null
    private var iconMap : MutableMap<String , String>? = null

    fun getTypeByCode(code : String) : Int {
        return if (typeMap!!.containsKey(code)) {
            typeMap!![code] ?: KoalaBaseCellView.CARD_URL_VIEW
        } else {
            KoalaBaseCellView.CARD_URL_VIEW
        }
    }


    fun getIconByCode(code : String) : String {
        if (code.startsWith("http")) {
            return code
        }
        return if (iconMap!!.containsKey(code)) {
            iconMap!![code] ?: ""
        } else {
            ""
        }
    }

    init {
        typeMap = HashMap()
        iconMap = HashMap()

        typeMap!!.put("app" , KoalaBaseCellView.CARD_APP_VIEW)
        typeMap!!.put("video" , KoalaBaseCellView.CARD_VIDEO_VIEW)
        typeMap!!.put("music" , KoalaBaseCellView.CARD_MUSIC_VIEW)
        typeMap!!.put("shop" , KoalaBaseCellView.CARD_SHOP_VIEW)

        iconMap!!.put("play.google" , "https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/android@3x.png")
        iconMap!!.put("itunes.apple" , "https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/iOS@3x.png")
        iconMap!!.put("appsto.re" , "https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/iOS@3x.png")
        iconMap!!.put("bilibili" , "https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/Bilibili@3x.png")
        iconMap!!.put("youku" , "https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/youku video@3x.png")
        iconMap!!.put("v.qq" , "https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/Tencent Video@3x.png")
        iconMap!!.put("y.qq" , "https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/QQmusic@3x.png")
        iconMap!!.put("music.163" , "https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/Netease music@3x.png")
        iconMap!!.put("xiami.com" , "https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/Xiami@3x.png")
        iconMap!!.put("itun.es" , "https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/iOS@3x.png")
        iconMap!!.put("amazon" , "https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/Amazon@3x.png")
        iconMap!!.put("jd" , "https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/JD@3x.png")
        iconMap!!.put("taobao" , "https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/taobao@3x.png")
        iconMap!!.put("default" , "https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/default/image@3x.png")
    }

    fun buildImageResize(view : View) : String {
        val params = view.layoutParams
        return "?x-oss-process=image/resize,m_fill,w_" + params.width.toString() + ",h_" + params.height.toString()
    }

}
