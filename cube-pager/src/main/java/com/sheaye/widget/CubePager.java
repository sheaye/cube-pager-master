package com.sheaye.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yexinyan on 2017/3/23.
 */

public class CubePager extends ViewGroup {

    private static final String TAG = "CubePager";
    private static final int SCROLL_TO_LEFT = 1;
    private static final int SCROLL_TO_RIGHT = -1;
    private int mScrollDirect;
    private CubePagerAdapter mPagerAdapter;
    private int mItemsCount;
    private Interpolator mInterpolator = new Interpolator() {
        @Override
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };
    private Scroller mScroller;
    private float mDownX;
    private int mTouchSlop;
    private float mLastMoveX;
    private float mMoveX;
    private int mWidth;
    private int mHeight;
    private int mLeftPosition;
    private int mRightPosition;
    private int mCurrentPosition;
    private Camera mCamera;
    private static float MAX_ROTATE = 50;
    private Matrix mMatrix;
    private CubeObserver mObserver;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            moveToNextPage();
        }
    };
    private Timer mTimer;
    private long mDuration = 5000;
    private boolean mAutoMove;

    public CubePager(Context context) {
        this(context, null);
    }

    public CubePager(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSolidPager(context, attrs);
    }

    private void initSolidPager(Context context, AttributeSet attrs) {
        mScroller = new Scroller(context, mInterpolator);
        mMatrix = new Matrix();
        mCamera = new Camera();
        mTouchSlop = ViewConfiguration.get(context).getScaledDoubleTapSlop();
    }

    //  onMeasure决定View本身和它的内容的尺寸
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childLeft = -mWidth;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                child.layout(childLeft, 0, childLeft + getMeasuredWidth(), child.getMeasuredHeight());
            }
            childLeft += child.getMeasuredWidth();
        }
    }

    public void setAdapter(CubePagerAdapter pagerAdapter) {
        mPagerAdapter = pagerAdapter;
        if (mObserver == null) {
            mObserver = new CubeObserver();
        }
        mPagerAdapter.setCubeObserver(mObserver);
        onDataSetChanged();
    }

    //  水平拖动的距离超过双击距离，则拦截MOVE事件交给onTouchEvent处理
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                    scrollTo(0, 0);
                }
                if (mAutoMove) {
                    stopTimer();
                }
                mDownX = ev.getRawX();
                mLastMoveX = mDownX;
                break;
            case MotionEvent.ACTION_MOVE:
                mMoveX = ev.getRawX();
                float delta = Math.abs(mMoveX - mDownX);
                mLastMoveX = mMoveX;
                if (delta > mTouchSlop) {
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float curX = event.getRawX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float deltaX = (mLastMoveX - curX);
                scrollBy(((int) deltaX), 0);
                invalidate();
                mLastMoveX = curX;
                break;
            case MotionEvent.ACTION_UP:
                int dist = ((int) (curX - mDownX));
                if (Math.abs(dist) > mWidth / 2) {
                    if (dist < 0) {// 滑到右边一页
                        mScrollDirect = SCROLL_TO_RIGHT;
                    } else {
                        mScrollDirect = SCROLL_TO_LEFT;
                    }
                } else {
                    mScrollDirect = 0;
                }
                updateLayout(mScrollDirect);
                if (mAutoMove) {
                    startTimer();
                }
                break;
        }
        return true;
    }

    //  必要时父布局用来请求子布局更新其mScrollX和mScrollY，通常在子布局使用scroller实现动画时实现
    @Override
    public void computeScroll() {
//      获取新位置时调用此方法，返回true表示滑动还没有结束
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
//          记得刷新View，否则可能出现滑动中止的异常
            invalidate();
        }
    }

    private void updateLayout(int scrollDirect) {
        int oldPosition = mCurrentPosition;
        switch (scrollDirect) {
            case SCROLL_TO_RIGHT:
                mPagerAdapter.destroyItem(this, mLeftPosition, getChildAt(0));
                mLeftPosition = oldPosition;
                mCurrentPosition = mRightPosition;
                mRightPosition = (mRightPosition + 1) % mItemsCount;
                addView((mPagerAdapter.instantiateItem(this, mRightPosition)), 2);
                break;
            case SCROLL_TO_LEFT:
                mPagerAdapter.destroyItem(this, mRightPosition, getChildAt(2));
                mRightPosition = oldPosition;
                mCurrentPosition = mLeftPosition;
                mLeftPosition = (mLeftPosition + mItemsCount - 1) % mItemsCount;
                addView((mPagerAdapter.instantiateItem(this, mLeftPosition)), 0);
                break;
        }
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageChanged(mCurrentPosition, oldPosition);
        }
