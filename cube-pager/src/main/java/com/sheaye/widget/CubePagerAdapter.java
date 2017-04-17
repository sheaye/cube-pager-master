package com.sheaye.widget;

import android.content.Context;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.util.Log;
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
    private DataSetObserver mObserver;
    private DataSetObservable mObservable = new DataSetObservable();
    private View mConvertView;

    public CubePagerAdapter(Context context) {
        mContext = context;
        mData = new ArrayList<>();
    }

    public CubePagerAdapter(Context mContext, List<T> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    public void addAll(Collection<? extends T> collection) {
        mData.addAll(collection);
        notifyDataSetChanged();
    }

    public void addAll(T[] t) {
        Collections.addAll(mData, t);
        notifyDataSetChanged();
    }

    public void clear() {
        mData.clear();
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

    final View instantiateItem(ViewGroup parent, int position) {
        View view = getItemView(position, parent, mConvertView, mData.get(position));
        return view;
    }

    final void destroyItem(ViewGroup parent, int position, int index) {
        mConvertView = parent.getChildAt(index);
        if (mConvertView != null) {
            parent.removeView(mConvertView);
        }
    }

    public abstract View getItemView(int position, ViewGroup parent, View convertView, T item);

    private LayoutInflater getLayoutInflater() {
        return LayoutInflater.from(mContext);
    }

    void setCubeObserver(DataSetObserver dataSetObserver) {
        mObserver = dataSetObserver;
    }

    public void notifyDataSetChanged() {
        mConvertView = null;
        synchronized (this) {
            if (mObserver != null) {
                mObserver.onChanged();
            }
        }
        mObservable.notifyChanged();
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        mObservable.registerObserver(observer);
    }

    public void unregisterObserver(DataSetObserver observer) {
        mObservable.unregisterObserver(observer);
    }

    public List<T> getData() {
        return mData;
    }
}
