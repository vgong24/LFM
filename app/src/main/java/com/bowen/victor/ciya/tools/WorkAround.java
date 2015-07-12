package com.bowen.victor.ciya.tools;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.WindowManager;

/**
 * Created by Victor on 7/11/2015.
 */
public class WorkAround {
    public static void setNotificationBarColor(Activity activity, int color){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().setStatusBarColor(activity.getResources().getColor(color));
        }

    }

}