//      mScrollDirect 左滑1,右滑-1,其他0
        int startX = getScrollX() + this.mScrollDirect * mWidth;
//      此时可视页面的实际位置已经发生变化（从3-->2或者从1-->2）,需要回到0位置，这里伪造一个持续滚动的假象
        int duration = ((int) (1000 * Math.abs(startX * 2f / mWidth)));
        mScroller.startScroll(startX, 0, -startX, 0, duration);
//        Log.e(TAG, "startX = " + startX + ", duration = " + duration);
        invalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        mMatrix.reset();
        int scrollX = getScrollX();
        float interpolation = scrollX * 1f / mWidth;
//      手势左滑时scrollX>0，页面向右滚动，mCamera的旋转中心在子View的mWidth；反之旋转中心在0
        float centerX = scrollX > 0 ? mWidth : 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int left = child.getLeft() - scrollX;
            int right = child.getRight() - scrollX;
//          如果该页面没有出现在可视的屏幕范围中，跳过此View的绘制
            if (right <= 0 || left >= mWidth) {
                continue;
            }
//          页面左滑由1翻转到2时，页面1的旋转角度由0到-MAX_ROTATE，页面2的旋转角度由MAX_ROTATE到0
//          页面右滑由1翻转到0时，页面0的旋转角度由-MAX_ROTATE到0，页面1的旋转角度由0到+MAX_ROTATE
            float rotate = (i - 1 - interpolation) * MAX_ROTATE;
//            Log.e(TAG, "position = " + i + ", left = " + left + ", right = " + right + ", rotate = " + rotate + ", scrollX = " + scrollX);

            mCamera.save();
            mCamera.rotateY(rotate);
            mCamera.getMatrix(mMatrix);
            mCamera.restore();

            canvas.save();
//          旋转中心位置的偏移
            mMatrix.preTranslate(-centerX, -mHeight / 2);
            mMatrix.postTranslate(centerX, +mHeight / 2);
            canvas.concat(mMatrix);
            drawChild(canvas, child, getDrawingTime());
            canvas.restore();
        }
    }

    public void setAutoMove(boolean autoMove) {
        mAutoMove = autoMove;
        mScrollDirect = SCROLL_TO_RIGHT;
        if (mAutoMove) {
            startTimer();
        }
    }

    public void startTimer() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(0);
            }
        }, mDuration, mDuration);
    }

    public void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = null;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    private void moveToNextPage() {
//      仿照onTouchEvent完成前半部分的动作
        mScroller.startScroll(0, 0, -mScrollDirect * mWidth / 2, 0);
        invalidate();
        updateLayout(mScrollDirect);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    public interface OnPageChangeListener {
        void onPageChanged(int currentPosition, int oldPosition);
    }

    private OnPageChangeListener mOnPageChangeListener;

    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        mOnPageChangeListener = onPageChangeListener;
    }

    public int getItemsCount() {
        return mItemsCount;
    }

    //  数据变化，界面更新
    public void onDataSetChanged() {
        removeAllViews();
        mItemsCount = mPagerAdapter.getCount();
        if (mItemsCount == 0) {
            return;
        }
        if (mItemsCount > 2) {
            mCurrentPosition = 0;
            mRightPosition = 1;
            mLeftPosition = mItemsCount - 1;
            addView(mPagerAdapter.instantiateItem(this, mLeftPosition), 0);
            addView(mPagerAdapter.instantiateItem(this, mCurrentPosition), 1);
            addView(mPagerAdapter.instantiateItem(this, mRightPosition), 2);
        }
        requestLayout();
        if (mAutoMove) {
            startTimer();
        }
    }

    private class CubeObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            onDataSetChanged();
        }
    }
}
