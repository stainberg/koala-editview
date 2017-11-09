package com.stainberg.keditview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stainberg on 7/5/17.
 */

public class KoalaRichEditorView extends FrameLayout {

    public LinearLayout container;
    private Context context;
    private int margin = 0;
    private OnStatusListener keyStatusListener;
    private OnEditTextChangedListener onEditTextChangedListener;

    public KoalaRichEditorView(Context context) {
        this(context, null);
    }

    public KoalaRichEditorView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KoalaRichEditorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context c) {
        context = c;
        View root = View.inflate(c, R.layout.layout_koala_rich_editor, this);
        NestedScrollView scrollView = root.findViewById(R.id.koala_rich_editor_srollview);
        container = root.findViewById(R.id.koala_rich_editor_container);
        scrollView.setSmoothScrollingEnabled(true);
        KoalaEditTextView editTextView = new KoalaEditTextView(context, onPressEnterListener, statusListener, onHintSetListener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(editTextView, lp);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                KoalaBaseCellView edit = (KoalaBaseCellView) container.getChildAt(container.getChildCount() - 1);
                if (edit instanceof KoalaEditTextView) {
                    ((KoalaEditTextView) edit).getEditView().requestFocus();
                    ((KoalaEditTextView) edit).getEditView().setSelection(((KoalaEditTextView) edit).getEditView().length());
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(((KoalaEditTextView) edit).getEditView(), InputMethodManager.SHOW_FORCED);
                }
            }
        });
        setHint();
    }

    public void swapViewGroupChildren(ViewGroup viewGroup, View firstView, View secondView) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.card_file_height));
        int firstIndex = viewGroup.indexOfChild(firstView);
        int secondIndex = viewGroup.indexOfChild(secondView);
        if (firstIndex < secondIndex) {
            viewGroup.removeViewAt(secondIndex);
            viewGroup.removeViewAt(firstIndex);
            viewGroup.addView(secondView, firstIndex, lp1);
            secondView.setVisibility(VISIBLE);
            viewGroup.addView(firstView, secondIndex, lp);
            firstView.setVisibility(VISIBLE);
        } else {
            viewGroup.removeViewAt(firstIndex);
            viewGroup.removeViewAt(secondIndex);
            viewGroup.addView(firstView, secondIndex, lp);
            firstView.setVisibility(VISIBLE);
            viewGroup.addView(secondView, firstIndex, lp1);
            secondView.setVisibility(VISIBLE);
        }
    }

    private void swapViews(ViewGroup viewGroup, final View view, int index,
                           DragState dragState) {
        swapViewGroupChildren(viewGroup, view, dragState.view);
        final float viewY = view.getY();
        dragState.index = index;
        postOnPreDraw(view, new Runnable() {
            @Override
            public void run() {
                ObjectAnimator
                        .ofFloat(view, View.Y, viewY, view.getTop())
                        .setDuration(getDuration(view))
                        .start();
            }
        });
    }

    private int getDuration(View view) {
        return 100;
    }

    public void postOnPreDraw(View view, final Runnable runnable) {
        final ViewTreeObserver observer = view.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (observer.isAlive()) {
                    observer.removeOnPreDrawListener(this);
                }
                runnable.run();
                return true;
            }
        });
    }

    public void setOnEditTextChangedListener(OnEditTextChangedListener listener) {
        onEditTextChangedListener = listener;
    }

    public void setKeyStatusListener(OnStatusListener listener) {
        keyStatusListener = listener;
    }

    public void setMargin(int m) {
        margin = m;
    }

    public void resetEditor() {
        container.removeAllViews();
    }

    public String getDesc() {
        if (container.getChildCount() <= 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < container.getChildCount(); i++) {
            if(container.getChildAt(i) instanceof KoalaBaseCellView) {
                KoalaBaseCellView data = (KoalaBaseCellView) container.getChildAt(i);
                if (data.getType() == KoalaBaseCellView.EDIT_VIEW) {
                    builder.append(data.getText());
                } else if (data.getType() == KoalaBaseCellView.IMAGE_VIEW) {
                    builder.append(getResources().getString(R.string.draft_pic));
                } else if (data.getType() == KoalaBaseCellView.CARD_VIEW) {
                    builder.append(getResources().getString(R.string.draft_card));
                } else if (data instanceof KoalaFileView) {
                    builder.append(getResources().getString(R.string.draft_file));
                }

                if (builder.length() > 50) {
                    break;
                }
            }
        }
        return builder.toString();
    }

    public int getItemCount() {
        return container.getChildCount();
    }

    public KoalaBaseCellView getItem(int index) {
        if(container.getChildAt(index) instanceof KoalaBaseCellView) {
            return (KoalaBaseCellView) container.getChildAt(index);
        }
        return null;
    }

    public List<KoalaBaseCellView> getAllViews() {
        List<KoalaBaseCellView> list = new ArrayList<>();
        for(int i = 0; i < container.getChildCount(); i++) {
            if(container.getChildAt(i) instanceof KoalaBaseCellView) {
                list.add((KoalaBaseCellView) container.getChildAt(i));
            }
        }
        return list;
    }

    public void setStyleH1() {
        KoalaBaseCellView v = (KoalaBaseCellView) container.getFocusedChild();
        if (v != null) {
            v.setStyleH1();
        }
    }

    public void setStyleH2() {
        KoalaBaseCellView v = (KoalaBaseCellView) container.getFocusedChild();
        if (v != null) {
            v.setStyleH2();
        }
    }

    public void setGravity() {
        KoalaBaseCellView v = (KoalaBaseCellView) container.getFocusedChild();
        if (v != null) {
            v.setGravity();
        }
    }

    public void setQuote() {
        KoalaBaseCellView v = (KoalaBaseCellView) container.getFocusedChild();
        if (v != null) {
            v.setQuote();
        }
    }

    public void setSection(int type) {
        KoalaBaseCellView v = (KoalaBaseCellView) container.getFocusedChild();
        if (v != null) {
            v.setSection(type);
        }
    }

    public void setSection() {
        KoalaBaseCellView v = (KoalaBaseCellView) container.getFocusedChild();
        if (v != null) {
            switch (v.getSection()) {
                case 1:
                    v.setSection(2);
                    break;
                case 2:
                    v.setSection(0);
                    break;
                default:
                    v.setSection(1);
                    break;
            }
        }
    }

    public void setBold() {
        KoalaBaseCellView v = (KoalaBaseCellView) container.getFocusedChild();
        if (v != null) {
            v.setBold();
        }
    }

    public void setItalic() {
        KoalaBaseCellView v = (KoalaBaseCellView) container.getFocusedChild();
        if (v != null) {
            v.setItalic();
        }
    }

    public void setStrike() {
        KoalaBaseCellView v = (KoalaBaseCellView) container.getFocusedChild();
        if (v != null) {
            v.setStrike();
        }
    }

    public void addKoalaView(KoalaBaseCellView koalaBaseCellView, int h) {
        if (!(koalaBaseCellView instanceof View)) {
            throw new IllegalArgumentException("view is not Extends View Class");
        }
        int index = container.getChildCount();
        View v = container.getFocusedChild();
        if (v != null) {
            index = container.indexOfChild(v);
        }
        LinearLayout.LayoutParams lpslider = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h);
        container.addView((View) koalaBaseCellView, index + 1, lpslider);
        KoalaEditTextView editTextView = new KoalaEditTextView(context, onPressEnterListener, statusListener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(editTextView, index + 2, lp);
        editTextView.requestFocus();
        if (v instanceof KoalaEditTextView) {
            KoalaEditTextView view = (KoalaEditTextView) v;
            CharSequence p, n;
            int start = view.getSelectionStart();
            if (start < view.getText().length()) {
                p = view.getText().subSequence(0, start);
                n = view.getText().subSequence(start, view.getText().length());
            } else {
                p = view.getText();
                n = "";
            }
            view.setText(p);
            editTextView.setText(n);
            editTextView.setSelection(0);
        }
    }

    public View getCurrentFocusEdit() {
        View v = container.getFocusedChild();
        if (v != null && v instanceof KoalaEditTextView) {
            return ((KoalaEditTextView) v).getEditView();
        }
        return null;
    }

    public void enableDrag(boolean enable) {
        for(int i = 0; i < container.getChildCount(); i++) {
            if(container.getChildAt(i) instanceof KoalaBaseCellView) {
                KoalaBaseCellView v = (KoalaBaseCellView) container.getChildAt(i);
                v.enableDrag(enable);
            }
        }
    }

    public void addCellText(String sequence) {
        addCellText(sequence, false, false);
    }

    public void addCellText(String sequence, boolean center, boolean isAddLast) {
        int index = getNextIndex(isAddLast);
        KoalaEditTextView editTextView = new KoalaEditTextView(context, onPressEnterListener, statusListener);
        if (center) {
            editTextView.setGravity();
        }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(editTextView, index, lp);
        editTextView.setHtmlText(sequence);
    }

    private void addCellTextLast(String sequence) {
        int index = container.getChildCount();
        KoalaEditTextView editTextView = new KoalaEditTextView(context, onPressEnterListener, statusListener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(editTextView, index, lp);
        editTextView.setHtmlText(sequence);
        editTextView.requestFocus();
    }

    public void addCellQuote(String sequence, boolean isAddLast) {
        int index = getNextIndex(isAddLast);
        KoalaEditTextView editTextView = new KoalaEditTextView(context, onPressEnterListener, statusListener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(editTextView, index, lp);
        editTextView.setHtmlText(sequence);
        editTextView.setQuote();
        System.out.println(sequence);
        System.out.println(editTextView.getText().toString());
    }

    public void addCellH1(String sequence) {
        addCellH1(sequence, false, false);
    }

    public void addCellH1(String sequence, boolean center, boolean isAddLast) {
        int index = getNextIndex(isAddLast);
        KoalaEditTextView editTextView = new KoalaEditTextView(context, onPressEnterListener, statusListener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (center) {
            editTextView.setGravity();
        }
        container.addView(editTextView, index, lp);
        editTextView.setHtmlText(sequence);
        editTextView.setStyleH1();
    }

    public void addCellH2(String sequence) {
        addCellH2(sequence, false, false);
    }

    public void addCellH2(String sequence, boolean center, boolean isAddLast) {
        int index = getNextIndex(isAddLast);
        KoalaEditTextView editTextView = new KoalaEditTextView(context, onPressEnterListener, statusListener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (center) {
            editTextView.setGravity();
        }
        container.addView(editTextView, index, lp);
        editTextView.setHtmlText(sequence);
        editTextView.setStyleH2();
    }

    public void addCellList1(String sequence, boolean isAddLast) {
        int index = getNextIndex(isAddLast);
        KoalaEditTextView editTextView = new KoalaEditTextView(context, onPressEnterListener, statusListener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(editTextView, index, lp);
        editTextView.setHtmlText(sequence);
        editTextView.setSection(KoalaEditTextView.SECTION_NUMBER);
        System.out.println(sequence);
        System.out.println(editTextView.getText().toString());
    }

    public void addCellList2(String sequence, boolean isAddLast) {
        int index = getNextIndex(isAddLast);
        KoalaEditTextView editTextView = new KoalaEditTextView(context, onPressEnterListener, statusListener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(editTextView, index, lp);
        editTextView.setHtmlText(sequence);
        editTextView.setSection(KoalaEditTextView.SECTION_DOT);
        System.out.println(sequence);
    }

    private int getNextIndex(boolean isAddLast) {
        int index = container.getChildCount();
        if (!isAddLast) {
            View v = container.getFocusedChild();
            if (v != null) {
                index = container.indexOfChild(v) + 1;
            }
        }
        return index;
    }

    public void addFile(FileData data, boolean addEmptyAfter, boolean addLast) {
        if (null == data) {
            return;
        }
        data.type = 1;
        int index = getNextIndex(addLast);
        KoalaFileView cardView = new KoalaFileView(context, data);
        LinearLayout.LayoutParams lpCard = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(cardView, index, lpCard);
        if (addEmptyAfter && index == container.getChildCount() - 1) {
            addCellTextLast("");
        }
    }

    public void addImage(FileData fileData, boolean addEmptyAfter, boolean addLast) {
        if (null == fileData) {
            return;
        }
        fileData.type = 0;
        int index = getNextIndex(addLast);
        KoalaImageView imageView;
        if (TextUtils.isEmpty(fileData.filePath)) {
            //网络图片
            imageView = new KoalaImageView(context, fileData, onImageDeleteListener, margin);
        } else {
            int h;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(fileData.filePath, options);
            fileData.width = options.outWidth;
            fileData.height = options.outHeight;
            //本地图片
            boolean c;
            try {
                ExifInterface exif = new ExifInterface(fileData.filePath);
                int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);
                switch (ori) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        c = true;
                        break;
                    default:
                        c = false;
                        break;
                }
                if(c) {
                    int w = fileData.width;
                    fileData.width = fileData.height;
                    fileData.height = w;
                }
            } catch (Exception e) {

            }
            imageView = new KoalaImageView(context, fileData, onImageDeleteListener, margin);
        }
        ViewGroup.LayoutParams lpimage = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(imageView, index, lpimage);
        if (addEmptyAfter && index == container.getChildCount() - 1) {
            addCellTextLast("");
        }
    }

    public void addSlider() {
        int index = container.getChildCount();
        View v = container.getFocusedChild();
        if (v != null) {
            index = container.indexOfChild(v);
        }
        KoalaSliderView sliderView = new KoalaSliderView(context);
        LinearLayout.LayoutParams lpslider = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(sliderView, index + 1, lpslider);
        KoalaEditTextView editTextView = new KoalaEditTextView(context, onPressEnterListener, statusListener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(editTextView, index + 2, lp);
        editTextView.requestFocus();
        if (v instanceof KoalaEditTextView) {
            KoalaEditTextView view = (KoalaEditTextView) v;
            CharSequence p, n;
            int start = view.getSelectionStart();
            if (start < view.getText().length()) {
                p = view.getText().subSequence(0, start);
                n = view.getText().subSequence(start, view.getText().length());
            } else {
                p = view.getText();
                n = "";
            }
            view.setText(p);
            editTextView.setText(n);
            editTextView.setSelection(0);
            editTextView.resetNextSection(editTextView);
        }
    }

    public void addCode() {
        int index = container.getChildCount();
        View v = container.getFocusedChild();
        if (v != null) {
            index = container.indexOfChild(v);
        }
        KoalaEditTextView codeView = new KoalaEditTextView(context, onPressEnterListener, statusListener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(codeView, index + 1, lp);
        codeView.requestFocus();
        codeView.addCode();
    }

    @Override
    protected void onDetachedFromWindow() {
        for(int i = 0; i < container.getChildCount(); i++) {
            if(container.getChildAt(i) instanceof KoalaBaseCellView) {
                KoalaBaseCellView v = (KoalaBaseCellView) container.getChildAt(i);
                v.release();
            }
        }
        super.onDetachedFromWindow();
    }

    private KoalaEditTextView.OnEditListener onPressEnterListener = new KoalaEditTextView.OnEditListener() {
        @Override
        public void insertEdit(KoalaBaseCellView v) {
            if (v instanceof KoalaEditTextView) {
                KoalaEditTextView view = (KoalaEditTextView) v;
                int index = container.indexOfChild(view);
                KoalaEditTextView editTextView = new KoalaEditTextView(context, onPressEnterListener, statusListener);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                container.addView(editTextView, index, lp);
                editTextView.requestFocus();
                editTextView.setSelection(0);
            }
            setHint();
        }

        @Override
        public void pressEnter(KoalaBaseCellView v) {
            if (v instanceof KoalaEditTextView) {
                KoalaEditTextView view = (KoalaEditTextView) v;
                CharSequence p, n;
                int index = container.indexOfChild(view);
                int start = view.getSelectionStart();
                if (start < view.getText().length()) {
                    p = view.getText().subSequence(0, start);
                    n = view.getText().subSequence(start, view.getText().length());
                } else {
                    p = view.getText();
                    n = "";
                }
                final KoalaEditTextView editTextView = new KoalaEditTextView(context, onPressEnterListener, statusListener);
                view.setText(p);
                editTextView.setText(n);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                container.addView(editTextView, index + 1, lp);
                editTextView.requestFocus();
                editTextView.setSelection(0);
                if (view.section == 1) {
                    editTextView.setNumberSection(editTextView);
                } else if (view.section == 2) {
                    editTextView.setDotSection(editTextView);
                }
                editTextView.setNextSection(editTextView);
            }
            setHint();
        }

        @Override
        public void deleteSelf(KoalaBaseCellView v) {
            if (container.getChildCount() > 1) {
                int index = container.indexOfChild((View) v);
                CharSequence lastStr = v.getText();
                if (index > 0) {
                    View view = container.getChildAt(index - 1);
                    if (view instanceof KoalaEditTextView) {
                        container.removeView((View) v);
                        ((KoalaEditTextView) view).append(lastStr, ((KoalaEditTextView) view).length());
                        view.requestFocus();
                        ((KoalaEditTextView) view).setNextSection(((KoalaEditTextView) view));
                    } else if (view instanceof KoalaImageView) {
                        container.removeView((View) v);
                        container.removeView(view);
                        if (index > 1) {
                            View pprev = container.getChildAt(index - 2);
                            if (pprev instanceof KoalaEditTextView) {
                                ((KoalaEditTextView) pprev).append(lastStr, ((KoalaEditTextView) pprev).length());
                                pprev.requestFocus();
                                ((KoalaEditTextView) pprev).setNextSection((KoalaEditTextView) pprev);
                            }
                        }

                    } else if (view instanceof KoalaSliderView) {
                        container.removeView((View) v);
                        container.removeView(view);
                        if (index > 1) {
                            View pprev = container.getChildAt(index - 2);
                            if (pprev instanceof KoalaEditTextView) {
                                ((KoalaEditTextView) pprev).append(lastStr, ((KoalaEditTextView) pprev).length());
                                pprev.requestFocus();
                                ((KoalaEditTextView) pprev).setNextSection((KoalaEditTextView) pprev);
                            }
                        }
                    } else if (view instanceof KoalaBaseCellView) {
                        container.removeView((View) v);
                        container.removeView(view);
                        if (index > 1) {
                            View pprev = container.getChildAt(index - 2);
                            if (pprev instanceof KoalaEditTextView) {
                                ((KoalaEditTextView) pprev).append(lastStr, ((KoalaEditTextView) pprev).length());
                                pprev.requestFocus();
                                ((KoalaEditTextView) pprev).setNextSection((KoalaEditTextView) pprev);
                            }
                        }
                    }
                }
            }
            setHint();
        }

        @Override
        public void splitSelf(KoalaBaseCellView v, CharSequence p, CharSequence s, CharSequence n, int section, int style) {
            int index = container.indexOfChild((View) v);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            String[] ss = s.toString().split("\n");
            if (!TextUtils.isEmpty(p)) {
                v.setText(p);
                v.setQuote();
                for (int i = 1; i < ss.length + 1; i++) {
                    KoalaEditTextView c = new KoalaEditTextView(context, onPressEnterListener, statusListener);
                    container.addView(c, index + i, lp);
                    c.setText(ss[i - 1]);
                    c.setSection(section);
                    if (i == 1) {
                        c.requestFocus();
                        c.setSelection(0);
                    }
                    c.setTextStyle(style);
                }
                index = index + ss.length + 1;
            } else {
                for (int i = 0; i < ss.length; i++) {
                    if (i == 0) {
                        v.setText(ss[i]);
                        ((KoalaEditTextView) v).requestFocus();
                        ((KoalaEditTextView) v).setSelection(0);
                        v.setSection(section);
                        ((KoalaEditTextView) v).setTextStyle(style);
                    } else {
                        KoalaEditTextView c = new KoalaEditTextView(context, onPressEnterListener, statusListener);
                        container.addView(c, index + i, lp);
                        c.setText(ss[i]);
                        c.setSection(section);
                        c.setTextStyle(style);
                    }
                }
                index = index + ss.length;
            }
            if (n != null) {
                KoalaEditTextView ne = new KoalaEditTextView(context, onPressEnterListener, statusListener);
                container.addView(ne, index);
                ne.setText(n);
                ne.setQuote();
            }
            setHint();
        }
    };

    private KoalaImageView.OnImageDeleteListener onImageDeleteListener = new KoalaImageView.OnImageDeleteListener() {
        @Override
        public void delete(KoalaBaseCellView v) {
            container.removeView((View) v);
            for(int i = 0; i < container.getChildCount(); i++) {
                if (container.getChildAt(i) instanceof KoalaImageView) {
                    KoalaImageView kcv = (KoalaImageView) container.getChildAt(i);
                    kcv.reload();
                }
            }
        }
    };

    private KoalaEditTextView.OnHintSetListener onHintSetListener = new KoalaEditTextView.OnHintSetListener() {

        @Override
        public void onHintChanged() {
            setHint();
        }
    };

    public void setHint() {
        if (container.getChildAt(0) instanceof KoalaEditTextView) {
            KoalaEditTextView v = (KoalaEditTextView) container.getChildAt(0);
            if (container.getChildCount() == 1 && v.getEditView().length() == 0) {
                v.getEditView().setHint("输入内容");
            } else {
                v.getEditView().setHint("");
            }
        }
    }

    public void clearHint() {
        if (container.getChildAt(0) instanceof KoalaEditTextView) {
            KoalaEditTextView v = (KoalaEditTextView) container.getChildAt(0);
            v.getEditView().setHint("");
        }
    }

    private KoalaEditTextView.OnEditTextStatusListener statusListener = new KoalaEditTextView.OnEditTextStatusListener() {

        @Override
        public void setEnableKeyBoard(boolean enable) {
            if (keyStatusListener == null) {
                return;
            }
            if (enable) {
                keyStatusListener.setEnableKeyBoard(true);
            } else {
                keyStatusListener.setEnableKeyBoard(false);
            }
        }

        @Override
        public void setEnableFocus(boolean enable) {
            for(int i = 0; i < container.getChildCount(); i++) {
                if (container.getChildAt(i) instanceof KoalaEditTextView) {
                    KoalaEditTextView v = (KoalaEditTextView) container.getChildAt(i);
                    v.setEditable(false);
                }
            }
        }

        @Override
        public void onEditStatus(int status) {
            if (onEditTextChangedListener != null) {
                onEditTextChangedListener.onEditTextTextChanged(status);
            }
        }
    };

    static KoalaBaseCellView getPrev(ViewGroup parent, View v) {
        int index = parent.indexOfChild(v);
        if(index > 0) {
            View prev = parent.getChildAt(index - 1);
            if(prev instanceof KoalaBaseCellView) {
                return (KoalaBaseCellView) prev;
            }
        }
        return null;
    }

    static KoalaBaseCellView getNext(ViewGroup parent, View v) {
        int index = parent.indexOfChild(v);
        if(index < parent.getChildCount() - 1) {
            View next = parent.getChildAt(index + 1);
            if(next instanceof KoalaBaseCellView) {
                return (KoalaBaseCellView) next;
            }
        }
        return null;
    }

    public interface OnStatusListener {
        void setEnableKeyBoard(boolean enableKeyBoard);
    }

    public interface OnEditTextChangedListener {
        void onEditTextTextChanged(int status);
    }
}
