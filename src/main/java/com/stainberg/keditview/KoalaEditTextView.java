package com.stainberg.keditview;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.XMLReader;

/**
 * Created by Stainberg on 7/5/17.
 */

public class KoalaEditTextView extends FrameLayout implements KoalaBaseCellView {

    public static final int S_H1 = 1;
    public static final int S_H2 = 1<<1;
    public static final int S_G = 1<<2;
    public static final int S_B = 1<<3;
    public static final int S_Q = 1<<4;
    public static final int S_L = 1<<5;

    private View move;
    private KoalaEditText editText;
    private AppCompatTextView sectionText;
    private int index;
    private OnEditListener listener;
    private OnEditTextStatusListener statusListener;
    private OnHintSetListener onHintSetListener;
    private KoalaBaseCellView prev;
    private KoalaBaseCellView next;
    int style;
    private int gravity;
    int section;
    private boolean quote;
    private boolean code;
    private int sectionTop = 0;
    private int singleHeight = 0;
    private int sectionIndex = 0;
    private boolean showHint = true;
    public static final int SECTION_NULL = 0;
    public static final int SECTION_NUMBER = 1;
    public static final int SECTION_DOT = 2;
    public static final int GRAVITY_LEFT = 0;
    public static final int GRAVITY_CENTER = 1;
    public static final int GRAVITY_RIGHT = 2;
    public static final int STYLE_NORMAL = 0;
    public static final int STYLE_H1 = 1;
    public static final int STYLE_H2 = 2;

    public KoalaEditTextView(Context context) {
        super(context);
        init(context);
    }

    public KoalaEditTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public KoalaEditTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public KoalaEditTextView(Context context, OnEditListener l, OnEditTextStatusListener sl) {
        super(context);
        listener = l;
        statusListener = sl;
        init(context);
    }

    public KoalaEditTextView(Context context, OnEditListener l, OnEditTextStatusListener sl, OnHintSetListener hl) {
        super(context);
        listener = l;
        statusListener = sl;
        onHintSetListener = hl;
        init(context);
    }

