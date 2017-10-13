package com.stainberg.keditview;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;

/**
 * Created by Stainberg on 7/5/17.
 */

public class KoalaImageView extends FrameLayout implements KoalaBaseCellView {

    private View move;
    private SimpleDraweeView imageView;
    private ImageView delete;
    private int index;
    private KoalaBaseCellView prev;
    private KoalaBaseCellView next;
    private OnImageDeleteListener listener;
    private String src;
    private boolean visible;
    private int width, height;
    private Bitmap bitmap;
    private int bound = getResources().getDisplayMetrics().heightPixels;

    public KoalaImageView(Context context) {
        super(context);
        init(context);
    }

    public KoalaImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public KoalaImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public KoalaImageView(Context context, OnImageDeleteListener l, int w, int h) {
        super(context);
        width = w;
        height = h;
        listener = l;
        init(context);
    }

    private void init(Context context) {
        setOnClickListener(onClickListener);
        imageView = new SimpleDraweeView(context);
        imageView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.gray_placeholder));
        ViewGroup.LayoutParams lpimage = new ViewGroup.LayoutParams(width, height);
        addView(imageView, lpimage);
        delete = new ImageView(context);
        delete.setOnClickListener(onDeleteImageListener);
        delete.setVisibility(GONE);
        delete.setImageResource(R.mipmap.icon_edit_close);
        LayoutParams lpdelete = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpdelete.gravity = Gravity.END;
        lpdelete.topMargin = 24;
        lpdelete.rightMargin = 24;
        addView(delete, lpdelete);
        visible = false;
        getViewTreeObserver().addOnScrollChangedListener(onScrollChangedListener);
        move = new View(context);
        FrameLayout.LayoutParams l0 = new FrameLayout.LayoutParams(120, 60);
        l0.gravity = Gravity.END;
        move.setBackgroundColor(Color.parseColor("#00FF00"));
        addView(move, l0);
        move.setVisibility(GONE);
    }

    @Override
    protected void onDetachedFromWindow() {
        getViewTreeObserver().removeOnScrollChangedListener(onScrollChangedListener);
        releaseImage();
        super.onDetachedFromWindow();
    }

    public void setImageSource(String source) {
        src = source;
        reloadImage();
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

    @Override
    public String getUrl() {
        return src;
    }

    @Override
    public int getType() {
        return IMAGE_VIEW;
    }

    @Override
    public void reload() {
        int[] location = new int[2];
        imageView.getLocationInWindow(location);
        if(location[1] < 0) {
            if(location[1] < -(height + bound) && visible) {
                visible = false;
                return;
            }
            if(location[1] > -(height + bound) && !visible) {
                visible = true;
                reloadImage();
            }
        } else {
            if((location[1] > getResources().getDisplayMetrics().heightPixels + bound && visible)) {
                visible = false;
                return;
            }
            if((location[1] < getResources().getDisplayMetrics().heightPixels + bound && !visible)) {
                visible = true;
                reloadImage();
            }
        }
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

    @Override
    public int getImageWidth() {
        return width;
    }

    @Override
    public int getImageHeight() {
        return height;
    }

    @Override
    public void setEditable(boolean enable) {

    }

    public void enableDrag(boolean enable) {
        if(enable) {
            move.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    KoalaImageView.this.startDrag(ClipData.newPlainText("text", getUrl()), new KoalaDragShadowBuilder(KoalaImageView.this), new DragState(KoalaImageView.this), 0);
                    return true;
                }
            });
            move.setVisibility(VISIBLE);
        } else {
            move.setOnLongClickListener(null);
            move.setVisibility(GONE);
        }
    }

    private void reloadImage() {
        System.out.println("reload bitmap");
        if(src.startsWith("http")) {
            imageView.setImageURI(src);
        } else {
            if(bitmap != null) {
                return;
            }
            KoalaImageLoadPoll.getPoll().handle(new Runnable() {
                @Override
                public void run() {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(src, options);
                    int w = options.outWidth;
                    float scale = w / width;
                    int s = (int) Math.ceil(scale);
                    options.inJustDecodeBounds = false;
                    options.inSampleSize = s;
                    bitmap = BitmapFactory.decodeFile(src, options);
                    post(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                }
            });
        }
    }

    private void releaseImage() {
        System.out.println("release bitmap");
        if(src.startsWith("http")) {
            imageView.setImageBitmap(null);
            imageView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.gray_placeholder));
        } else {
            if (bitmap != null && !bitmap.isRecycled()) {
                imageView.setImageBitmap(null);
                imageView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.gray_placeholder));
                bitmap.recycle();
            }
            bitmap = null;
        }
    }

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(delete.getVisibility() == VISIBLE) {
                delete.setVisibility(GONE);
            } else {
                delete.setVisibility(VISIBLE);
            }
        }
    };

    private OnClickListener onDeleteImageListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            listener.delete(KoalaImageView.this);
        }
    };

    ViewTreeObserver.OnScrollChangedListener onScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
        @Override
        public void onScrollChanged() {
            int[] location = new int[2];
            imageView.getLocationInWindow(location);
            if(location[1] < 0) {
                if(location[1] < -(height + bound) && visible) {
                    visible = false;
                    releaseImage();
                    return;
                }
                if(location[1] > -(height + bound) && !visible) {
                    visible = true;
                    reloadImage();
                }
            } else {
                if((location[1] > getResources().getDisplayMetrics().heightPixels + bound && visible)) {
                    visible = false;
                    releaseImage();
                    return;
                }
                if((location[1] < getResources().getDisplayMetrics().heightPixels + bound && !visible)) {
                    visible = true;
                    reloadImage();
                }
            }
        }
    };

    interface OnImageDeleteListener {
        void delete(KoalaBaseCellView v);
    }

    public static File getFrescoCache(String url){
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url)).build();
        CacheKey cacheKey= DefaultCacheKeyFactory.getInstance()
                .getEncodedCacheKey(imageRequest,false);
        BinaryResource bRes= ImagePipelineFactory.getInstance()
                .getMainFileCache()
                .getResource(cacheKey);
        if(bRes == null){
            return null;
        }
        return ((FileBinaryResource) bRes).getFile();
    }
}
