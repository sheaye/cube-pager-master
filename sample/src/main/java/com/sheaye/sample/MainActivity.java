package com.sheaye.sample;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sheaye.widget.CubePager;
import com.sheaye.widget.CubePagerAdapter;
import com.sheaye.widget.DotsLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    protected CubePager mCubePager;
    protected DotsLayout mDotsLayout;
    protected PicAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCubePager = ((CubePager) findViewById(R.id.m_cube_pager));
        mDotsLayout = ((DotsLayout) findViewById(R.id.m_dots_layout));
        mCubePager.setAutoMove(true);
        mPagerAdapter = new PicAdapter(this);
        mCubePager.setAdapter(mPagerAdapter);
        mDotsLayout.setUpWithCubePager(mCubePager);

        List<Integer> picList = new ArrayList<>();
        TypedArray typedArray = getResources().obtainTypedArray(R.array.pictures);
        for (int i = 0; i < typedArray.length(); i++) {
            picList.add(typedArray.getResourceId(i, 0));
        }
        typedArray.recycle();
        mPagerAdapter.addAll(picList);
    }

    class PicAdapter extends CubePagerAdapter<Integer> {

        public PicAdapter(Context context) {
            super(context);
        }

        @Override
        public View getItemView(int position, ViewGroup parent, View convertView, Integer item) {
            ImageView imageView;
            if (convertView != null) {
                imageView = (ImageView) convertView;
            }else {
                imageView = new ImageView(mContext);
                imageView.setScaleType(ImageView.ScaleType.CENTER);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
            imageView.setImageResource(item);
            return imageView;
        }
    }
}
