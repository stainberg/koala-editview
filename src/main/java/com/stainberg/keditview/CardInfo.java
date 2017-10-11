package com.stainberg.keditview;

import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wanbo on 2017/6/13.
 */

public class CardInfo {

    private static Map<String, Integer> typeMap;
    private static Map<String, String> iconMap;

    public static int getTypeByCode(String code) {
        if (typeMap.containsKey(code)) {
            return typeMap.get(code);
        } else {
            return KoalaBaseCellView.CARD_URL_VIEW;
        }
    }


    public static String getIconByCode(String code) {
        if(code.startsWith("http")){
            return code;
        }
        if (iconMap.containsKey(code)) {
            return iconMap.get(code);
        } else {
            return "";
        }
    }

    static {
        typeMap = new HashMap<>();
        iconMap = new HashMap<>();

        typeMap.put("app", KoalaBaseCellView.CARD_APP_VIEW);
        typeMap.put("video", KoalaBaseCellView.CARD_VIDEO_VIEW);
        typeMap.put("music", KoalaBaseCellView.CARD_MUSIC_VIEW);
        typeMap.put("shop", KoalaBaseCellView.CARD_SHOP_VIEW);

        iconMap.put("play.google", "https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/android@3x.png");
        iconMap.put("itunes.apple","https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/iOS@3x.png");
        iconMap.put("appsto.re", "https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/iOS@3x.png");
        iconMap.put("bilibili", "https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/Bilibili@3x.png");
        iconMap.put("youku", "https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/youku video@3x.png");
        iconMap.put("v.qq", "https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/Tencent Video@3x.png");
        iconMap.put("y.qq", "https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/QQmusic@3x.png");
        iconMap.put("music.163", "https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/Netease music@3x.png");
        iconMap.put("xiami.com", "https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/Xiami@3x.png");
        iconMap.put("itun.es", "https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/iOS@3x.png");
        iconMap.put("amazon", "https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/Amazon@3x.png");
        iconMap.put("jd", "https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/JD@3x.png");
        iconMap.put("taobao", "https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/taobao@3x.png");
        iconMap.put("default", "https://morespace-assets.oss-cn-beijing.aliyuncs.com/urlparser/default/image@3x.png");
    }

    public static String buildImageResize(View view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        return "?x-oss-process=image/resize,m_fill,w_" + String.valueOf(params.width) + ",h_" + String.valueOf(params.height);
    }

}
