package com.stainberg.keditview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.media.ExifInterface;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import static com.stainberg.keditview.UtilsKt.eventInView;

/**
 * Created by Stainberg on 7/5/17.
 */

public class KoalaImageView extends FrameLayout implements KoalaBaseCellView {
    private SimpleDraweeView imageView;
    private ImageView delete;
    private ImageView drag;
    private OnImageDeleteListener listener;
    private String src;
    private boolean visible;
    private int width, height;
    private Bitmap bitmap;
    private int bound = getResources().getDisplayMetrics().heightPixels;
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
        this.fileData = fileData;
        this.margin = margin;
        listener = l;
        src = TextUtils.isEmpty(fileData.fileUrl) ? fileData.filePath : fileData.fileUrl;
        init();
        reloadImage();
    }

    private boolean isDragEnabled = false;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (null != drag && drag.getVisibility() == View.VISIBLE) {
            isDragEnabled = eventInView(ev, drag);
            if (isDragEnabled) {
                return isDragEnabled;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isDragEnabled) {
            return false;
        }
        return super.onTouchEvent(event);
    }

    private void init() {
        final View v = LayoutInflater.from(getContext()).inflate(R.layout.item_view_image, this, true);
        delete = v.findViewById(R.id.icon_delete);
        delete.setOnClickListener(onDeleteImageListener);
        drag = v.findViewById(R.id.icon_drag);
        visible = false;
        setOnClickListener(onClickListener);
        getViewTreeObserver().addOnScrollChangedListener(onScrollChangedListener);
        width = fileData.width;
        height = fileData.height;
        float x, y;
        x = getResources().getDisplayMetrics().widthPixels - 2 * margin;
        y = x / ((float) width / (float) height);
        imageView = v.findViewById(R.id.icon);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams((int) x, (int) y);
        imageView.setLayoutParams(lp);
    }

    public String getFilePath() {
        return src;
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
    public void setEditable(boolean enable) {

    }

    @Override
    public void enableDrag(boolean enable) {
        if (enable) {
            drag.setVisibility(VISIBLE);
        } else {
            drag.setVisibility(GONE);
        }
    }

    @Override
    public void release() {
        getViewTreeObserver().removeOnScrollChangedListener(onScrollChangedListener);
        releaseImage();
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
                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(src, options);
                        int w = options.outWidth;
                        float scale = w / 800;
                        int s = (int) Math.ceil(scale);
                        options.inJustDecodeBounds = false;
                        options.inSampleSize = s;
                        bitmap = BitmapFactory.decodeFile(src, options);
                        int digree;
                        ExifInterface exif = new ExifInterface(src);
                        int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_UNDEFINED);
                        switch (ori) {
                            case ExifInterface.ORIENTATION_ROTATE_90:
                                digree = 90;
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_180:
                                digree = 180;
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_270:
                                digree = 270;
                                break;
                            default:
                                digree = 0;
                                break;
                        }
                        if (digree != 0) {
                            Matrix m = new Matrix();
                            m.postRotate(digree);
                            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                            imageView.setImageBitmap(bitmap);
                        }
                        post(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    } catch (Exception e) {

                    }

                }
            });
        }
        visible = true;
    }

    public void releaseImage() {
        visible = false;
        imageView.setImageBitmap(null);
        imageView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.gray_placeholder));
        if (bitmap != null) {
            bitmap.recycle();
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

}
