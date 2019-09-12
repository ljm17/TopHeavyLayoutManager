package com.ljm.topheavylayoutmanager;

import android.content.Context;
import android.support.v7.widget.LinearSmoothScroller;

/**
 * 将子view与父view顶部对齐
 * @Date 2019-9-10
 * @author Ljm
 */
public class TopSmoothScroller extends LinearSmoothScroller {

    TopSmoothScroller(Context context) {
        super(context);
    }

    @Override
    protected int getHorizontalSnapPreference() {
        return SNAP_TO_START;
    }

    @Override
    protected int getVerticalSnapPreference() {
        return SNAP_TO_START;
    }
}
