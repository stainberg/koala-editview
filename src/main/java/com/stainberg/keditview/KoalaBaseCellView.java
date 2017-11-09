package com.stainberg.keditview;

import java.util.List;

/**
 * Created by Stainberg on 7/5/17.
 */

public interface KoalaBaseCellView {

    int EDIT_VIEW = 1;
    int IMAGE_VIEW = 2;
    int SLIDER_VIEW = 3;
    int CARD_VIEW = 4;
    int CARD_URL_VIEW = 5;
    int CARD_VIDEO_VIEW = 6;
    int CARD_MUSIC_VIEW = 7;
    int CARD_FILE_VIEW = 8;
    int CARD_SHOP_VIEW = 9;
    int CARD_APP_VIEW = 10;

    String getUrl();
    int getType();
    void reload();
    void setStyleH1();
    void setStyleH2();
    void setStyleNormal();
    void setGravity();
    void setQuote();
    void setSection(int st);
    void setBold();
    void setItalic();
    void setStrike();
    void addCode();
    void setText(CharSequence sequence);
    CharSequence getText();
    List<String> getHtmlText();
    void setHtmlText(String html);
    boolean isQuote();
    boolean isCode();
    int getStyle();
    int getSection();
    void setEditable(boolean enable);
    void enableDrag(boolean enable);
    void release();
}
