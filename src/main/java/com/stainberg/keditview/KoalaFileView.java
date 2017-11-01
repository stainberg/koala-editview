package com.stainberg.keditview;

import android.content.ClipData;
import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

/**
 * Created by Stainberg on 7/5/17.
 */

public class KoalaFileView extends FrameLayout implements KoalaBaseCellView {

    public KoalaFileView(@NonNull Context context) {
        this(context, null, 0);
    }

    public KoalaFileView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KoalaFileView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public KoalaFileView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private FileData fileData;
    private View drag;

    public KoalaFileView(Context context, FileData fileData) {
        this(context);
        this.fileData = fileData;
        if (getChildCount() == 1) {
            View container = getChildAt(0);
            SimpleDraweeView icon = container.findViewById(R.id.icon);
            TextView iconText = container.findViewById(R.id.icon_text);
            TextView title = container.findViewById(R.id.title);
            TextView desc = container.findViewById(R.id.desc);
            int colorId = R.color.color_unknwon;
            if (fileData != null) {
                if (fileData.iconResId != 0) {
                    icon.setImageResource(fileData.iconResId);
                    iconText.setBackground(null);
                } else if (!TextUtils.isEmpty(fileData.iconUrl)) {
                    icon.setImageURI(fileData.iconUrl);
                    iconText.setBackground(null);
                } else {
                    String type = fileData.getFileType();
                    switch (type) {
                        case FileData.DOC:
                        case FileData.DOCX:
                            iconText.setBackgroundResource(R.drawable.svg_file_doc);
//                            colorId = R.color.color_word;
                            break;
                        case FileData.PDF:
                            iconText.setBackgroundResource(R.drawable.svg_file_pdf);
//                            colorId = R.color.color_pdf;
                            break;
                        case FileData.PPT:
                        case FileData.PPTX:
                            iconText.setBackgroundResource(R.drawable.svg_file_ppt);
//                            colorId = R.color.color_ppt;
                            break;
                        case FileData.EPUB:
                            iconText.setBackgroundResource(R.drawable.svg_file_epub);
//                            colorId = R.color.color_epub;
                            break;
                        case FileData.TXT:
                            iconText.setBackgroundResource(R.drawable.svg_file_txt);
//                            colorId = R.color.color_txt;
                            break;
                        case FileData.XLS:
                        case FileData.XLSX:
                            iconText.setBackgroundResource(R.drawable.svg_file_xls);
//                            colorId = R.color.color_excel;
                            break;
                        default:
                            iconText.setBackgroundResource(R.drawable.svg_file_unknown);
//                            colorId = R.color.color_unknwon;
                            break;
                    }
                    colorId = R.color.white_card_bg;
                }
                iconText.setText(TextUtils.isEmpty(fileData.getFileType()) ? "" : fileData.getFileType());
                title.setText(TextUtils.isEmpty(fileData.fileName) ? "" : fileData.fileName);
                desc.setText(TextUtils.isEmpty(fileData.desc) ? "" : fileData.desc);
            } else {
                iconText.setBackgroundResource(R.drawable.svg_file_unknown);
                iconText.setText(TextUtils.isEmpty(fileData.getFileType()) ? "" : fileData.getFileType());
                title.setText(TextUtils.isEmpty(fileData.fileName) ? "" : fileData.fileName);
                desc.setText(TextUtils.isEmpty(fileData.desc) ? "" : fileData.desc);
            }
            iconText.setTextColor(getContext().getResources().getColor(colorId));
        }
    }

    public FileData getFileData() {
        return fileData;
    }

    private void init() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.item_view_file, this, true);
        drag = v.findViewById(R.id.icon_drag);
    }

    @Override
    public void setPosition(int index) {

    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    public void setPrevView(KoalaBaseCellView v) {

    }

    @Override
    public void setNextView(KoalaBaseCellView v) {

    }

    @Override
    public KoalaBaseCellView getPrevView() {
        return null;
    }

    @Override
    public KoalaBaseCellView getNextView() {
        return null;
    }

    @Override
    public String getUrl() {
        return "";
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public void reload() {

    }

    @Override
    public void setStyleH1() {

    }

    @Override
    public void setStyleH2() {

    }

    @Override
    public void setStyleNormal() {

    }

    @Override
    public void setGravity() {

    }

    @Override
    public void setQuote() {

    }

    @Override
    public void setSection(int st) {

    }

    @Override
    public void setBold() {

    }

    @Override
    public void setItalic() {

    }

    @Override
    public void setStrike() {

    }

    @Override
    public void addCode() {

    }

    @Override
    public void setText(CharSequence sequence) {

    }

    @Override
    public CharSequence getText() {
        return "";
    }

    @Override
    public String getHtmlText() {
        return "";
    }

    @Override
    public void setHtmlText(String html) {

    }

    @Override
    public boolean isQuote() {
        return false;
    }

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

    @Override
    public int getImageWidth() {
        return 0;
    }

    @Override
    public int getImageHeight() {
        return 0;
    }

    @Override
    public void setEditable(boolean enable) {

    }

    @Override
    public void enableDrag(boolean enable) {
        if (enable) {
            drag.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    KoalaFileView.this.startDrag(ClipData.newPlainText("text", getUrl()), new KoalaDragShadowBuilder(KoalaFileView.this), new DragState(KoalaFileView.this), 0);
                    return true;
                }
            });
            drag.setVisibility(VISIBLE);
        } else {
            drag.setOnLongClickListener(null);
            drag.setVisibility(GONE);
        }
    }
}