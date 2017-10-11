package com.stainberg.keditview;

import java.util.ArrayList;

/**
 * Created by Stainberg on 7/5/17.
 */

class EditList<T> extends ArrayList<T> {

    @Override
    public boolean add(T t) {
        boolean flag = super.add(t);
        if (t instanceof KoalaBaseCellView) {
            KoalaBaseCellView v = ((KoalaBaseCellView) t);
            if (indexOf(v) > 0) {
                KoalaBaseCellView prev = (KoalaBaseCellView) get(indexOf(v) - 1);
                v.setPrevView(prev);
                prev.setNextView(v);
                v.setNextView(null);
            } else {
                v.setPrevView(null);
                v.setNextView(null);
            }
            for (int i = 0; i < size(); i++) {
                KoalaBaseCellView vv = (KoalaBaseCellView) get(i);
                vv.setPosition(i);
            }
        } else {
            throw new IllegalArgumentException("T mast be implements KoalaBaseCellView");
        }
        return flag;
    }

    @Override
    public void add(int index, T t) {
        super.add(index, t);
        if (t instanceof KoalaBaseCellView) {
            KoalaBaseCellView v = ((KoalaBaseCellView) t);
            if (index > 0) {
                KoalaBaseCellView prev = (KoalaBaseCellView) get(index - 1);
                KoalaBaseCellView next = null;
                if (index < size() - 1) {
                    next = (KoalaBaseCellView) get(index + 1);
                }
                prev.setNextView(v);
                if (next != null) {
                    next.setPrevView(v);
                }
                v.setPrevView(prev);
                v.setNextView(next);
            } else {
                v.setPrevView(null);
                v.setNextView(null);
            }
            for (int i = 0; i < size(); i++) {
                KoalaBaseCellView vv = (KoalaBaseCellView) get(i);
                vv.setPosition(i);
            }
        } else {
            throw new IllegalArgumentException("T mast be implements KoalaBaseCellView");
        }
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof KoalaBaseCellView) {
            KoalaBaseCellView v = ((KoalaBaseCellView) o);
            if (indexOf(v) >= 0) {
                KoalaBaseCellView prev = v.getPrevView();
                KoalaBaseCellView next = v.getNextView();
                if (next != null) {
                    next.setPrevView(prev);
                }
                if (prev != null) {
                    prev.setNextView(next);
                }
                v.setPrevView(null);
                v.setNextView(null);
                boolean flag = super.remove(o);
                for (int i = 0; i < size(); i++) {
                    KoalaBaseCellView vv = (KoalaBaseCellView) get(i);
                    vv.setPosition(i);
                }
                return flag;
            }
        } else {
            throw new IllegalArgumentException("T mast be implements KoalaBaseCellView");
        }
        return false;
    }


}
