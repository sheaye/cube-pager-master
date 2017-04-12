package com.sheaye.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by yexinyan on 2017/4/11.
 */

public class DotsLayout extends LinearLayout implements CubePager.OnPageChangeListener {
    protected Context mContext;
    protected int mDotRadius;
    private DotsObserver mObserver;
    protected CubePager mCubePager;

    public DotsLayout(Context context) {
        this(context, null);
    }

    public DotsLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initDotsLayout(context, attrs);
    }

    private void initDotsLayout(Context context, AttributeSet attrs) {
        mContext = context;
        setGravity(Gravity.CENTER);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DotsLayout);
        mDotRadius = typedArray.getInteger(R.styleable.DotsLayout_radius, 10);
        typedArray.recycle();
    }

    /**
     * 请在CubePager.setAdapter之后使用此方法
     *
     * @param cubePager
     */
    public void setUpWithCubePager(CubePager cubePager) {
        mCubePager = cubePager;
        CubePagerAdapter pagerAdapter = mCubePager.getPagerAdapter();
        if (pagerAdapter == null) {
            throw new IllegalStateException("setUpWithCubePager之前请请为CubePager设置Adapter");
        }
        mObserver = new DotsObserver();
        pagerAdapter.registerDataSetObserver(mObserver);
        mCubePager.setOnPageChangeListener(this);
        onDataSetChanged();
    }

    @Override
    public void onPageChanged(int currentPosition, int oldPosition) {
        if (getChildCount() > currentPosition) {
            getChildAt(currentPosition).setSelected(true);
        }
        if (getChildCount() > oldPosition) {
            getChildAt(oldPosition).setSelected(false);
        }
    }

    public void onDataSetChanged() {
        removeAllViews();
        for (int i = 0; i < mCubePager.getItemsCount(); i++) {
            ImageView imageView = new ImageView(mContext);
            imageView.setImageResource(R.drawable.selector_dot);
            imageView.setLayoutParams(new LayoutParams(4 * mDotRadius, 4 * mDotRadius));
            imageView.setPadding(10, 10, 10, 10);
            addView(imageView);
        }
        if (getChildCount() > 0) {
            getChildAt(0).setSelected(true);
        }
    }

    private class DotsObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            onDataSetChanged();
        }
    }
}
