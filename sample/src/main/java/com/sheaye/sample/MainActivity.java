package com.sheaye.sample;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sheaye.widget.CubePager;
import com.sheaye.widget.CubePagerAdapter;
import com.sheaye.widget.DotsLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    protected PicAdapter mPagerAdapter;
    @BindView(R.id.m_cube_pager)
    CubePager mCubePager;
    @BindView(R.id.m_dots_layout)
    DotsLayout mDotsLayout;
    @BindView(R.id.m_seek_bar)
    SeekBar mSeekBar;
    @BindView(R.id.m_items_count_text)
    TextView mItemsCountText;
    protected List<Integer> mPicList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        int progress = mSeekBar.getProgress();
        mItemsCountText.setText(String.valueOf(progress));
        addPics(progress);

        mPagerAdapter = new PicAdapter(this, mPicList);
        mCubePager
                .setWith3D(false)
                .setAutoMove(true) // 设置自动翻页
                .setMaxRotate(50) // 设置旋转的最大夹角
                .setInterval(4000) // 设置翻页间隔时间，自动翻页为true有效
                .setDuration(2000) // 设置翻页持续时间，仅自动翻页为true有效
                .setAdapter(mPagerAdapter);

        mDotsLayout.setUpWithCubePager(mCubePager);
        mSeekBar.setOnSeekBarChangeListener(new OnProgressChangeListener());
    }

    private void addPics(int count) {
        TypedArray typedArray = getResources().obtainTypedArray(R.array.pictures);
        for (int i = 0; i < count; i++) {
            mPicList.add(typedArray.getResourceId(i, 0));
        }
        typedArray.recycle();
    }

    private class OnProgressChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mItemsCountText.setText(String.valueOf(progress));
            mPicList.clear();
            addPics(progress);
            mPagerAdapter.notifyDataSetChanged();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    class PicAdapter extends CubePagerAdapter<Integer> {

        public PicAdapter(Context mContext, List<Integer> mData) {
            super(mContext, mData);
        }

        @Override
        public View getItemView(int position, ViewGroup parent, View convertView, Integer item) {
            ImageView imageView;
            if (convertView != null) {
                imageView = (ImageView) convertView;
            } else {
                imageView = new ImageView(mContext);
                imageView.setScaleType(ImageView.ScaleType.CENTER);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
            imageView.setImageResource(item);
            return imageView;
        }
    }
}
