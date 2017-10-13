package com.stainberg.keditview;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.List;

/**
 * Created by Stainberg on 7/5/17.
 */

public class KoalaRichEditorView extends FrameLayout {

    private LinearLayout container;
    private Context context;
    private List<KoalaBaseCellView> views;
    private int margin = 0;
    private OnStatusListener keyStatusListener;

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
        views = new EditList<>();
        View root = View.inflate(c, R.layout.layout_koala_rich_editor, this);
        KoalaScrollView scrollView = root.findViewById(R.id.koala_rich_editor_srollview);
        container = root.findViewById(R.id.koala_rich_editor_container);
        scrollView.setSmoothScrollingEnabled(true);
        KoalaEditTextView editTextView = new KoalaEditTextView(context, onPressEnterListener, statusListener, onHintSetListener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(editTextView, lp);
        setDragAndDrop(editTextView);
        views.add(editTextView);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                KoalaBaseCellView edit = (KoalaBaseCellView) container.getChildAt(container.getChildCount() - 1);
                if(edit instanceof KoalaEditTextView) {
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
        int firstIndex = viewGroup.indexOfChild(firstView);
        int secondIndex = viewGroup.indexOfChild(secondView);
        if (firstIndex < secondIndex) {
            viewGroup.removeViewAt(secondIndex);
            views.remove(secondView);
            viewGroup.removeViewAt(firstIndex);
            views.remove(firstView);
            viewGroup.addView(secondView, firstIndex, lp);
            secondView.setVisibility(VISIBLE);
            views.add(firstIndex, (KoalaBaseCellView) secondView);
            viewGroup.addView(firstView, secondIndex, lp);
            firstView.setVisibility(VISIBLE);
            views.add(secondIndex, (KoalaBaseCellView) firstView);
        } else {
            viewGroup.removeViewAt(firstIndex);
            views.remove(firstView);
            viewGroup.removeViewAt(secondIndex);
            views.remove(secondView);
            firstView.setVisibility(VISIBLE);
            viewGroup.addView(firstView, secondIndex, lp);
            views.add(secondIndex, (KoalaBaseCellView) firstView);
            secondView.setVisibility(VISIBLE);
            viewGroup.addView(secondView, firstIndex, lp);
            views.add(firstIndex, (KoalaBaseCellView) secondView);
        }
        for(KoalaBaseCellView v : views) {
            Log.v("333", v.getText().toString());
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
        return view.getResources().getInteger(android.R.integer.config_shortAnimTime);
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

    public void setKeyStatusListener(OnStatusListener listener) {
        keyStatusListener = listener;
    }

    public void setMargin(int m) {
        margin = m;
    }

    public void resetEditor() {
        container.removeAllViews();
        views.clear();
    }

    public String getDesc() {
        if (views.size() <= 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (KoalaBaseCellView data : views) {
            if (data.getType() == KoalaBaseCellView.EDIT_VIEW) {
                builder.append(data.getText());
            }
            if (data.getType() == KoalaBaseCellView.IMAGE_VIEW) {
                builder.append(getResources().getString(R.string.draft_pic));
            }
            if (data.getType() == KoalaBaseCellView.CARD_VIEW) {
                builder.append(getResources().getString(R.string.draft_card));
            }
            if(builder.length() > 50) {
                break;
            }
        }
        return builder.toString();
    }

    public int getItemCount() {
        return views.size();
    }

    public KoalaBaseCellView getItem(int index) {
        return views.get(index);
    }

    public List<KoalaBaseCellView> getAllViews() {
        return views;
    }

    public void setStyleH1() {
        KoalaBaseCellView v = (KoalaBaseCellView) container.getFocusedChild();
        if(v != null) {
            v.setStyleH1();
        }
    }

    public void setStyleH2() {
        KoalaBaseCellView v = (KoalaBaseCellView) container.getFocusedChild();
        if(v != null) {
            v.setStyleH2();
        }
    }

    public void setGravity() {
        KoalaBaseCellView v = (KoalaBaseCellView) container.getFocusedChild();
        if(v != null) {
            v.setGravity();
        }
    }

    public void setQuote() {
        KoalaBaseCellView v = (KoalaBaseCellView) container.getFocusedChild();
        if(v != null) {
            v.setQuote();
        }
    }

    public void setSection(int type) {
        KoalaBaseCellView v = (KoalaBaseCellView) container.getFocusedChild();
        if(v != null) {
            v.setSection(type);
        }
    }

    public void setBold() {
        KoalaBaseCellView v = (KoalaBaseCellView) container.getFocusedChild();
        if(v != null) {
            v.setBold();
        }
    }

    public void setItalic() {
        KoalaBaseCellView v = (KoalaBaseCellView) container.getFocusedChild();
        if(v != null) {
            v.setItalic();
        }
    }

    public void setStrike() {
        KoalaBaseCellView v = (KoalaBaseCellView) container.getFocusedChild();
        if(v != null) {
            v.setStrike();
        }
    }

    public void addKoalaView(KoalaBaseCellView koalaBaseCellView, int h) {
        if(!(koalaBaseCellView instanceof View)) {
            throw new IllegalArgumentException("view is not Extends View Class");
        }
        int index = container.getChildCount();
        View v = container.getFocusedChild();
        if(v != null) {
            index = container.indexOfChild(v);
        }
        LinearLayout.LayoutParams lpslider = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h);
        container.addView((View) koalaBaseCellView, index + 1, lpslider);
        views.add(index + 1, koalaBaseCellView);
        KoalaEditTextView editTextView = new KoalaEditTextView(context, onPressEnterListener, statusListener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(editTextView, index + 2, lp);
        views.add(index + 2, editTextView);
        editTextView.requestFocus();
        if(v instanceof KoalaEditTextView) {
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

    private void setDragAndDrop(KoalaBaseCellView view) {
        ((View)view).setOnDragListener(new OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent event) {
                ViewGroup viewGroup = (ViewGroup)view.getParent();
                DragState dragState = (DragState)event.getLocalState();
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        if (view == dragState.view) {
                            view.setVisibility(View.INVISIBLE);
                        }
                        break;
                    case DragEvent.ACTION_DROP:
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        for(KoalaBaseCellView v : views) {
                            v.setEditable(true);
                        }
                        if (view == dragState.view) {
                            view.setVisibility(View.VISIBLE);
                        }
                        break;
                    case DragEvent.ACTION_DRAG_LOCATION: {
                        if (view == dragState.view){
                            break;
                        }
                        int index = viewGroup.indexOfChild(view);
                        if ((index > dragState.index && event.getY() > view.getHeight() / 2)
                                || (index < dragState.index && event.getY() < view.getHeight() / 2)) {
                            swapViews(viewGroup, view, index, dragState);
                        }
                        break;
                    }
                }
                return true;
            }
        });
    }

    public View getCurrentFocusEdit() {
        View v = container.getFocusedChild();
        if(v != null && v instanceof KoalaEditTextView) {
            return ((KoalaEditTextView) v).getEditView();
        }
        return null;
    }

    public void addCard(KoalaCardView cardView) {
        int index = container.getChildCount() - 1;
        View v = container.getFocusedChild();
        if(v != null) {
            index = container.indexOfChild(v);
        }
        LinearLayout.LayoutParams lpCard = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(cardView, index + 1, lpCard);
        views.add(index + 1, cardView);
        KoalaEditTextView editTextView = new KoalaEditTextView(context, onPressEnterListener, statusListener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(editTextView, index + 2, lp);
        views.add(index + 2, editTextView);
        editTextView.requestFocus();
        if(v instanceof KoalaEditTextView) {
            if(!((KoalaEditTextView) v).isCode()) {
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
            }
            editTextView.setSelection(0);
            editTextView.resetNextSection(editTextView);
        }
        setHint();
    }

    public void addImage(String path) {
        int index = container.getChildCount() - 1;
        View v = container.getFocusedChild();
        if(v != null) {
            index = container.indexOfChild(v);
        }
        int w, h;
        w = getResources().getDisplayMetrics().widthPixels - margin * 2;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        float ow = options.outWidth;
        float oh = options.outHeight;
        float scale = ow / oh;
        h = (int) (w / scale);
        KoalaImageView imageView = new KoalaImageView(context, onImageDeleteListener, w, h);
        ViewGroup.LayoutParams lpimage = new ViewGroup.LayoutParams(w, h);
        container.addView(imageView, index + 1, lpimage);
        views.add(index + 1, imageView);
        imageView.setImageSource(path);
        KoalaEditTextView editTextView = new KoalaEditTextView(context, onPressEnterListener, statusListener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(editTextView, index + 2, lp);
        views.add(index + 2, editTextView);
        editTextView.requestFocus();
        if(v instanceof KoalaEditTextView) {
            if(!((KoalaEditTextView) v).isCode()) {
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
                if(((KoalaEditTextView) v).isQuote()) {
                    editTextView.setQuote();
                }
            }
            editTextView.setSelection(0);
            editTextView.resetNextSection(editTextView);
        }
        setHint();
    }

    public void enableDrag(boolean enable) {
        for(KoalaBaseCellView v : views) {
            v.enableDrag(enable);
        }
    }

    public void addCellText(String sequence) {
        int index = container.getChildCount();
        KoalaEditTextView editTextView = new KoalaEditTextView(context, onPressEnterListener, statusListener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(editTextView, index, lp);
        views.add(index, editTextView);
        editTextView.setHtmlText(sequence);
    }

    public void addCellQuote(String sequence) {
        int index = container.getChildCount();
        KoalaEditTextView editTextView = new KoalaEditTextView(context, onPressEnterListener, statusListener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(editTextView, index, lp);
        views.add(index, editTextView);
        editTextView.setHtmlText(sequence);
        editTextView.setQuote();
        System.out.println(sequence);
        System.out.println(editTextView.getText().toString());
    }

    public void addCellH1(String sequence) {
        int index = container.getChildCount();
        KoalaEditTextView editTextView = new KoalaEditTextView(context, onPressEnterListener, statusListener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(editTextView, index, lp);
        views.add(index, editTextView);
        editTextView.setHtmlText(sequence);
        editTextView.setStyleH1();
    }

    public void addCellH2(String sequence) {
        int index = container.getChildCount();
        KoalaEditTextView editTextView = new KoalaEditTextView(context, onPressEnterListener, statusListener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(editTextView, index, lp);
        views.add(index, editTextView);
        editTextView.setHtmlText(sequence);
        editTextView.setStyleH2();
    }

    public void addCellList1(String sequence) {
        int index = container.getChildCount();
        KoalaEditTextView editTextView = new KoalaEditTextView(context, onPressEnterListener, statusListener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(editTextView, index, lp);
        views.add(index, editTextView);
        editTextView.setHtmlText(sequence);
        editTextView.setSection(KoalaEditTextView.SECTION_NUMBER);
        System.out.println(sequence);
        System.out.println(editTextView.getText().toString());
    }

    public void addCellList2(String sequence) {
        int index = container.getChildCount();
        KoalaEditTextView editTextView = new KoalaEditTextView(context, onPressEnterListener, statusListener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(editTextView, index, lp);
        views.add(index, editTextView);
        editTextView.setHtmlText(sequence);
        editTextView.setSection(KoalaEditTextView.SECTION_DOT);
        System.out.println(sequence);
    }

    public void addCellImage(String url, int width, int height) {
        int index = container.getChildCount();
        int w, h;
        w = getResources().getDisplayMetrics().widthPixels - margin * 2;
        float scale = ((float) width / (float)height);
        h = (int) (w / scale);
        KoalaImageView imageView = new KoalaImageView(context, onImageDeleteListener, w, h);
        ViewGroup.LayoutParams lpimage = new ViewGroup.LayoutParams(w, h);
        container.addView(imageView, index, lpimage);
        views.add(index, imageView);
        imageView.setImageSource(url);
    }

    public void addCellCard(UrlCard card) {
        int index = container.getChildCount();
        KoalaCardView cardView = new KoalaCardView(context, card);
        LinearLayout.LayoutParams lpCard = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(cardView, index, lpCard);
        views.add(index, cardView);
    }

    public void addSlider() {
        int index = container.getChildCount();
        View v = container.getFocusedChild();
        if(v != null) {
            index = container.indexOfChild(v);
        }
        KoalaSliderView sliderView = new KoalaSliderView(context);
        LinearLayout.LayoutParams lpslider = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(sliderView, index + 1, lpslider);
        views.add(index + 1, sliderView);
        KoalaEditTextView editTextView = new KoalaEditTextView(context, onPressEnterListener, statusListener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(editTextView, index + 2, lp);
        views.add(index + 2, editTextView);
        editTextView.requestFocus();
        if(v instanceof KoalaEditTextView) {
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
        views.add(index + 1, codeView);
        codeView.requestFocus();
        codeView.addCode();
    }

    private KoalaEditTextView.OnEditListener onPressEnterListener = new KoalaEditTextView.OnEditListener() {
        @Override
        public void insertEdit(KoalaBaseCellView v) {
            if(v instanceof KoalaEditTextView) {
                KoalaEditTextView view = (KoalaEditTextView) v;
                int index = container.indexOfChild(view);
                KoalaEditTextView editTextView = new KoalaEditTextView(context, onPressEnterListener, statusListener);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                container.addView(editTextView, index, lp);
                views.add(index, editTextView);
                editTextView.requestFocus();
                editTextView.setSelection(0);
            }
            setHint();
        }

        @Override
        public void pressEnter(KoalaBaseCellView v) {
            if(v instanceof KoalaEditTextView) {
                KoalaEditTextView view = (KoalaEditTextView) v;
                CharSequence p, n;
                int index = container.indexOfChild(view);
                int start = view.getSelectionStart();
                if(start < view.getText().length()) {
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
                views.add(index + 1, editTextView);
                setDragAndDrop(editTextView);
                editTextView.requestFocus();
                editTextView.setSelection(0);
                if(view.section == 1) {
                    editTextView.setNumberSection(editTextView);
                } else if(view.section == 2) {
                    editTextView.setDotSection(editTextView);
                }
                editTextView.setNextSection(editTextView);
            }
            setHint();
        }

        @Override
        public void deleteSelf(KoalaBaseCellView v) {
            if(views.size() > 1) {
                int index = container.indexOfChild((View) v);
                CharSequence lastStr = v.getText();
                if(index > 0) {
                    View view = container.getChildAt(index - 1);
                    if(view instanceof KoalaEditTextView) {
                        container.removeView((View) v);
                        views.remove(v);
                        ((KoalaEditTextView) view).append(lastStr, ((KoalaEditTextView) view).length());
                        view.requestFocus();
                        ((KoalaEditTextView)view).setNextSection(((KoalaEditTextView)view));
                    } else if(view instanceof KoalaImageView) {
                        container.removeView((View) v);
                        views.remove(v);
                        container.removeView(view);
                        views.remove(view);
                        if(index > 1) {
                            View pprev = container.getChildAt(index - 2);
                            if(pprev instanceof KoalaEditTextView) {
                                ((KoalaEditTextView) pprev).append(lastStr, ((KoalaEditTextView) pprev).length());
                                pprev.requestFocus();
                                ((KoalaEditTextView) pprev).setNextSection((KoalaEditTextView) pprev);
                            }
                        }

                    } else if(view instanceof KoalaSliderView) {
                        container.removeView((View) v);
                        views.remove(v);
                        container.removeView(view);
                        views.remove(view);
                        if(index > 1) {
                            View pprev = container.getChildAt(index - 2);
                            if(pprev instanceof KoalaEditTextView) {
                                ((KoalaEditTextView) pprev).append(lastStr, ((KoalaEditTextView) pprev).length());
                                pprev.requestFocus();
                                ((KoalaEditTextView) pprev).setNextSection((KoalaEditTextView) pprev);
                            }
                        }
                    } else if(view instanceof KoalaBaseCellView) {
                        container.removeView((View) v);
                        views.remove(v);
                        container.removeView(view);
                        views.remove(view);
                        if(index > 1) {
                            View pprev = container.getChildAt(index - 2);
                            if(pprev instanceof KoalaEditTextView) {
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
            if(!TextUtils.isEmpty(p)) {
                v.setText(p);
                v.setQuote();
                for(int i = 1; i < ss.length + 1; i++) {
                    KoalaEditTextView c = new KoalaEditTextView(context, onPressEnterListener, statusListener);
                    container.addView(c, index + i, lp);
                    views.add(index + i, c);
                    c.setText(ss[i - 1]);
                    c.setSection(section);
                    if(i == 1) {
                        c.requestFocus();
                        c.setSelection(0);
                    }
                    c.setTextStyle(style);
                }
                index = index + ss.length + 1;
            } else {
                for(int i = 0; i < ss.length; i++) {
                    if(i == 0) {
                        v.setText(ss[i]);
                        ((KoalaEditTextView)v).requestFocus();
                        ((KoalaEditTextView)v).setSelection(0);
                        v.setSection(section);
                        ((KoalaEditTextView)v).setTextStyle(style);
                    } else {
                        KoalaEditTextView c = new KoalaEditTextView(context, onPressEnterListener, statusListener);
                        container.addView(c, index + i, lp);
                        views.add(index + i, c);
                        c.setText(ss[i]);
                        c.setSection(section);
                        c.setTextStyle(style);
                    }
                }
                index = index + ss.length;
            }
            if(n != null) {
                KoalaEditTextView ne = new KoalaEditTextView(context, onPressEnterListener, statusListener);
                container.addView(ne, index);
                views.add(index, ne);
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
            views.remove(v);
            for(KoalaBaseCellView kcv : views) {
                if (kcv instanceof KoalaImageView) {
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

    private void setHint() {
        if(container.getChildAt(0) instanceof KoalaEditTextView) {
            KoalaEditTextView v = (KoalaEditTextView) container.getChildAt(0);
            if(container.getChildCount() == 1 && v.getEditView().length() == 0) {
                v.getEditView().setHint("输入内容");
            } else {
                v.getEditView().setHint("");
            }
        }
    }

    private KoalaEditTextView.OnEditTextStatusListener statusListener = new KoalaEditTextView.OnEditTextStatusListener() {

        @Override
        public void setEnableKeyBoard(boolean enable) {
            if(keyStatusListener == null) {
                return;
            }
            if(enable) {
                keyStatusListener.setEnableKeyBoard(true);
            } else {
                keyStatusListener.setEnableKeyBoard(false);
            }
        }

        @Override
        public void setEnableFocus(boolean enable) {
            for(KoalaBaseCellView v : views) {
                if(v.getType() == KoalaBaseCellView.EDIT_VIEW) {
                    v.setEditable(false);
                }
            }
        }
    };

    public interface OnStatusListener {
        void setEnableKeyBoard(boolean enableKeyBoard);
    }
}
