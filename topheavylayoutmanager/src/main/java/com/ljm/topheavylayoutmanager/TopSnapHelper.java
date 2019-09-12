package com.ljm.topheavylayoutmanager;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.SmoothScroller.ScrollVectorProvider;
import android.support.v7.widget.SnapHelper;
import android.view.View;

/**
 * Start Page Alignment
 * @Date 2019-9-10
 * @author Ljm
 */
public class TopSnapHelper extends SnapHelper {

    /**
     * limit the number of filling pages
     */
    private int mFillingLimit;

    @Nullable
    private OrientationHelper mVerticalHelper;
    @Nullable
    private OrientationHelper mHorizontalHelper;

    public TopSnapHelper() {
    }

    public TopSnapHelper(int fillingLimit) {
        this.mFillingLimit = fillingLimit;
    }

    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull LayoutManager layoutManager, @NonNull View targetView) {
        int[] out = new int[2];
        if (layoutManager.canScrollHorizontally()) {
            out[0] = this.distanceToStart(layoutManager, targetView, this.getHorizontalHelper(layoutManager));
        }

        if (layoutManager.canScrollVertically()) {
            out[1] = this.distanceToStart(layoutManager, targetView, this.getVerticalHelper(layoutManager));
        }

        return out;
    }

    @Override
    public int findTargetSnapPosition(LayoutManager layoutManager, int velocityX, int velocityY) {
        if (!(layoutManager instanceof ScrollVectorProvider)) {
            return -1;
        } else {
            int itemCount = layoutManager.getItemCount();
            if (itemCount == 0) {
                return -1;
            } else {
                View currentView = this.findSnapView(layoutManager);
                if (currentView == null) {
                    return -1;
                } else {
                    int currentPosition = layoutManager.getPosition(currentView);
                    if (currentPosition == -1) {
                        return -1;
                    } else {
                        ScrollVectorProvider vectorProvider = (ScrollVectorProvider)layoutManager;
                        PointF vectorForEnd = vectorProvider.computeScrollVectorForPosition(itemCount - 1);
                        if (vectorForEnd == null) {
                            return -1;
                        } else {
                            int hDeltaJump;
                            if (layoutManager.canScrollHorizontally()) {
                                hDeltaJump = this.estimateNextPositionDiffForFling(layoutManager, this.getHorizontalHelper(layoutManager), velocityX, 0);
                                if (vectorForEnd.x < 0.0F) {
                                    hDeltaJump = -hDeltaJump;
                                }
                            } else {
                                hDeltaJump = 0;
                            }

                            int vDeltaJump;
                            if (layoutManager.canScrollVertically()) {
                                vDeltaJump = this.estimateNextPositionDiffForFling(layoutManager, this.getVerticalHelper(layoutManager), 0, velocityY);
                                if (vectorForEnd.y < 0.0F) {
                                    vDeltaJump = -vDeltaJump;
                                }
                            } else {
                                vDeltaJump = 0;
                            }

                            int deltaJump = layoutManager.canScrollVertically() ? vDeltaJump : hDeltaJump;
                            if (deltaJump == 0) {
                                return -1;
                            } else {
                                if (mFillingLimit > 0 && Math.abs(deltaJump) > mFillingLimit) {
                                    if (deltaJump > 0) {
                                        deltaJump = mFillingLimit;
                                    } else {
                                        deltaJump = -mFillingLimit;
                                    }
                                }
                                int targetPos = currentPosition + deltaJump;
                                if (targetPos < 0) {
                                    targetPos = 0;
                                }

                                if (targetPos >= itemCount) {
                                    targetPos = itemCount - 1;
                                }

                                return targetPos;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public View findSnapView(LayoutManager layoutManager) {
        if (layoutManager.canScrollVertically()) {
            return this.findStartView(layoutManager, this.getVerticalHelper(layoutManager));
        } else {
            return layoutManager.canScrollHorizontally() ? this.findStartView(layoutManager, this.getHorizontalHelper(layoutManager)) : null;
        }
    }

    private int distanceToStart(@NonNull LayoutManager layoutManager, @NonNull View targetView, OrientationHelper helper) {
        int childStart = helper.getDecoratedStart(targetView);
        int containerStart;
        if (layoutManager.getClipToPadding()) {
            containerStart = helper.getStartAfterPadding();
        } else {
            containerStart = 0;
        }

        return childStart - containerStart;
    }

    private int estimateNextPositionDiffForFling(LayoutManager layoutManager, OrientationHelper helper, int velocityX, int velocityY) {
        int[] distances = this.calculateScrollDistance(velocityX, velocityY);
        float distancePerChild = this.computeDistancePerChild(layoutManager, helper);
        if (distancePerChild <= 0.0F) {
            return 0;
        } else {
            int distance = Math.abs(distances[0]) > Math.abs(distances[1]) ? distances[0] : distances[1];
            return Math.round((float)distance / distancePerChild);
        }
    }

    @Nullable
    private View findStartView(LayoutManager layoutManager, OrientationHelper helper) {
        int childCount = layoutManager.getChildCount();
        if (childCount == 0) {
            return null;
        } else {
            View closestChild = null;
            int start;
            if (layoutManager.getClipToPadding()) {
                start = helper.getStartAfterPadding();
            } else {
                start = 0;
            }

            int absClosest = 2147483647;

            for(int i = 0; i < childCount; ++i) {
                View child = layoutManager.getChildAt(i);
                int childStart = helper.getDecoratedStart(child);
                int absDistance = Math.abs(childStart - start);
                if (absDistance < absClosest) {
                    absClosest = absDistance;
                    closestChild = child;
                }
            }

            return closestChild;
        }
    }

    private float computeDistancePerChild(LayoutManager layoutManager, OrientationHelper helper) {
        View minPosView = null;
        View maxPosView = null;
        int minPos = 2147483647;
        int maxPos = -2147483648;
        int childCount = layoutManager.getChildCount();
        if (childCount == 0) {
            return 1.0F;
        } else {
            int start;
            int pos;
            for(start = 0; start < childCount; ++start) {
                View child = layoutManager.getChildAt(start);
                pos = layoutManager.getPosition(child);
                if (pos != -1) {
                    if (pos < minPos) {
                        minPos = pos;
                        minPosView = child;
                    }

                    if (pos > maxPos) {
                        maxPos = pos;
                        maxPosView = child;
                    }
                }
            }

            if (minPosView != null && maxPosView != null) {
                start = Math.min(helper.getDecoratedStart(minPosView), helper.getDecoratedStart(maxPosView));
                int end = Math.max(helper.getDecoratedEnd(minPosView), helper.getDecoratedEnd(maxPosView));
                pos = end - start;
                if (pos == 0) {
                    return 1.0F;
                } else {
                    return 1.0F * (float)pos / (float)(maxPos - minPos + 1);
                }
            } else {
                return 1.0F;
            }
        }
    }

    @NonNull
    private OrientationHelper getVerticalHelper(@NonNull LayoutManager layoutManager) {
        if (this.mVerticalHelper == null || this.mVerticalHelper.getLayoutManager() != layoutManager) {
            this.mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager);
        }

        return this.mVerticalHelper;
    }

    @NonNull
    private OrientationHelper getHorizontalHelper(@NonNull LayoutManager layoutManager) {
        if (this.mHorizontalHelper == null || this.mHorizontalHelper.getLayoutManager() != layoutManager) {
            this.mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
        }

        return this.mHorizontalHelper;
    }
}
