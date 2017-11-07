package com.stainberg.keditview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;
import java.util.List;

/**
 * Created by Stainberg on 7/5/17.
 */

public class KoalaImageView extends FrameLayout implements KoalaBaseCellView {
    private boolean dg;
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
    private int cardHeight = 0;
    private int margin;

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
        dg = false;
        this.fileData = fileData;
        this.margin = margin;
        width = fileData.width;
        height = fileData.height;
        listener = l;
        init();
        src = TextUtils.isEmpty(fileData.fileUrl) ? fileData.filePath : fileData.fileUrl;
        reloadImage();
    }

    private void init() {
        float x, y;
        x = getResources().getDisplayMetrics().widthPixels - 2 * margin;
        y = x / ((float) width / (float) height);
        setOnClickListener(onClickListener);
        View v = LayoutInflater.from(getContext()).inflate(R.layout.item_view_image, this, true);
        imageView = v.findViewById(R.id.icon);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams((int) x, (int) y);
        imageView.setLayoutParams(lp);
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
                releaseImage();
                return;
            }
            if (location[1] > -(height + bound) && !visible) {
                reloadImage();
            }
        } else {
            if ((location[1] > getResources().getDisplayMetrics().heightPixels + bound && visible)) {
                releaseImage();
                return;
            }
            if ((location[1] < getResources().getDisplayMetrics().heightPixels + bound && !visible)) {
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
    public List<String> getHtmlText() {
        return null;
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
                    startDrag();
                    return true;
                }
            });
        } else {
            drag.setOnLongClickListener(null);
            drag.setVisibility(GONE);
        }
    }

    @Override
    public void startDrag() {
        dg = true;
        if(cardHeight == 0) {
            cardHeight = this.getHeight();
        }
        ObjectAnimator animator = ObjectAnimator.ofInt(new AnimCard(this), "height", this.getHeight(), (int)(KoalaImageView.this.getResources().getDimension(R.dimen.card_file_height)));
        animator.setDuration(getDuration(this));
        animator.addListener(new Animator.AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) KoalaImageView.this.getLayoutParams();
                        lp.height = (int)(KoalaImageView.this.getResources().getDimension(R.dimen.card_file_height));
                        KoalaImageView.this.setLayoutParams(lp);
                        KoalaImageView.this.startDrag(ClipData.newPlainText("text", getUrl()), new KoalaDragShadowBuilder(KoalaImageView.this), new DragState(KoalaImageView.this), 0);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
        animator.start();
    }

    @Override
    public void endDrag() {
        if(dg) {
            System.out.print("123 end image Drag \n");
            dg = false;
            ObjectAnimator animator = ObjectAnimator.ofInt(new AnimCard(this), "height", (int) (KoalaImageView.this.getResources().getDimension(R.dimen.card_file_height)), cardHeight);
            animator.setDuration(getDuration(this));
            animator.start();
        }
    }

    private int getDuration(View view) {
        return 100;
    }

    private void reloadImage() {
        System.out.println("reload bitmap");
        float w = width;
        float h = height;
        float rate = w / h;
        if (imageView.getAspectRatio() != rate) {
            imageView.setAspectRatio(w / h);
        }
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
                    float scale = w / 800;
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
        visible = true;
    }

    public void releaseImage() {
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
        visible = false;
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
                    releaseImage();
                    return;
                }
                if (location[1] > -(height + bound) && !visible) {
                    reloadImage();
                }
            } else {
                if ((location[1] > getResources().getDisplayMetrics().heightPixels + bound && visible)) {
                    releaseImage();
                    return;
                }
                if ((location[1] < getResources().getDisplayMetrics().heightPixels + bound && !visible)) {
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
