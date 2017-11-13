package com.stainberg.keditview

import android.text.TextUtils

import java.io.Serializable
import java.util.ArrayList

/**
 * Created by wanbo on 2017/6/13.
 */

class UrlCard : Serializable {

    var duration : String? = null
    var short_desc : String? = null
    var thumbnail : String = ""
    var title : String? = null
    var url = ""
    var type : String = ""
    var sitename : String? = null
    @Transient
    var typeI = ""

    private val cardTypeList : List<String>
        get() {
            val list = ArrayList<String>()
            list.add("play.google")
            list.add("itunes.apple")
            list.add("appsto.re")
            list.add("youtube")
            list.add("viemo")
            list.add("bilibili")
            list.add("acfun")
            list.add("youku")
            list.add("v.qq")
            list.add("y.qq")
            list.add("music.163")
            list.add("xiami.com")
            list.add("spotify.com")
            list.add("itun.es")
            list.add("soundcloud.com")
            list.add("amazon")
            list.add("jd")
            list.add("taobao")
            return list
        }

    fun setCardType() {
        if (!TextUtils.isEmpty(this.url)) {
            for (type in cardTypeList) {
                if (this.url.contains(type)) {
                    this.typeI = type
                    return
                }
            }

        }
    }
}
