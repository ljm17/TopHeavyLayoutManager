package com.ljm.layoutmanagerdemo.utils;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

public class DimensionUtil {
    
    /**
     * Gets display metrics that describe the size and density of this display.
     * The size returned by this method does not necessarily represent the
     * actual raw size (native resolution) of the display.
     * */
    public static DisplayMetrics getDisplayMetrics(@NonNull Context context) {
        return getDisplayMetricsInner(context);
    }

    /***
     * Return the absolute width of the available display size in pixels.
     * */
    public static int getWidthPixel(Context context) {
        DisplayMetrics displayMetrics = getDisplayMetricsInner(context);
        if (null != displayMetrics) {
            return displayMetrics.widthPixels;
        }
        return 0;
    }

    /***
     * Return the absolute height of the available display size in pixels.
     * */
    public static int getHeightPixel(Context context) {
        DisplayMetrics displayMetrics = getDisplayMetricsInner(context);
        if (null != displayMetrics) {
            return displayMetrics.heightPixels;
        }
        return 0;
    }

    /**
     * Converts an unpacked complex data value holding a dimension to its final floating
     * point value.
     * */
    public static float dp2valueFloat(@NonNull Context context, float dp) {
        DisplayMetrics displayMetrics = getDisplayMetricsInner(context);
        if (null == displayMetrics) {
            return 0;
        }
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
    }

    /**
     * Converts an unpacked complex data value holding a dimension to its final floating
     * point value.
     * */
    public static int dp2valueInt(@NonNull Context context, float dp) {
        DisplayMetrics displayMetrics = getDisplayMetricsInner(context);
        if (null == displayMetrics) {
            return 0;
        }
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
    }

    /**
     * Converts an unpacked complex data value holding a dimension to its final floating
     * point value.
     * */
    public static float px2valueFloat(@NonNull Context context, float px) {
        DisplayMetrics displayMetrics = getDisplayMetricsInner(context);
        if (null == displayMetrics) {
            return 0;
        }
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, displayMetrics);
    }

    /**
     * Converts an unpacked complex data value holding a dimension to its final floating
     * point value.
     * */
    public static int px2valueInt(@NonNull Context context, float px) {
        DisplayMetrics displayMetrics = getDisplayMetricsInner(context);
        if (null == displayMetrics) {
            return 0;
        }
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, displayMetrics);
    }

    /**
     * */
    public static int getStatusBarHeight(@NonNull Context context) {
        Resources resources = context.getResources();
        int identify = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelOffset(identify);
    }

    /**
     * Gets display metrics that describe the size and density of this display.
     * The size returned by this method does not necessarily represent the
     * actual raw size (native resolution) of the display.
     * */
    private static DisplayMetrics getDisplayMetricsInner(@NonNull Context context) {
        Object service = context.getSystemService(Context.WINDOW_SERVICE);
        if (!(service instanceof WindowManager)) {
            return null;
        }
        WindowManager windowManager = (WindowManager) service;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }
}
