package com.stainberg.keditview;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

/**
 * Created by Stainberg on 7/5/17.
 */

public class KoalaCardView extends FrameLayout implements KoalaBaseCellView {

    private View move;
    private int index;
    private KoalaBaseCellView prev;
    private KoalaBaseCellView next;
    private UrlCard data;

    public KoalaCardView(Context context) {
        super(context);
        init(context);
    }

    public KoalaCardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public KoalaCardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public KoalaCardView(Context context, UrlCard d) {
        super(context);
        data = d;
        init(context);
    }


    private void init(Context context) {
        View v;
        int cardHeight;
        switch (CardInfo.getTypeByCode(data.type)) {
            case KoalaCardView.CARD_APP_VIEW:
                cardHeight = (int) getResources().getDimension(R.dimen.card_height);
                v = View.inflate(context, R.layout.item_view_card_url_app, null);
                TextView titleApp = v.findViewById(R.id.url_title);
                titleApp.setText(data.title);
                TextView descApp = v.findViewById(R.id.url_desc);
                descApp.setText(data.short_desc);
                TextView urlApp = v.findViewById(R.id.url);
                urlApp.setText(data.sitename);
                SimpleDraweeView imageApp = v.findViewById(R.id.url_thumb);
                imageApp.setImageURI(getThumbnailUrl(data.thumbnail));
                SimpleDraweeView iconApp = v.findViewById(R.id.card_type_image);
                iconApp.setImageURI(CardInfo.getIconByCode(data.typeI));
                if(!TextUtils.isEmpty(data.typeI)) {
                    iconApp.setVisibility(VISIBLE);
                }
                iconApp.setVisibility(VISIBLE);
                break;
            case KoalaCardView.CARD_VIDEO_VIEW:
                cardHeight = (int) getResources().getDimension(R.dimen.card_height);
                v = View.inflate(context, R.layout.item_view_card_video, null);
                TextView titleVideo = v.findViewById(R.id.url_title);
                titleVideo.setText(data.title);
                TextView descVideo = v.findViewById(R.id.url_desc);
                descVideo.setText(data.short_desc);
                TextView urlVideo = v.findViewById(R.id.url);
                urlVideo.setText(data.sitename);
                SimpleDraweeView imageVideo = v.findViewById(R.id.url_thumb);
                imageVideo.setImageURI(getThumbnailUrl(data.thumbnail));
                SimpleDraweeView iconVideo = v.findViewById(R.id.card_type_image);
                iconVideo.setImageURI(CardInfo.getIconByCode(data.typeI));
                if(!TextUtils.isEmpty(data.typeI)) {
                    iconVideo.setVisibility(VISIBLE);
                }
                break;
            case KoalaCardView.CARD_MUSIC_VIEW:
                cardHeight = (int) getResources().getDimension(R.dimen.card_height);
                v = View.inflate(context, R.layout.item_view_card_music, null);
                TextView titleMusic = v.findViewById(R.id.url_title);
                titleMusic.setText(data.title);
                TextView descMusic = v.findViewById(R.id.url_desc);
                descMusic.setText(data.short_desc);
                TextView urlMusic = v.findViewById(R.id.url);
                urlMusic.setText(data.sitename);
                SimpleDraweeView imageMusic = v.findViewById(R.id.url_thumb);
                imageMusic.setImageURI(getThumbnailUrl(data.thumbnail));
                SimpleDraweeView iconMusic = v.findViewById(R.id.card_type_image);
                iconMusic.setImageURI(CardInfo.getIconByCode(data.typeI));
                if(!TextUtils.isEmpty(data.typeI)) {
                    iconMusic.setVisibility(VISIBLE);
                }
                break;
            case KoalaCardView.CARD_FILE_VIEW:
                cardHeight = (int) getResources().getDimension(R.dimen.card_file_height);
                v = View.inflate(context, R.layout.item_view_card_file, null);
                TextView titleFile = v.findViewById(R.id.url_title);
                titleFile.setText(data.title);
                TextView descFile = v.findViewById(R.id.url_desc);
                descFile.setText(data.short_desc);
                SimpleDraweeView imageFile = v.findViewById(R.id.url_thumb);
                imageFile.setImageURI(getThumbnailUrl(data.thumbnail));
                break;
            case KoalaCardView.CARD_SHOP_VIEW:
                cardHeight = (int) getResources().getDimension(R.dimen.card_height);
                v = View.inflate(context, R.layout.item_view_card_shop, null);
                TextView titleShop = v.findViewById(R.id.url_title);
                titleShop.setText(data.title);
                TextView descShop = v.findViewById(R.id.url_desc);
                descShop.setText(data.short_desc);
                TextView urlShop = v.findViewById(R.id.url);
                urlShop.setText(data.sitename);
                SimpleDraweeView imageShop = v.findViewById(R.id.url_thumb);
                imageShop.setImageURI(getThumbnailUrl(data.thumbnail));
                SimpleDraweeView iconShop = v.findViewById(R.id.card_type_image);
                iconShop.setImageURI(CardInfo.getIconByCode(data.typeI));
                if(!TextUtils.isEmpty(data.typeI)) {
                    iconShop.setVisibility(VISIBLE);
                }
                break;
            default:
                cardHeight = (int) getResources().getDimension(R.dimen.card_height);
                v = View.inflate(context, R.layout.item_view_card_url_app, null);
                TextView titleLink = v.findViewById(R.id.url_title);
                titleLink.setText(data.title);
                TextView descLink = v.findViewById(R.id.url_desc);
                descLink.setText(data.short_desc);
                TextView urlLink = v.findViewById(R.id.url);
                urlLink.setText(data.sitename);
                SimpleDraweeView imageLink = v.findViewById(R.id.url_thumb);
                imageLink.setImageURI(getThumbnailUrl(data.thumbnail));
                SimpleDraweeView iconLink = v.findViewById(R.id.card_type_image);
                iconLink.setImageURI(CardInfo.getIconByCode(data.typeI));
                if(!TextUtils.isEmpty(data.typeI)) {
                    iconLink.setVisibility(VISIBLE);
                }
                break;
        }
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, cardHeight);
        lp.topMargin = (int) getResources().getDimension(R.dimen.cell_top_margin);
        lp.bottomMargin = (int) getResources().getDimension(R.dimen.cell_bottom_margin);
        v.setBackgroundResource(R.drawable.shape_card_bg);
        addView(v ,lp);
        move = new View(context);
        FrameLayout.LayoutParams l0 = new FrameLayout.LayoutParams(120, 60);
        l0.gravity = Gravity.END;
        move.setBackgroundColor(Color.parseColor("#00FF00"));
        addView(move, l0);
        move.setVisibility(GONE);
    }

    private  String getThumbnailUrl(String url) {
        return (TextUtils.isEmpty(url)) ? CardInfo.getIconByCode("default") : url;
    }

    public void enableDrag(boolean enable) {
        if(enable) {
            move.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    KoalaCardView.this.startDrag(ClipData.newPlainText("text", data.url), new KoalaDragShadowBuilder(KoalaCardView.this), new DragState(KoalaCardView.this), 0);
                    return true;
                }
            });
            move.setVisibility(VISIBLE);
        } else {
            move.setOnLongClickListener(null);
            move.setVisibility(GONE);
        }
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
        return CARD_VIEW;
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

    public UrlCard getData() {
        return data;
    }
}
