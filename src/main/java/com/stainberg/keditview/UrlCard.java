package com.stainberg.keditview;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wanbo on 2017/6/13.
 */

public class UrlCard implements Serializable {

    public String duration;
    public String short_desc;
    public String thumbnail;
    public String title;
    public String url = "";
    public String type;
    public String sitename;
    public transient String typeI = "";

    private List<String> getCardTypeList(){
        List<String> list = new ArrayList<>();
        list.add("play.google");
        list.add("itunes.apple");
        list.add("appsto.re");
        list.add("youtube");
        list.add("viemo");
        list.add("bilibili");
        list.add("acfun");
        list.add("youku");
        list.add("v.qq");
        list.add("y.qq");
        list.add("music.163");
        list.add("xiami.com");
        list.add("spotify.com");
        list.add("itun.es");
        list.add("soundcloud.com");
        list.add("amazon");
        list.add("jd");
        list.add("taobao");
        return list;
    }

    public void setCardType(){
        if(!TextUtils.isEmpty(this.url)){
            for(String type : getCardTypeList()){
                if(this.url.contains(type)){
                    this.typeI = type;
                    return;
                }
            }

        }
    }


}
