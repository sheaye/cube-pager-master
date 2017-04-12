package com.sheaye.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by yexinyan on 2017/4/12.
 */

public abstract class CubePagerAdapter<T> {

    protected Context mContext;
    private List<T> mData;
    private SparseArray<View> mViews;
    private DataSetObserver mObserver;

    public CubePagerAdapter(Context context) {
        mContext = context;
        mData = new ArrayList<>();
        mViews = new SparseArray<>();
    }

    public void addAll(Collection<? extends T> collection) {
        mData.addAll(collection);
        notifyDataSetChanged();
    }

    public void addAll(T[] t) {
        Collections.addAll(mData, t);
        notifyDataSetChanged();
    }

    public final int getCount() {
        return mData != null ? mData.size() : 0;
    }

    public T getItem(int position) {
        if (mData.size() > position) {
            return mData.get(position);
        }
        return null;
    }

    final View instantiateItem(ViewGroup container, int position) {
        View view = mViews.get(position);
        if (view == null) {
            view = getView(getLayoutInflater(), container, position, mData.get(position));
            mViews.append(position, view);
        }
        return view;
    }

    final void destroyItem(ViewGroup container, int position, Object object) {
        View view = mViews.get(position);
        if (view != null) {
            container.removeView(view);
        }
    }

    public abstract View getView(LayoutInflater layoutInflater, ViewGroup container, int position, T item);

    private LayoutInflater getLayoutInflater() {
        return LayoutInflater.from(mContext);
    }

    void setCubeObserver(DataSetObserver dataSetObserver) {
        mObserver = dataSetObserver;
    }

    private void notifyDataSetChanged() {
        synchronized (this) {
            if (mObserver != null) {
                mObserver.onChanged();
            }
        }
    }
}
