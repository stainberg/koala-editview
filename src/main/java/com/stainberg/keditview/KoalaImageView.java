package com.stainberg.keditview;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
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

    private SimpleDraweeView imageView;
    private ImageView delete;
    private ImageView drag;
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
        this(context, null, 0);
    }

    public KoalaImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KoalaImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private FileData fileData;

    @Deprecated
    public KoalaImageView(Context context, OnImageDeleteListener l, int w, int h) {
        super(context);
        width = w;
        height = h;
        listener = l;
        init();
    }

    public FileData getFileData() {
        return fileData;
    }

    public KoalaImageView(Context context, FileData fileData, OnImageDeleteListener l, int margin) {
        super(context);
        this.fileData = fileData;
        width = fileData.width;
        height = fileData.height;
        listener = l;
        init();
        src = TextUtils.isEmpty(fileData.fileUrl) ? fileData.filePath : fileData.fileUrl;
        reloadImage();
    }

    private void init() {
        setOnClickListener(onClickListener);
        View v = LayoutInflater.from(getContext()).inflate(R.layout.item_view_image, this, true);
        imageView = v.findViewById(R.id.icon);
        delete = v.findViewById(R.id.icon_delete);
        delete.setOnClickListener(onDeleteImageListener);

        drag = v.findViewById(R.id.icon_drag);

        visible = false;
        getViewTreeObserver().addOnScrollChangedListener(onScrollChangedListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        getViewTreeObserver().removeOnScrollChangedListener(onScrollChangedListener);
        releaseImage();
        super.onDetachedFromWindow();
    }

    public String getFilePath() {
        return src;
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
        if (location[1] < 0) {
            if (location[1] < -(height + bound) && visible) {
                visible = false;
                return;
            }
            if (location[1] > -(height + bound) && !visible) {
                visible = true;
                reloadImage();
            }
        } else {
            if ((location[1] > getResources().getDisplayMetrics().heightPixels + bound && visible)) {
                visible = false;
                return;
            }
            if ((location[1] < getResources().getDisplayMetrics().heightPixels + bound && !visible)) {
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

    @Override
    public void enableDrag(boolean enable) {
        if (enable) {
            drag.setVisibility(VISIBLE);
            drag.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    KoalaImageView.this.startDrag(ClipData.newPlainText("text", getUrl()), new KoalaDragShadowBuilder(KoalaImageView.this), new DragState(KoalaImageView.this), 0);
                    return true;
                }
            });
        } else {
            drag.setOnLongClickListener(null);
            drag.setVisibility(GONE);
        }
    }

    private void reloadImage() {
        System.out.println("reload bitmap");
        if (src.startsWith("http")) {
            imageView.setImageURI(src);
        } else {
            if (bitmap != null) {
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
        float width = fileData.width;
        float height = fileData.height;
        imageView.setAspectRatio(width / height);
    }

    private void releaseImage() {
        System.out.println("release bitmap");
        if (src.startsWith("http")) {
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
            if (delete.getVisibility() == VISIBLE) {
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
            if (location[1] < 0) {
                if (location[1] < -(height + bound) && visible) {
                    visible = false;
                    releaseImage();
                    return;
                }
                if (location[1] > -(height + bound) && !visible) {
                    visible = true;
                    reloadImage();
                }
            } else {
                if ((location[1] > getResources().getDisplayMetrics().heightPixels + bound && visible)) {
                    visible = false;
                    releaseImage();
                    return;
                }
                if ((location[1] < getResources().getDisplayMetrics().heightPixels + bound && !visible)) {
                    visible = true;
                    reloadImage();
                }
            }
        }
    };

    interface OnImageDeleteListener {
        void delete(KoalaBaseCellView v);
    }

    public static File getFrescoCache(String url) {
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url)).build();
        CacheKey cacheKey = DefaultCacheKeyFactory.getInstance()
                .getEncodedCacheKey(imageRequest, false);
        BinaryResource bRes = ImagePipelineFactory.getInstance()
                .getMainFileCache()
                .getResource(cacheKey);
        if (bRes == null) {
            return null;
        }
        return ((FileBinaryResource) bRes).getFile();
    }
}
