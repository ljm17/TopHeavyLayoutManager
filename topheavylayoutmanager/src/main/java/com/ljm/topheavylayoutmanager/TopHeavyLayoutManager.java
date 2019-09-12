package com.ljm.topheavylayoutmanager;

import android.graphics.PointF;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Top Heavy Layout Manager
 * @Date 2019-9-9
 * @author Ljm
 */
public class TopHeavyLayoutManager extends RecyclerView.LayoutManager implements RecyclerView.SmoothScroller.ScrollVectorProvider {

    /**
     * width of child view
     */
    private int mItemWidth;

    /**
     * height of child view
     */
    private int mItemHeight;

    /**
     * the zoom value of the tail child view
     */
    private float mChildScale;

    /**
     * the zoom value of covered child view
     */
    private float mCoverScale;

    /**
     * gap between child views
     */
    private float mSpace;

    /**
     * horizontal scrolling distance
     */
    private int mSumDx;

    /**
     * distance of the first child view from the left boundary
     */
    private int mLeftResult;

    /**
     * the position of the first child view currently displayed
     */
    private int mBeginPos;

    private int mPendingScrollPosition;

    /**
     * support for infinite loops(item count is Integer.MAX_VALUE)
     */
    private boolean mInfinite;


    public TopHeavyLayoutManager() {
        this.mSpace = 60;
        this.mChildScale = 0.5f;
        this.mCoverScale = 0.8f;
        mPendingScrollPosition = -1;
    }

    public TopHeavyLayoutManager(int space) {
        this.mSpace = space;
        this.mChildScale = 0.5f;
        this.mCoverScale = 0.8f;
        mPendingScrollPosition = -1;
    }

    public TopHeavyLayoutManager(float childScale, float coverScale) {
        this.mSpace = 60;
        this.mChildScale = childScale;
        this.mCoverScale = coverScale;
        mPendingScrollPosition = -1;
    }

