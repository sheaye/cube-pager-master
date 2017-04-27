package com.sheaye.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yexinyan on 2017/3/23.
 */

public class CubePager extends ViewGroup {

    protected float mDownY;

    public interface OnPageChangeListener {

        void onPageChanged(int currentPosition, int oldPosition);

    }

    private static final int MIN_FLING_VELOCITY = 400; // dips
    private int mActivePointerId;
    private float mMinVelocity;
    private VelocityTracker mVelocityTracker;
    private ArrayList<OnPageChangeListener> mOnPageChangeListeners;
    private static final String TAG = "CubePager";
    private static final int SCROLL_TO_LEFT = 1;
    private static final int SCROLL_TO_RIGHT = -1;
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
    private float mLastMoveX;
    private int mWidth;
    private int mHeight;
    private Camera mCamera;
    private float mMaxRotate = 50;
    private Matrix mMatrix;
    private CubeObserver mObserver;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            scheduleToNextPage(SCROLL_TO_RIGHT);
        }
    };
    private Timer mTimer;
    //  定时翻转的间隔时间
    private long mInterval = 5000;
    //  翻转的持续时间
    private int mDuration = 2000;

    private boolean mAutoMove;
    private static final int LEFT = 0;
    private static final int CURRENT = 1;
    private static final int RIGHT = 2;
    private boolean mWith3D = true;
    private int[] mPositions;

    public CubePager(Context context) {
        this(context, null);
    }

    public CubePager(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCubePager(context, attrs);
    }

    private void initCubePager(Context context, AttributeSet attrs) {
        mScroller = new Scroller(context, mInterpolator);
        mMatrix = new Matrix();
        mCamera = new Camera();
        float density = context.getResources().getDisplayMetrics().density;
        mMinVelocity = MIN_FLING_VELOCITY * density;
    }

    //  onMeasure决定View本身和它的内容的尺寸
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getLayoutParams().height == LayoutParams.WRAP_CONTENT) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                int h = child.getMeasuredHeight();
                if (h > mHeight) {
                    mHeight = h;
                }
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY);
            }
        }
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
                mDownY = ev.getRawY();
                mLastMoveX = mDownX;
                requestParentDisallowIntercept(true);
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = ev.getRawX();
                float moveY = ev.getRawY();
                float deltaX = Math.abs(moveX - mDownX);
                float deltaY = Math.abs(moveY - mDownY);
                mLastMoveX = moveX;
                if (deltaX > deltaY) {
                    requestParentDisallowIntercept(true);
                    return true;
                } else {
                    requestParentDisallowIntercept(false);
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void requestParentDisallowIntercept(boolean disallowIntercept) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getChildCount() == 0) {
            return false;
        }
        int scrollDirect;
        float curX = event.getRawX();
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        mActivePointerId = event.getPointerId(0);
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float deltaX = (mLastMoveX - curX);
                scrollBy(((int) deltaX), 0);
                invalidate();
                mLastMoveX = curX;
                break;
            case MotionEvent.ACTION_UP:
                int dist = ((int) (curX - mDownX));
                mVelocityTracker.computeCurrentVelocity(1000);
                float xVelocity = VelocityTrackerCompat.getXVelocity(mVelocityTracker, mActivePointerId);
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                if (Math.abs(dist) > mWidth / 2 || Math.abs(xVelocity) > mMinVelocity) {
                    if (dist < 0) {// 滑到右边一页
                        scrollDirect = SCROLL_TO_RIGHT;
                    } else {
                        scrollDirect = SCROLL_TO_LEFT;
                    }
                } else {
                    scrollDirect = 0;
                }
                updateLayout(scrollDirect);
                if (mAutoMove) {
                    startTimer();
                }
                break;
        }
        return true;
    }

    private void updateLayout(int scrollDirect) {
        if (scrollDirect != 0) {
            int oldPosition = mPositions[CURRENT];
            adjustViews(scrollDirect);
            int currentPosition = mPositions[CURRENT];
            if (mOnPageChangeListeners != null) {
                for (int i = 0; i < mOnPageChangeListeners.size(); i++) {
                    OnPageChangeListener listener = mOnPageChangeListeners.get(i);
                    if (listener != null) {
                        listener.onPageChanged(currentPosition, oldPosition);
                    }
                }
            }
        }
//      mScrollDirect 左滑1,右滑-1,其他0
        int startX = getScrollX() + scrollDirect * mWidth;
//      此时可视页面的实际位置已经发生变化（从3-->2或者从1-->2）,需要回到0位置，这里伪造一个持续滚动的假象
        int duration = ((int) (mDuration * Math.abs(startX * 1.f / mWidth)));
        mScroller.startScroll(startX, 0, -startX, 0, duration);
//        log("startX = " + startX + ", duration = " + duration);
        invalidate();
    }

    private void adjustViews(int scrollDirect) {
        int destroyIndex, insertIndex;
        if (scrollDirect == SCROLL_TO_RIGHT) {
            destroyIndex = LEFT;
            insertIndex = RIGHT;
        } else {
            destroyIndex = RIGHT;
            insertIndex = LEFT;
        }
        mPagerAdapter.destroyItem(this, mPositions[destroyIndex], destroyIndex);
        movePositions(scrollDirect);
        insertView(insertIndex);
    }

    private void insertView(int index) {
        View child = mPagerAdapter.instantiateItem(this, mPositions[index]);
        if (child != null) {
            addView(child, index);
        }
    }

    private void movePositions(int direct) {
        for (int i = 0; i < mPositions.length; i++) {
            mPositions[i] += -direct;
            if (mPositions[i] < 0) {
                mPositions[i] += mItemsCount;
            } else if (mPositions[i] >= mItemsCount) {
                mPositions[i] = 0;
            }
        }
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

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (mWith3D) {
            drawWith3D(canvas);
        } else {
            super.dispatchDraw(canvas);
        }
    }

    private void drawWith3D(Canvas canvas) {
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
//          页面左滑由1翻转到2时，页面1的旋转角度由0到-mMaxRotate，页面2的旋转角度由MAX_ROTATE到0
//          页面右滑由1翻转到0时，页面0的旋转角度由-MAX_ROTATE到0，页面1的旋转角度由0到+mMaxRotate
            float rotate = (i - 1 - interpolation) * mMaxRotate;
//            log("position = " + i + ", left = " + left + ", right = " + right + ", rotate = " + rotate + ", scrollX = " + scrollX);

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
        }, mInterval, mInterval);
    }

    public void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = null;
    }

    private void scheduleToNextPage(int scrollDirect) {
//      仿照onTouchEvent完成前半部分的动作
        if (getChildCount() > 0) {
            mScroller.startScroll(0, 0, -scrollDirect * mWidth / 2, 0, mDuration / 2);
            invalidate();
            updateLayout(scrollDirect);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    public void addOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        if (mOnPageChangeListeners == null) {
            mOnPageChangeListeners = new ArrayList<>();
        }
        mOnPageChangeListeners.add(onPageChangeListener);
    }

    public void removeOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        if (mOnPageChangeListeners != null) {
            mOnPageChangeListeners.remove(onPageChangeListener);
        }
    }

    public int getItemsCount() {
        return mItemsCount;
    }

    //  数据变化，界面更新
    public void onDataSetChanged() {
        stopTimer();
        removeAllViews();
        mItemsCount = mPagerAdapter.getCount();
        if (mItemsCount == 0) {
            return;
        }
        initPositions();
        for (int i = 0; i < mPositions.length; i++) {
            insertView(i);
        }
        requestLayout();
        if (mAutoMove) {
            startTimer();
        }
    }

    private void initPositions() {
        if (mItemsCount < 2) {
            mPositions = new int[]{0, 0, 0};
        } else {
            mPositions = new int[]{mItemsCount - 1, 0, 1};
        }
    }

    public CubePagerAdapter getPagerAdapter() {
        return mPagerAdapter;
    }

    private class CubeObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            onDataSetChanged();
        }
    }

    public CubePager setMaxRotate(float maxRotate) {
        this.mMaxRotate = maxRotate;
        return this;
    }

    public CubePager setInterval(long interval) {
        mInterval = interval;
        return this;
    }

    public CubePager setDuration(int duration) {
        this.mDuration = duration;
        return this;
    }

    public CubePager setAutoMove(boolean autoMove) {
        mAutoMove = autoMove;
        return this;
    }

    public CubePager setWith3D(boolean with3D) {
        mWith3D = with3D;
        return this;
    }

}
