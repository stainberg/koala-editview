package com.stainberg.keditview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by Stainberg on 7/5/17.
 */

public class KoalaSliderView extends FrameLayout implements KoalaBaseCellView {

    private int index;
    private KoalaBaseCellView prev;
    private KoalaBaseCellView next;

    public KoalaSliderView(Context context) {
        super(context);
        init(context);
    }

    public KoalaSliderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public KoalaSliderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View v = new View(context);
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.slider_heigh));
        v.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.gray_placeholder_bg));
        addView(v ,lp);
    }

    @Override
    public void setPosition(int idx) {
        index = idx;
    }

    @Override
    public int getPosition() {
        return index;
    }

    @Override
    public void setPrevView(KoalaBaseCellView v) {
        prev = v;
    }

    @Override
    public void setNextView(KoalaBaseCellView v) {
        next = v;
    }

    @Override
    public KoalaBaseCellView getPrevView() {
        return prev;
    }

    @Override
    public KoalaBaseCellView getNextView() {
        return next;
    }

    @Deprecated
    @Override
    public String getUrl() {
        return "";
    }

    @Override
    public int getType() {
        return SLIDER_VIEW;
    }

    @Deprecated
    @Override
    public void reload() {

    }

    @Deprecated
    @Override
    public void setStyleH1() {

    }

    @Deprecated
    @Override
    public void setStyleH2() {

    }

    @Deprecated
    @Override
    public void setStyleNormal() {

    }

    @Deprecated
    @Override
    public void setGravity() {

    }

    @Deprecated
    @Override
    public void setQuote() {

    }

    @Deprecated
    @Override
    public void setSection(int st) {

    }

    @Deprecated
    @Override
    public void setBold() {

    }

    @Deprecated
    @Override
    public void setItalic() {

    }

    @Deprecated
    @Override
    public void setStrike() {

    }

    @Deprecated
    @Override
    public void addCode() {

    }

    @Deprecated
    @Override
    public boolean isQuote() {
        return false;
    }

    @Deprecated
    @Override
    public boolean isCode() {
        return false;
    }

    @Override
    public int getStyle() {
        return 0;
    }

    @Override
    public int getSection() {
        return 0;
    }

    @Deprecated
    @Override
    public int getImageWidth() {
        return 0;
    }

    @Deprecated
    @Override
    public int getImageHeight() {
        return 0;
    }

    @Override
    public void setEditable(boolean enable) {

    }

    @Override
    public void enableDrag(boolean enable) {

    }

    @Deprecated
    @Override
    public void setText(CharSequence sequence) {

    }

    @Deprecated
    @Override
    public CharSequence getText() {
        return "";
    }

    @Deprecated
    @Override
    public String getHtmlText() {
        return "";
    }

    @Override
    public void setHtmlText(String html) {

    }
}