    private void init(Context context) {
        style = STYLE_NORMAL;
        gravity = GRAVITY_LEFT;
        section = SECTION_NULL;
        quote = false;
        code = false;
        sectionIndex = 1;
        setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.white));
        editText = new KoalaEditText(context, onSelectionChangedListener);
        editText.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
        editText.setPadding((int) getResources().getDimension(R.dimen.text_padding_v),
                (int) getResources().getDimension(R.dimen.text_padding_h),
                (int) getResources().getDimension(R.dimen.text_padding_v),
                (int) getResources().getDimension(R.dimen.text_padding_h));
        editText.setLineSpacing(getResources().getDimension(R.dimen.normal_text), 1.0f);
        editText.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
        LayoutParams lpEdit = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        addView(editText, lpEdit);
        editText.setTextSize(getResources().getDimension(R.dimen.normal_text));
        editText.setTextColor(ContextCompat.getColor(getContext(), R.color.gray_text));
        editText.setOnKeyListener(keyListener);
        editText.setOnFocusChangeListener(onFocusChangeListener);
        editText.measure(MeasureSpec.getMode(0), MeasureSpec.getMode(0));
        singleHeight = editText.getMeasuredHeight();
        sectionTop = (int) ((singleHeight - getResources().getDimension(R.dimen.section_radio)) / 2);
        sectionText = new AppCompatTextView(context);
        sectionText.setVisibility(INVISIBLE);
        sectionText.setSingleLine(true);
        sectionText.setMaxLines(1);
        addView(sectionText);
        if(onHintSetListener != null) {
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 0) {
                        showHint = true;
                        onHintSetListener.onHintChanged();
                    } else {
                        if(showHint) {
                            showHint = false;
                            onHintSetListener.onHintChanged();
                        }
                    }
                }
            });
        }
        move = new View(context);
        FrameLayout.LayoutParams l0 = new FrameLayout.LayoutParams(120, 60);
        l0.gravity = Gravity.END;
        move.setBackgroundColor(Color.parseColor("#00FF00"));
        addView(move, l0);
        move.setVisibility(GONE);
    }

    public void enableDrag(boolean enable) {
        if(enable) {
            move.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (statusListener != null) {
                        statusListener.setEnableFocus(false);
                    }
                    KoalaEditTextView.this.startDrag(ClipData.newPlainText("text", getHtmlText()), new KoalaDragShadowBuilder(KoalaEditTextView.this), new DragState(KoalaEditTextView.this), 0);
                    return true;
                }
            });
            move.setVisibility(VISIBLE);
        } else {
            move.setOnLongClickListener(null);
            move.setVisibility(GONE);
        }
    }

    public int getSelectionStart() {
        return editText.getSelectionStart();
    }

    public void setText(String p) {
        editText.setText(p);
    }

    public void setSelection(int selection) {
        editText.setSelection(selection);
    }

    public int length() {
        return editText.length();
    }

    public KoalaEditText getEditView() {
        return editText;
    }

    @Deprecated
    @Override
    public void reload() {

    }

    @Override
    public void setStyleH1() {
        if(quote) {
            cleanQuote(KoalaEditTextView.this, SECTION_NULL, STYLE_H1);
            return;
        }
        cleanSection(KoalaEditTextView.this);
        if(style != STYLE_H1) {
            editText.setTextSize(getResources().getDimension(R.dimen.large_text));
            editText.setTextColor(ContextCompat.getColor(getContext(), R.color.black_text));
            style = STYLE_H1;
        } else {
            setStyleNormal();
        }
    }

    @Override
    public void setStyleH2() {
        if(quote) {
            cleanQuote(KoalaEditTextView.this, SECTION_NULL, STYLE_H2);
            return;
        }
        cleanSection(KoalaEditTextView.this);
        if(style != STYLE_H2) {
            editText.setTextSize(getResources().getDimension(R.dimen.middle_text));
            editText.setTextColor(ContextCompat.getColor(getContext(), R.color.black_text));
            style = STYLE_H2;
        } else {
            setStyleNormal();
        }
    }

    @Override
    public void setStyleNormal() {
        if(quote) {
            cleanQuote(KoalaEditTextView.this, SECTION_NULL, STYLE_NORMAL);
            return;
        }
        cleanSection(KoalaEditTextView.this);
        editText.setTextSize(getResources().getDimension(R.dimen.normal_text));
        editText.setTextColor(ContextCompat.getColor(getContext(), R.color.gray_text));
        style = STYLE_NORMAL;
    }

    @Deprecated
    @Override
    public String getUrl() {
        return null;
    }

    public void append(CharSequence text, int start) {
        editText.append(text, 0, text.length());
        editText.setSelection(start);
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
    public int getType() {
        return EDIT_VIEW;
    }

    @Override
    public void setGravity() {
        if(gravity == GRAVITY_LEFT) {//left
            editText.setGravity(Gravity.CENTER_HORIZONTAL);
            gravity = GRAVITY_CENTER;
        } else if(gravity == GRAVITY_CENTER) {//middle
            editText.setGravity(Gravity.END);
            gravity = GRAVITY_RIGHT;
        } else {//right
            editText.setGravity(Gravity.START);
            gravity = GRAVITY_LEFT;
        }
    }

    @Override
    public void setQuote() {
        if(quote) {
            int start = editText.getSelectionStart();
            int end = editText.getSelectionEnd();
            if(start != end) {
                cleanQuote(KoalaEditTextView.this, SECTION_NULL, style);
            } else {
                cleanAllQuote(KoalaEditTextView.this);
            }
        } else {
            markQuote(KoalaEditTextView.this);
        }
    }

    @Override
    public void setSection(int st) {
        if(quote) {
            cleanQuote(KoalaEditTextView.this, st, style);
            return;
        }
        if(section == st) {
            cleanSection(KoalaEditTextView.this);
        } else {
            if (st == SECTION_NUMBER) {//normal to No.
                setNumberSection(KoalaEditTextView.this);
            } else if (st == SECTION_DOT) {//No. to dot
                setDotSection(KoalaEditTextView.this);
            } else {//dot to normal
                cleanSection(KoalaEditTextView.this);
            }
        }
        if(next != null && next instanceof KoalaEditTextView && ((KoalaEditTextView) next).section != SECTION_NULL) {
            resetNextSection(KoalaEditTextView.this);
        }
    }

    @Override
    public void setBold() {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if(start < end) {
            boolean bold = false;
            SpannableString ssb = new SpannableString(editText.getText());
            StyleSpan[] spans = ssb.getSpans(start, end, StyleSpan.class);
            for(StyleSpan span : spans) {
                if(span.getStyle() == Typeface.BOLD) {
                    if(ssb.getSpanStart(span) == start) {
                        bold = true;
                    }
                    ssb.removeSpan(span);
                }
            }
            if(!bold) {
                ssb.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            editText.setText(ssb);
            editText.setSelection(start, end);
        } else {
            boolean hasNormal = false;
            CharSequence s = editText.getText();
            SpannableStringBuilder ssb = new SpannableStringBuilder(s);
            StyleSpan[] spans = editText.getEditableText().getSpans(0, editText.getText().length(), StyleSpan.class);
            if(spans.length == 0) {
                hasNormal = true;
            }
            for(StyleSpan sp : spans) {
                if(sp.getStyle() == Typeface.NORMAL) {
                    ssb.removeSpan(sp);
                    hasNormal = true;
                }
                if(sp.getStyle() == Typeface.BOLD) {
                    ssb.removeSpan(sp);
                }
            }
            if (hasNormal) {
                ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                ssb.setSpan(new StyleSpan(Typeface.NORMAL), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            editText.setText(ssb);
            editText.setSelection(start);
        }
    }

    @Override
    public void setItalic() {
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if(start < end) {
            boolean strike = false;
            SpannableString ssb = new SpannableString(editText.getText());
            StyleSpan[] spans = ssb.getSpans(start, end, StyleSpan.class);
            for(StyleSpan span : spans) {
                if(span.getStyle() == Typeface.ITALIC) {
                    if(ssb.getSpanStart(span) == start) {
                        strike = true;
                    }
                    ssb.removeSpan(span);
                }
            }
            if(!strike) {
                ssb.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            editText.setText(ssb);
            editText.setSelection(start, end);
        }
    }

    @Override
    public void setStrike() {
        int styleSpan = 0;
        boolean strike = false;
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if(start < end) {
            CharSequence t1 = editText.getText().subSequence(0, start);
            CharSequence t2 = editText.getText().subSequence(start, end);
            CharSequence t3 = editText.getText().subSequence(end, editText.length());
            StyleSpan[] spans = editText.getEditableText().getSpans(start, end, StyleSpan.class);
            StrikethroughSpan[] spans2 = editText.getEditableText().getSpans(start, end, StrikethroughSpan.class);
            if(spans.length > 0) {
                styleSpan = spans[0].getStyle();
            }
            if(spans2.length > 0) {
                strike = true;
            }
            SpannableStringBuilder ssb = new SpannableStringBuilder(t2);
            ssb.clearSpans();
            ssb.setSpan(new StyleSpan(styleSpan), 0, t2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if(!strike) {
                ssb.setSpan(new StrikethroughSpan(), 0, t2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            editText.setText(t1);
            editText.append(ssb);
            editText.append(t3);
            editText.setSelection(start, end);
        }
    }

    @Override
    public void addCode() {
        markCode(KoalaEditTextView.this);
    }

    @Override
    public void setText(CharSequence sequence) {
        editText.setText(sequence);
    }

    @Override
    public CharSequence getText() {
        return editText.getText();
    }

    @Override
    public String getHtmlText() {
        String s = StringEscapeUtils.unescapeHtml(Html.toHtml(editText.getText()));
        s = s.replaceAll("<strike>", "<del>");
        s = s.replaceAll("</strike>", "</del>");
        if(TextUtils.isEmpty(s)) {
            return null;
        }
        if(s.substring(s.length() - 1, s.length()).equals("\n")) {
            s = s.substring(0, s.length() - 1);
        }
        Document doc = Jsoup.parseBodyFragment(s);
        Element body = doc.body();
        Elements allElements = body.children();
        for (int i = 0; i < allElements.size(); i++) {
            Element e = allElements.get(i);
            if (e.tagName().equals("p")) {
                return e.html();
            }
        }
        return s;
    }

    @Override
    public void setHtmlText(String html) {
        Spanned spanned = Html.fromHtml(html, null, new HtmlTagHandler());
        editText.setText(spanned);
    }

    @Override
    public boolean isQuote() {
        return quote;
    }

    @Override
    public boolean isCode() {
        return code;
    }

    @Override
    public int getStyle() {
        return style;
    }

    @Override
    public int getSection() {
        return section;
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
        editText.setEnabled(enable);
        editText.setFocusable(enable);
    }

    public void setTextStyle(int syl) {
        if(syl == STYLE_H1) {
            setStyleH1();
        } else if(syl == STYLE_H2) {
            setStyleH2();
        } else {
            setStyleNormal();
        }
    }

    private void markCode(KoalaEditTextView v) {
        v.setBackgroundResource(R.drawable.shape_edit_code_bg);
        v.code = true;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
        lp.topMargin = 40;
        lp.bottomMargin = 40;
        v.setLayoutParams(lp);
        v.cleanSection(v);
        v.resetNextSection(v);
    }

    void setNumberSection(KoalaEditTextView v) {
        if(quote) {
            cleanQuote(KoalaEditTextView.this, SECTION_NUMBER, STYLE_NORMAL);
            return;
        }
        v.setStyleNormal();
        v.sectionText.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);
        LayoutParams lpSelect = new LayoutParams((int) (getResources().getDimension(R.dimen.section_margin)), v.singleHeight);
        lpSelect.topMargin = 0;
        lpSelect.leftMargin = 0;
        v.sectionText.setLayoutParams(lpSelect);
        v.sectionText.setBackgroundResource(0);
        v.sectionText.setVisibility(VISIBLE);
        LayoutParams lpEdit = (LayoutParams) v.editText.getLayoutParams();
        lpEdit.leftMargin = (int) (getResources().getDimension(R.dimen.section_margin)) + (int) (getResources().getDimension(R.dimen.section_text_margin));
        v.editText.setLayoutParams(lpEdit);
        if(v.prev != null) {
            if(v.prev instanceof KoalaEditTextView) {
                if(((KoalaEditTextView) v.prev).section == 1) {
                    if(((KoalaEditTextView) v.prev).quote) {
                        v.sectionIndex = 1;
                    } else {
                        v.sectionIndex = ((KoalaEditTextView) v.prev).sectionIndex + 1;
                    }
                } else {
                    v.sectionIndex = 1;
                }
            }
        }
        v.section = 1;
        v.sectionText.setText(v.sectionIndex + ".");
    }

    void setDotSection(KoalaEditTextView v) {
        if(quote) {
            cleanQuote(KoalaEditTextView.this, SECTION_DOT, STYLE_NORMAL);
            return;
        }
        v.setStyleNormal();
        v.sectionText.setText("");
        LayoutParams lpSelect = new LayoutParams((int) getResources().getDimension(R.dimen.section_radio), (int) getResources().getDimension(R.dimen.section_radio));
        lpSelect.topMargin = sectionTop;
        lpSelect.leftMargin = (int) (getResources().getDimension(R.dimen.section_margin) - (int) (getResources().getDimension(R.dimen.section_text_margin)));
        lpSelect.rightMargin = 0;
        v.sectionText.setLayoutParams(lpSelect);
        v.sectionText.setBackgroundResource(R.drawable.shape_section_dot);
        v.sectionText.setVisibility(VISIBLE);
        LayoutParams lpEdit = (LayoutParams) v.editText.getLayoutParams();
        lpEdit.leftMargin = (int) (getResources().getDimension(R.dimen.section_margin)) + (int) (getResources().getDimension(R.dimen.section_text_margin));
        v.editText.setLayoutParams(lpEdit);
        v.sectionIndex = 1;
        v.section = 2;
    }

    void cleanSection(KoalaEditTextView v) {
        v.sectionText.setVisibility(INVISIBLE);
        v.section = SECTION_NULL;
        v.sectionIndex = 1;
        LayoutParams lpEdit = (LayoutParams) v.editText.getLayoutParams();
        lpEdit.leftMargin = 0;
        v.editText.setLayoutParams(lpEdit);
        if(next != null && next instanceof KoalaEditTextView && ((KoalaEditTextView) next).section != SECTION_NULL) {
            resetNextSection(KoalaEditTextView.this);
        }
    }

    void setNextSection(KoalaEditTextView v) {
        if(v != null && v.prev != null && v.prev instanceof KoalaEditTextView) {
            if(((KoalaEditTextView) v.prev).section == v.section && v.section == SECTION_NUMBER && !v.quote) {
                v.setNumberSection(v);
            } else if(((KoalaEditTextView) v.prev).section == v.section  && v.section == SECTION_DOT && !v.quote) {
                v.setDotSection(v);
            }
            if(v.next instanceof KoalaEditTextView && v.section == ((KoalaEditTextView) v.next).section) {
                v.setNextSection((KoalaEditTextView) v.next);
            }
        }
    }

    public void resetNextSection(KoalaEditTextView v) {
        if(v != null) {
            if (v.next != null && v.next instanceof KoalaEditTextView) {
                if(!v.quote) {
                    if(((KoalaEditTextView) v.next).section == v.section) {
                        if (v.section == SECTION_NUMBER) {
                            ((KoalaEditTextView) v.next).setNumberSection((KoalaEditTextView) v.next);
                        } else if (v.section == SECTION_DOT) {
                            ((KoalaEditTextView) v.next).setDotSection((KoalaEditTextView) v.next);
                        } else {
                            ((KoalaEditTextView) v.next).cleanSection((KoalaEditTextView) v.next);
                        }
                    } else {
                        if(((KoalaEditTextView) v.next).section == SECTION_NUMBER) {
                            ((KoalaEditTextView) v.next).setNumberSection((KoalaEditTextView) v.next);
                        } else if (((KoalaEditTextView) v.next).section == SECTION_DOT) {
                            ((KoalaEditTextView) v.next).setDotSection((KoalaEditTextView) v.next);
                        } else {
                            ((KoalaEditTextView) v.next).cleanSection((KoalaEditTextView) v.next);
                        }
                    }
                } else {
                    if(((KoalaEditTextView) v.next).section == SECTION_NUMBER) {
                        ((KoalaEditTextView) v.next).setNumberSection((KoalaEditTextView) v.next);
                    } else if(((KoalaEditTextView) v.next).section == SECTION_DOT) {
                        ((KoalaEditTextView) v.next).setDotSection((KoalaEditTextView) v.next);
                    } else {
                        ((KoalaEditTextView) v.next).cleanSection((KoalaEditTextView) v.next);
                    }
                }
                v.resetNextSection((KoalaEditTextView) v.next);
            }
        }
    }

    private void cleanQuote(KoalaEditTextView v, int section, int setStyle) {
        CharSequence p, n, s;
        v.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.white));
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
        lp.topMargin = 0;
        lp.bottomMargin = 0;
        v.setLayoutParams(lp);
        v.editText.setPadding((int) getResources().getDimension(R.dimen.text_padding_v),
                (int) getResources().getDimension(R.dimen.text_padding_h),
                (int) getResources().getDimension(R.dimen.text_padding_v),
                (int) getResources().getDimension(R.dimen.text_padding_h));
        v.quote = false;
        if (listener != null) {
            int start = v.editText.getSelectionStart();
            int end = v.editText.getSelectionEnd();
            if(start == end) {
                int f = v.editText.getText().subSequence(0, start).toString().lastIndexOf('\n');
                int l = v.editText.getText().subSequence(end, editText.getText().length()).toString().indexOf('\n');
                if (l != -1) {
                    l = l + end;
                } else {
                    l = editText.getText().length();
                }
                if (f != -1) {
                    p = v.editText.getText().subSequence(0, f);
                } else {
                    p = null;
                }
                if (l < editText.getText().length()) {
                    n = v.editText.getText().subSequence(l + 1, editText.getText().length());
                } else {
                    n = null;
                }
                s = v.editText.getText().subSequence(f + 1, l);
            } else {
                p = v.editText.getText().subSequence(0, start);
                s = v.editText.getText().subSequence(start, end);
                n = v.editText.getText().subSequence(end, editText.getText().length());
            }

            listener.splitSelf(v, p, s, n, section, setStyle);
        }
    }

    private void cleanAllQuote(KoalaEditTextView v) {
        v.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.white));
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
        lp.topMargin = 0;
        lp.bottomMargin = 0;
        v.setLayoutParams(lp);
        v.editText.setPadding((int) getResources().getDimension(R.dimen.text_padding_v),
                (int) getResources().getDimension(R.dimen.text_padding_h),
                (int) getResources().getDimension(R.dimen.text_padding_v),
                (int) getResources().getDimension(R.dimen.text_padding_h));
        v.quote = false;
        listener.splitSelf(v, null, v.editText.getText(), null, SECTION_NULL, STYLE_NORMAL);
    }

    private void markQuote(KoalaEditTextView v) {
        v.setBackgroundResource(R.drawable.shape_edit_quote_bg);
        v.quote = true;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
        lp.topMargin = (int) getResources().getDimension(R.dimen.section_text_margin);
        lp.bottomMargin = (int) getResources().getDimension(R.dimen.section_text_margin);
        v.setLayoutParams(lp);
        v.editText.setPadding((int) getResources().getDimension(R.dimen.text_quote_padding_v),
                (int) getResources().getDimension(R.dimen.text_quote_padding_h),
                (int) getResources().getDimension(R.dimen.text_quote_padding_v),
                (int) getResources().getDimension(R.dimen.text_quote_padding_h));
        v.cleanSection(v);
        v.resetNextSection(v);
    }

    private OnKeyListener keyListener = new OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            int start = editText.getSelectionStart();
            if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (listener != null) {
                    if(code) {
                        if(editText.getSelectionEnd() == editText.getText().length()
                                && editText.getText().toString().charAt(editText.getText().toString().length() - 1) == ('\n')) {
                            editText.setText(editText.getText().subSequence(0, editText.getText().toString().length() - 1));
                            editText.setSelection(editText.length());
                            listener.pressEnter(KoalaEditTextView.this);
                            return true;
                        }
                        return false;
                    }
                    if(section == SECTION_NULL) {
                        if(!quote) {
                            listener.pressEnter(KoalaEditTextView.this);
                            return true;
                        } else {
                            if(editText.getSelectionEnd() == editText.getText().length()
                                    && editText.getText().toString().charAt(editText.getText().toString().length() - 1) == ('\n')) {
                                editText.setText(editText.getText().subSequence(0, editText.getText().toString().length() - 1));
                                editText.setSelection(editText.length());
                                listener.pressEnter(KoalaEditTextView.this);
                                return true;
                            }
                            if(editText.getSelectionStart() == editText.getSelectionEnd() && editText.getSelectionStart() == 1
                                    && editText.getText().toString().charAt(0) == ('\n')) {
                                editText.setText(editText.getText().subSequence(1, editText.getText().toString().length()));
                                listener.insertEdit(KoalaEditTextView.this);
                                return true;
                            }
                        }
                    } else {
                        if(TextUtils.isEmpty(editText.getText().toString().trim())) {
                            cleanSection(KoalaEditTextView.this);
                            resetNextSection(KoalaEditTextView.this);
                            return true;
                        } else {
                            listener.pressEnter(KoalaEditTextView.this);
                            return true;
                        }
                    }
                    return false;
                }
            } else if (start == 0 && keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                if(quote) {
                    if(KoalaEditTextView.this.prev == null) {
                        cleanAllQuote(KoalaEditTextView.this);
                        return false;
                    } else {
                        if(!(prev instanceof KoalaEditTextView)) {
                            cleanAllQuote(KoalaEditTextView.this);
                            return false;
                        } else {
                            if(!((KoalaEditTextView) prev).quote) {
                                cleanAllQuote(KoalaEditTextView.this);
                                return false;
                            }
                        }
                    }
                }
                if(section != SECTION_NULL) {
                    if(prev == null) {
                        cleanSection(KoalaEditTextView.this);
                        resetNextSection(KoalaEditTextView.this);
                        return false;
                    } else {
                        if(!(prev instanceof KoalaEditTextView)) {
                            cleanSection(KoalaEditTextView.this);
                            resetNextSection(KoalaEditTextView.this);
                            return false;
                        } else {
                            if(((KoalaEditTextView) prev).section != section) {
                                cleanSection(KoalaEditTextView.this);
                                resetNextSection(KoalaEditTextView.this);
                                return false;
                            }
                        }
                    }
                }
                if (listener != null) {
                    listener.deleteSelf(KoalaEditTextView.this);
                }
            }
            return false;
        }
    };

    private OnFocusChangeListener onFocusChangeListener = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(statusListener != null && v == editText) {
                if (hasFocus) {
                    statusListener.setEnableKeyBoard(true);
                } else {
                    statusListener.setEnableKeyBoard(false);
                }
            }
        }
    };

    private KoalaEditText.OnSelectionChangedListener onSelectionChangedListener = new KoalaEditText.OnSelectionChangedListener() {

        @Override
        public void onSelectionChanged(int selStart, int selEnd) {
            if(statusListener != null) {
                int s = 0;
                if(getStyle() == STYLE_H1) {
                    s = s|S_H1;
                } else if(getStyle() == STYLE_H2) {
                    s = s|S_H2;
                }
                boolean bold = false;
                SpannableString ssb = new SpannableString(editText.getText());
                StyleSpan[] spans = ssb.getSpans(selStart, selEnd, StyleSpan.class);
                for(StyleSpan span : spans) {
                    if(span.getStyle() == Typeface.BOLD) {
                        if(ssb.getSpanStart(span) == selStart) {
                            bold = true;
                        }
                    }
                }
                if(!bold) {
                    s = s|S_B;
                }
                if(gravity != GRAVITY_LEFT) {
                    s = s|S_G;
                }
                if(quote) {
                    s = s|S_Q;
                }
                if(section != 0) {
                    s= s|S_L;
                }
                statusListener.onEditStatus(s);
            }
        }
    };

    interface OnEditListener {
        void insertEdit(KoalaBaseCellView v);
        void pressEnter(KoalaBaseCellView v);
        void deleteSelf(KoalaBaseCellView v);
        void splitSelf(KoalaBaseCellView v, CharSequence p, CharSequence s, CharSequence n, int section, int style);
    }

    interface OnEditTextStatusListener {
        void setEnableKeyBoard(boolean enable);
        void setEnableFocus(boolean enable);
        void onEditStatus(int status);
    }

    interface OnHintSetListener {
        void onHintChanged();
    }

    private class HtmlTagHandler implements Html.TagHandler {

        @Override
        public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
            if (tag.equals("strike") || tag.equals("del")) {//自定义解析<strike></strike>标签
                int len = output.length();
                if (opening) {//开始解析该标签，打一个标记
                    output.setSpan(new StrikethroughSpan() , len , len , Spannable.SPAN_MARK_MARK);
                } else {//解析结束，读出所有标记，取最后一个标记为当前解析的标签的标记（因为解析方式是便读便解析）
                    StrikethroughSpan[] spans = output.getSpans(0 , len , StrikethroughSpan.class);
                    if (spans.length > 0) {
                        for (int i = 0; i < spans.length; i++) {
                            if (output.getSpanFlags(spans[i]) == Spannable.SPAN_MARK_MARK) {
                                int start = output.getSpanStart(spans[i]);
                                output.removeSpan(spans[i]);
                                if (start != len) {
                                    output.setSpan(new StrikethroughSpan() , start , len , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}