    public TopHeavyLayoutManager(int space, float childScale, float coverScale) {
        this.mSpace = space;
        this.mChildScale = childScale;
        this.mCoverScale = coverScale;
        mPendingScrollPosition = -1;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.getItemCount() == 0) {
            removeAndRecycleAllViews(recycler);
        } else {
            if ((state.isPreLayout() || !state.didStructureChange()) && mPendingScrollPosition == -1) {
                return;
            }

            detachAndScrapAttachedViews(recycler);

            View view = recycler.getViewForPosition(0);
            measureChildWithMargins(view, 0, 0);
            mItemWidth = view.getMeasuredWidth();
            mItemHeight = view.getMeasuredHeight();
            removeAndRecycleView(view, recycler);

            mInfinite = state.getItemCount() == Integer.MAX_VALUE;

            if (mPendingScrollPosition != -1) {
                mBeginPos = mPendingScrollPosition;
                mLeftResult = 0;
                mSumDx = mPendingScrollPosition * mItemWidth;
            } else if (mInfinite) {
                mBeginPos = state.getItemCount() >> 1;
                mLeftResult = 0;
                mSumDx = (state.getItemCount() >> 1) * mItemWidth;
            } else {
                mBeginPos = 0;
                mLeftResult = 0;
                mSumDx = 0;
            }

            //calculate and inflate
            int offSet = mLeftResult + getPaddingLeft();
            int visibleCount = getVisibleCount(offSet);
            if (state.getItemCount() < visibleCount) {
                visibleCount = state.getItemCount();
            }
            int lastPos = mBeginPos + visibleCount - 1;
            if (lastPos > state.getItemCount() - 1) {
                lastPos = state.getItemCount() - 1;
            }
            for (int i = mBeginPos; i <= lastPos; i++) {
                insertView(i, recycler, offSet);
                offSet += mItemWidth;
            }
        }
    }

    @Override
    public void onLayoutCompleted(RecyclerView.State state) {
        this.mPendingScrollPosition = -1;
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.getItemCount() == 0 || mItemWidth <= 0) {
            return dx;
        }

        //position of the first child before scrolling
        int firPos = mBeginPos;

        //limit scrolling area
        int travel = dx;
        if (!mInfinite) {
            if (travel + mSumDx < 0) {
                travel = -mSumDx;
            } else if (travel + mSumDx > getMaxOffset()) {
                travel = getMaxOffset() - mSumDx;
            }
        }
        mSumDx += travel;

        //calculate the distance of the first child view from the left boundary before and after scrolling
        int left = mLeftResult;
        int leftResult = ((left - travel) % mItemWidth - mItemWidth) % mItemWidth + getPaddingLeft();
        mLeftResult = leftResult - getPaddingLeft();
        int visibleCount = getVisibleCount(leftResult);

        //confirm the position of the first and last child view in the display area after scrolling
        if (travel >= 0) {
            firPos = -(left - travel) / mItemWidth + firPos;
        } else {
            int s = left - travel;
            if (s > 0) {
                firPos -= s / mItemWidth + 1;
            }
        }
        mBeginPos = firPos;
        int lastPos = firPos + visibleCount - 1;
        if (lastPos > state.getItemCount() - 1) {
            lastPos = state.getItemCount() - 1;
        }

        //recycler
        removeView(travel, recycler, leftResult);

        //inflate
        detachAndScrapAttachedViews(recycler);
        for (int i = firPos; i <= lastPos; i++) {
            insertView(i, recycler, leftResult);
            leftResult += mItemWidth;
        }
        return travel;
    }

    /**
     * Add child to layout
     * @param position child position
     * @param recycler recycler
     * @param left distance from left boundary
     */
    private void insertView(int position, RecyclerView.Recycler recycler, int left) {
        if (position < 0 || mItemWidth <= 0) {
            return;
        }
        View child = recycler.getViewForPosition(position);
        addView(child);

        measureChildWithMargins(child, 0, 0);
        int width = child.getMeasuredWidth();
        int height = child.getMeasuredHeight();
        layoutDecorated(child, left, getPaddingTop(), left + width, height + getPaddingTop());
        handleChildView(child, left - getPaddingLeft());
    }

    /**
     * Remove view that is no longer displayed
     * @param dx distance from left boundary
     * @param recycler recycler
     * @param leftResult leftResult
     */
    private void removeView(int dx, RecyclerView.Recycler recycler, int leftResult) {
        if(dx > 0) {
            while (getChildCount() > 0){
                View child = getChildAt(0);
                int p = getDecoratedRight(child) - dx;
                if (p - getPaddingLeft() <= 0) {
                    removeAndRecycleView(child, recycler);
                } else {
                    break;
                }
            }
        } else {
            while (getChildCount() > 0){
                View child = getChildAt(getChildCount() - 1);
                int p = getDecoratedLeft(child) - dx;
                if (p - getPaddingRight() >= leftResult + getVisibleCount(leftResult) * mItemWidth) {
                    removeAndRecycleView(child, recycler);
                } else {
                    break;
                }
            }
        }
    }

    /**
     * Migration and scaling of child view
     * @param child child view
     * @param x distance from left boundary
     */
    private void handleChildView(View child, int x) {
        float scale = computeScale(x);
        child.setScaleX(scale);
        child.setScaleY(scale);
        child.setTranslationX(-computeTranslationX(x, scale));
        child.setTranslationY(computeTranslationY(x, scale));
    }

    /**
     * Calculate the scaling value of child view
     * @param x distance from left boundary
     * @return scale
     */
    private float computeScale(int x) {
        if (x < -mItemWidth >> 1) {
            return mCoverScale;
        } else if (x < 0) {
            return mCoverScale + (1 - mCoverScale) * (x + (mItemWidth >> 1)) / (mItemWidth >> 1);
        } else if (x < mItemWidth >> 1) {
            return 1;
        } else if (x < mItemWidth) {
            return 1 - (1 - mChildScale) * (x - (mItemWidth >> 1)) / (mItemWidth >> 1);
        } else {
            return mChildScale;
        }
    }

    /**
     * Calculate translationX of child view
     * @param x distance from left boundary
     * @param scale scale
     * @return translationX
     */
    private float computeTranslationX(int x, float scale) {
        if (x < 0) {
            return x + (1 - scale) * mItemWidth / 2;
        } else if (x < mItemWidth >> 1) {
            return 0;
        } else if (x < mItemWidth) {
            return (1 - scale) *  mItemWidth / 2 - (1.0f * x /  (mItemWidth >> 1) - 1) * mSpace;
        } else {
            float s = computeScale(x % mItemWidth);
            float t = computeTranslationX(x % mItemWidth , s);
            int a = x / mItemWidth;
            return (1 - s) * mItemWidth / 2 + t + a * ((1 - scale) * mItemWidth / 2 - mSpace)
                    + (a - 1) * ((1 - scale) * mItemWidth / 2);
        }
    }

    /**
     * Calculate translationY of child view
     * @param x distance from left boundary
     * @param scale scale
     * @return translationY
     */
    private float computeTranslationY(int x, float scale) {
        if (x > 0) {
            return -(1 - scale) * mItemHeight / 2;
        } else {
          return 0;
        }
    }

    /**
     * Display area
     * @return space
     */
    private int getHorizontalSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    /**
     * Maximum horizontal offset
     * @return maxOffset
     */
    private int getMaxOffset() {
        return (getItemCount() - 1) * mItemWidth;
    }

    /**
     * The current screen shows the number of children
     * @param left distance from left boundary
     * @return child count
     */
    private int getVisibleCount(int left) {
        if (left < 0) {
            float s = computeScale(left - getPaddingLeft() + mItemWidth);
            float f = getHorizontalSpace() -(mSpace + s * mItemWidth) - (left - getPaddingLeft() + mItemWidth);
            return (int) Math.ceil((f < 0 ? 0 : f) / (mItemWidth * mChildScale + mSpace) + 2);
        } else {
            float f = 1.0f * (getHorizontalSpace() - mItemWidth - mSpace);
            return (int) Math.ceil((f < 0 ? 0 : f) / (mChildScale * mItemWidth + mSpace) + 1);
        }
    }

    @Override
    public void onAdapterChanged(@Nullable RecyclerView.Adapter oldAdapter, @Nullable RecyclerView.Adapter newAdapter) {
        super.onAdapterChanged(oldAdapter, newAdapter);
    }

    @Override
    public void scrollToPosition(int position) {
        if (position < 0) {
            position = 0;
        } else if (position > getItemCount() - 1) {
            position = getItemCount() - 1;
        }
        this.mPendingScrollPosition = position;
        this.requestLayout();
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        if (position < 0) {
            position = 0;
        } else if (position > getItemCount() - 1) {
            position = getItemCount() - 1;
        }
        LinearSmoothScroller linearSmoothScroller = new TopSmoothScroller(recyclerView.getContext());
        linearSmoothScroller.setTargetPosition(position);
        this.startSmoothScroll(linearSmoothScroller);
    }

    /**
     * Sets the gap between child views
     * @param space space
     */
    public void setSpace(int space) {
        this.mSpace = space;
        this.requestLayout();
    }

    /**
     * Sets the scale of the tail child view
     * @param scale scale
     */
    public void setChildScale(@FloatRange(from = 0, to = 1) float scale) {
        this.mChildScale = scale;
        this.requestLayout();
    }

    /**
     * Sets the scale of the covered child view
     * @param scale scale
     */
    public void setCoverScale(@FloatRange(from = 0, to = 1) float scale) {
        this.mCoverScale = scale;
        this.requestLayout();
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(-2, -2);
    }

    @Override
    public boolean isAutoMeasureEnabled() {
        return true;
    }

    @Nullable
    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        if (this.getChildCount() == 0) {
            return null;
        } else {
            int firstChildPos = this.getPosition(this.getChildAt(0));
            int direction = targetPosition < firstChildPos ? -1 : 1;
            return new PointF((float)direction, 0.0F);
        }
    }
}
