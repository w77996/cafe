package com.w77996.cafeprint.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * time:2017/12/27
 * Created by w77996
 * Github:https://github.com/w77996
 * CSDN:http://blog.csdn.net/w77996?viewmode=contents
 */
public class Utils {
    /**
     * 获取手机屏幕高度
     *
     * @param context
     * @return
     */
    public static int getDisplayHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        // 获取屏幕信息
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    /**
     * 获取手机屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getDisplayWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        // 获取屏幕信息
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }
}
