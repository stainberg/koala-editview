package com.stainberg.keditview

/**
 * Created by Stainberg on 7/5/17.
 */

interface KoalaBaseCellView {

    fun resetMargin()

    fun enableDrag(enable: Boolean)

    companion object {

        val EDIT_VIEW = 1
        val IMAGE_VIEW = 2
        val SLIDER_VIEW = 3
        val CARD_VIEW = 4
        val CARD_URL_VIEW = 5
        val CARD_VIDEO_VIEW = 6
        val CARD_MUSIC_VIEW = 7
        val CARD_FILE_VIEW = 8
        val CARD_SHOP_VIEW = 9
        val CARD_APP_VIEW = 10
    }
}
