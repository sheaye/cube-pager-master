package com.sheaye.sample;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sheaye.widget.CubePager;
import com.sheaye.widget.CubePagerAdapter;
import com.sheaye.widget.DotsLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    protected CubePager mCubePager;
    private List<Integer> picList = Arrays.asList(R.drawable.pic1, R.drawable.pic2, R.drawable.pic3, R.drawable.pic4);
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
        mPagerAdapter.addAll(picList);

    }

    class PicAdapter extends CubePagerAdapter<Integer> {

        public PicAdapter(Context context) {
            super(context);
        }

        @Override
        public View getView(LayoutInflater layoutInflater, ViewGroup container, int position, Integer item) {
            ImageView imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setImageResource(item);
            return imageView;
        }
    }
}
